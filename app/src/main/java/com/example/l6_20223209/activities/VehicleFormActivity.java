package com.example.l6_20223209.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.l6_20223209.R;
import com.example.l6_20223209.models.Vehicle;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.example.l6_20223209.services.FirestoreService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class VehicleFormActivity extends AppCompatActivity {

    private TextInputEditText etVehicleId, etLicensePlate, etBrandModel, etYear, etReviewDate;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    private FirestoreService firestoreService;
    private FirebaseAuthService authService;

    private String vehicleDocId = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_form);

        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();

        initializeViews();
        loadDataIfEdit();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etVehicleId = findViewById(R.id.etVehicleId);
        etLicensePlate = findViewById(R.id.etLicensePlate);
        etBrandModel = findViewById(R.id.etBrandModel);
        etYear = findViewById(R.id.etYear);
        etReviewDate = findViewById(R.id.etReviewDate);
        btnSave = findViewById(R.id.btnSave);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadDataIfEdit() {
        vehicleDocId = getIntent().getStringExtra("vehicle_id");
        if (vehicleDocId != null) {
            isEditMode = true;
            toolbar.setTitle("Editar Vehículo");
            
            etVehicleId.setText(getIntent().getStringExtra("vehicle_nickname"));
            etLicensePlate.setText(getIntent().getStringExtra("license_plate"));
            etBrandModel.setText(getIntent().getStringExtra("brand_model"));
            etYear.setText(String.valueOf(getIntent().getIntExtra("year", 0)));
            etReviewDate.setText(getIntent().getStringExtra("review_date"));
        } else {
            toolbar.setTitle("Nuevo Vehículo");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveVehicle());
        
        etReviewDate.setOnClickListener(v -> showDatePicker());

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    calendar.set(year, month, dayOfMonth);
                    etReviewDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveVehicle() {
        String vehicleId = etVehicleId.getText().toString().trim();
        String licensePlate = etLicensePlate.getText().toString().trim();
        String brandModel = etBrandModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String reviewDate = etReviewDate.getText().toString().trim();

        if (!validateInputs(vehicleId, licensePlate, brandModel, yearStr, reviewDate)) {
            return;
        }

        int year = Integer.parseInt(yearStr);
        String userId = authService.getCurrentUser().getUid();

        Vehicle vehicle = new Vehicle(userId, vehicleId, licensePlate, brandModel, year, reviewDate);

        if (isEditMode) {
            vehicle.setId(vehicleDocId);
            firestoreService.updateVehicle(vehicleDocId, vehicle)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Vehículo actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> 
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            firestoreService.addVehicle(vehicle)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Vehículo guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> 
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private boolean validateInputs(String vehicleId, String licensePlate, String brandModel, 
                                    String yearStr, String reviewDate) {
        if (TextUtils.isEmpty(vehicleId)) {
            etVehicleId.setError("Ingrese ID del vehículo");
            return false;
        }

        if (TextUtils.isEmpty(licensePlate)) {
            etLicensePlate.setError("Ingrese placa");
            return false;
        }

        if (TextUtils.isEmpty(brandModel)) {
            etBrandModel.setError("Ingrese marca/modelo");
            return false;
        }

        if (TextUtils.isEmpty(yearStr)) {
            etYear.setError("Ingrese año");
            return false;
        }

        try {
            int year = Integer.parseInt(yearStr);
            if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR) + 1) {
                etYear.setError("Año inválido");
                return false;
            }
        } catch (NumberFormatException e) {
            etYear.setError("Ingrese un año válido");
            return false;
        }

        if (TextUtils.isEmpty(reviewDate)) {
            etReviewDate.setError("Ingrese fecha de revisión");
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
