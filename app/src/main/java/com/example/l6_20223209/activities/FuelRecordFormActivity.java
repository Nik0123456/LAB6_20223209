package com.example.l6_20223209.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.l6_20223209.R;
import com.example.l6_20223209.models.FuelRecord;
import com.example.l6_20223209.models.Vehicle;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.example.l6_20223209.services.FirestoreService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class FuelRecordFormActivity extends AppCompatActivity {

    private TextInputEditText etRecordId, etDate, etLiters, etMileage, etPrice;
    private AutoCompleteTextView spinnerVehicle, spinnerFuelType;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    private FirestoreService firestoreService;
    private FirebaseAuthService authService;

    private Map<String, String> vehicleMap; // displayName -> vehicleId
    private String recordDocId = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_record_form);

        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();
        vehicleMap = new HashMap<>();

        initializeViews();
        setupFuelTypeSpinner();
        loadVehicles();
        loadDataIfEdit();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etRecordId = findViewById(R.id.etRecordId);
        spinnerVehicle = findViewById(R.id.spinnerVehicle);
        etDate = findViewById(R.id.etDate);
        etLiters = findViewById(R.id.etLiters);
        etMileage = findViewById(R.id.etMileage);
        etPrice = findViewById(R.id.etPrice);
        spinnerFuelType = findViewById(R.id.spinnerFuelType);
        btnSave = findViewById(R.id.btnSave);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etRecordId.setEnabled(false);
    }

    private void setupFuelTypeSpinner() {
        String[] fuelTypes = {"Gasolina", "GLP", "GNV"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, fuelTypes);
        spinnerFuelType.setAdapter(adapter);
    }

    private void loadVehicles() {
        String userId = authService.getCurrentUser().getUid();

        firestoreService.getVehiclesByUserIdTask(userId).addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> vehicleNames = new ArrayList<>();

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Vehicle vehicle = doc.toObject(Vehicle.class);
                String displayName = vehicle.getVehicleId() + " - " + vehicle.getLicensePlate();
                vehicleNames.add(displayName);
                vehicleMap.put(displayName, doc.getId());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, vehicleNames);
            spinnerVehicle.setAdapter(adapter);
        });
    }

    private void loadDataIfEdit() {
        recordDocId = getIntent().getStringExtra("record_id");
        if (recordDocId != null) {
            isEditMode = true;
            toolbar.setTitle("Editar Registro");

            etRecordId.setText(getIntent().getStringExtra("record_code"));
            etDate.setText(getIntent().getStringExtra("date"));
            etLiters.setText(String.valueOf(getIntent().getDoubleExtra("liters", 0)));
            etMileage.setText(String.valueOf(getIntent().getDoubleExtra("mileage", 0)));
            etPrice.setText(String.valueOf(getIntent().getDoubleExtra("price", 0)));
            spinnerFuelType.setText(getIntent().getStringExtra("fuel_type"), false);
            
            // Set vehicle
            String vehicleId = getIntent().getStringExtra("vehicle_id");
            for (Map.Entry<String, String> entry : vehicleMap.entrySet()) {
                if (entry.getValue().equals(vehicleId)) {
                    spinnerVehicle.setText(entry.getKey(), false);
                    break;
                }
            }
        } else {
            toolbar.setTitle("Nuevo Registro");
            etRecordId.setText(generateRecordId());
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveRecord());
        etDate.setOnClickListener(v -> showDatePicker());
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    calendar.set(year, month, dayOfMonth);
                    etDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String generateRecordId() {
        Random random = new Random();
        return String.format(Locale.getDefault(), "%05d", random.nextInt(100000));
    }

    private void saveRecord() {
        String recordId = etRecordId.getText().toString().trim();
        String selectedVehicle = spinnerVehicle.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String litersStr = etLiters.getText().toString().trim();
        String mileageStr = etMileage.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String fuelType = spinnerFuelType.getText().toString().trim();

        if (!validateInputs(recordId, selectedVehicle, date, litersStr, mileageStr, priceStr, fuelType)) {
            return;
        }

        double liters = Double.parseDouble(litersStr);
        double mileage = Double.parseDouble(mileageStr);
        double price = Double.parseDouble(priceStr);

        String vehicleId = vehicleMap.get(selectedVehicle);
        String userId = authService.getCurrentUser().getUid();

        // Verificar que el kilometraje sea mayor al último registrado
        if (!isEditMode) {
            firestoreService.getLastMileageForVehicle(userId, vehicleId)
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Encontrar el registro con el kilometraje más alto
                            double maxMileage = 0;
                            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                FuelRecord record = doc.toObject(FuelRecord.class);
                                if (record != null && record.getMileage() > maxMileage) {
                                    maxMileage = record.getMileage();
                                }
                            }
                            
                            if (maxMileage > 0 && mileage <= maxMileage) {
                                Toast.makeText(this, 
                                        "El kilometraje debe ser mayor a " + maxMileage, 
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        saveFuelRecord(recordId, vehicleId, selectedVehicle, date, liters, mileage, price, fuelType, userId);
                    });
        } else {
            saveFuelRecord(recordId, vehicleId, selectedVehicle, date, liters, mileage, price, fuelType, userId);
        }
    }

    private void saveFuelRecord(String recordId, String vehicleId, String vehicleName,
                                String date, double liters, double mileage, double price, 
                                String fuelType, String userId) {
        FuelRecord record = new FuelRecord(recordId, userId, vehicleId, vehicleName, 
                date, liters, mileage, price, fuelType);

        if (isEditMode) {
            record.setId(recordDocId);
            firestoreService.updateFuelRecord(recordDocId, record)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Registro actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            firestoreService.addFuelRecord(record)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private boolean validateInputs(String recordId, String vehicle, String date,
                                    String liters, String mileage, String price, String fuelType) {
        if (TextUtils.isEmpty(recordId)) {
            etRecordId.setError("Código de registro requerido");
            return false;
        }

        if (TextUtils.isEmpty(vehicle)) {
            spinnerVehicle.setError("Seleccione un vehículo");
            return false;
        }

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Ingrese fecha");
            return false;
        }

        if (TextUtils.isEmpty(liters) || Double.parseDouble(liters) <= 0) {
            etLiters.setError("Ingrese litros válidos");
            return false;
        }

        if (TextUtils.isEmpty(mileage) || Double.parseDouble(mileage) <= 0) {
            etMileage.setError("Ingrese kilometraje válido");
            return false;
        }

        if (TextUtils.isEmpty(price) || Double.parseDouble(price) <= 0) {
            etPrice.setError("Ingrese precio válido");
            return false;
        }

        if (TextUtils.isEmpty(fuelType)) {
            spinnerFuelType.setError("Seleccione tipo de combustible");
            return false;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
