package com.example.l6_20223209.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20223209.R;
import com.example.l6_20223209.activities.FuelRecordFormActivity;
import com.example.l6_20223209.adapters.FuelRecordAdapter;
import com.example.l6_20223209.models.FuelRecord;
import com.example.l6_20223209.models.Vehicle;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.example.l6_20223209.services.FirestoreService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FuelRecordAdapter adapter;
    private List<FuelRecord> recordList;
    private FloatingActionButton fabAdd;

    private AutoCompleteTextView spinnerVehicle;
    private TextInputEditText etStartDate, etEndDate;
    private MaterialButton btnFilter, btnClearFilter;

    private FirestoreService firestoreService;
    private FirebaseAuthService authService;
    private Map<String, String> vehicleMap; // vehicleId -> vehicle name

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();
        vehicleMap = new HashMap<>();

        initializeViews(view);
        setupRecyclerView();
        loadVehicles();
        loadRecords();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewRecords);
        fabAdd = view.findViewById(R.id.fabAddRecord);
        spinnerVehicle = view.findViewById(R.id.spinnerVehicle);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);

        fabAdd.setOnClickListener(v -> openRecordForm(null));
        btnFilter.setOnClickListener(v -> applyFilter());
        btnClearFilter.setOnClickListener(v -> clearFilter());

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
    }

    private void setupRecyclerView() {
        recordList = new ArrayList<>();
        adapter = new FuelRecordAdapter(recordList, new FuelRecordAdapter.OnRecordClickListener() {
            @Override
            public void onEdit(FuelRecord record) {
                openRecordForm(record);
            }

            @Override
            public void onDelete(FuelRecord record) {
                deleteRecord(record);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadVehicles() {
        String userId = authService.getCurrentUser().getUid();

        firestoreService.getVehiclesByUserIdTask(userId).addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> vehicleNames = new ArrayList<>();
            vehicleNames.add("Todos los vehículos");
            
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Vehicle vehicle = doc.toObject(Vehicle.class);
                String displayName = vehicle.getVehicleId() + " - " + vehicle.getLicensePlate();
                vehicleNames.add(displayName);
                vehicleMap.put(displayName, doc.getId());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, vehicleNames);
            spinnerVehicle.setAdapter(adapter);
            if (!vehicleNames.isEmpty()) {
                spinnerVehicle.setText(vehicleNames.get(0), false);
            }
        });
    }

    private void loadRecords() {
        String userId = authService.getCurrentUser().getUid();

        firestoreService.getFuelRecordsByUserId(userId).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error al cargar registros: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            recordList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    FuelRecord record = doc.toObject(FuelRecord.class);
                    record.setId(doc.getId());
                    recordList.add(record);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void applyFilter() {
        String userId = authService.getCurrentUser().getUid();
        String selectedVehicle = spinnerVehicle.getText().toString();
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        if (!selectedVehicle.equals("Todos los vehículos") && vehicleMap.containsKey(selectedVehicle)) {
            String vehicleId = vehicleMap.get(selectedVehicle);
            firestoreService.getFuelRecordsByVehicle(userId, vehicleId)
                    .addSnapshotListener((value, error) -> updateRecordList(value, error));
        } else if (!startDate.isEmpty() && !endDate.isEmpty()) {
            firestoreService.getFuelRecordsByDateRange(userId, startDate, endDate)
                    .addSnapshotListener((value, error) -> updateRecordList(value, error));
        } else {
            Toast.makeText(getContext(), "Seleccione un filtro válido", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilter() {
        spinnerVehicle.setText("Todos los vehículos", false);
        etStartDate.setText("");
        etEndDate.setText("");
        loadRecords();
    }

    private void updateRecordList(com.google.firebase.firestore.QuerySnapshot value, 
                                   com.google.firebase.firestore.FirebaseFirestoreException error) {
        if (error != null) {
            Toast.makeText(getContext(), "Error al filtrar: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        recordList.clear();
        if (value != null) {
            for (QueryDocumentSnapshot doc : value) {
                FuelRecord record = doc.toObject(FuelRecord.class);
                record.setId(doc.getId());
                recordList.add(record);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void openRecordForm(FuelRecord record) {
        Intent intent = new Intent(getActivity(), FuelRecordFormActivity.class);
        if (record != null) {
            intent.putExtra("record_id", record.getId());
            intent.putExtra("record_code", record.getRecordId());
            intent.putExtra("vehicle_id", record.getVehicleId());
            intent.putExtra("date", record.getDate());
            intent.putExtra("liters", record.getLiters());
            intent.putExtra("mileage", record.getMileage());
            intent.putExtra("price", record.getTotalPrice());
            intent.putExtra("fuel_type", record.getFuelType());
        }
        startActivity(intent);
    }

    private void deleteRecord(FuelRecord record) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar registro")
                .setMessage("¿Está seguro de eliminar este registro?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    firestoreService.deleteFuelRecord(record.getId())
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Registro eliminado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVehicles();
        loadRecords();
    }
}
