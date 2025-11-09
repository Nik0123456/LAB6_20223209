package com.example.l6_20223209.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20223209.R;
import com.example.l6_20223209.activities.QRCodeActivity;
import com.example.l6_20223209.activities.VehicleFormActivity;
import com.example.l6_20223209.adapters.VehicleAdapter;
import com.example.l6_20223209.models.Vehicle;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.example.l6_20223209.services.FirestoreService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class VehiclesFragment extends Fragment {

    private RecyclerView recyclerView;
    private VehicleAdapter adapter;
    private List<Vehicle> vehicleList;
    private FloatingActionButton fabAdd;

    private FirestoreService firestoreService;
    private FirebaseAuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicles, container, false);

        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();

        initializeViews(view);
        setupRecyclerView();
        loadVehicles();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewVehicles);
        fabAdd = view.findViewById(R.id.fabAddVehicle);

        fabAdd.setOnClickListener(v -> openVehicleForm(null));
    }

    private void setupRecyclerView() {
        vehicleList = new ArrayList<>();
        adapter = new VehicleAdapter(vehicleList, new VehicleAdapter.OnVehicleClickListener() {
            @Override
            public void onEdit(Vehicle vehicle) {
                openVehicleForm(vehicle);
            }

            @Override
            public void onDelete(Vehicle vehicle) {
                deleteVehicle(vehicle);
            }

            @Override
            public void onGenerateQR(Vehicle vehicle) {
                generateQRCode(vehicle);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadVehicles() {
        String userId = authService.getCurrentUser().getUid();

        firestoreService.getVehiclesByUserId(userId).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error al cargar vehículos: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                return;
            }

            vehicleList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Vehicle vehicle = doc.toObject(Vehicle.class);
                    vehicle.setId(doc.getId());
                    vehicleList.add(vehicle);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void openVehicleForm(Vehicle vehicle) {
        Intent intent = new Intent(getActivity(), VehicleFormActivity.class);
        if (vehicle != null) {
            intent.putExtra("vehicle_id", vehicle.getId());
            intent.putExtra("vehicle_nickname", vehicle.getVehicleId());
            intent.putExtra("license_plate", vehicle.getLicensePlate());
            intent.putExtra("brand_model", vehicle.getBrandModel());
            intent.putExtra("year", vehicle.getYear());
            intent.putExtra("review_date", vehicle.getLastTechnicalReviewDate());
        }
        startActivity(intent);
    }

    private void deleteVehicle(Vehicle vehicle) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar vehículo")
                .setMessage("¿Está seguro de eliminar este vehículo?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    firestoreService.deleteVehicle(vehicle.getId())
                            .addOnSuccessListener(aVoid -> 
                                    Toast.makeText(getContext(), "Vehículo eliminado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> 
                                    Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void generateQRCode(Vehicle vehicle) {
        Intent intent = new Intent(getActivity(), QRCodeActivity.class);
        intent.putExtra("license_plate", vehicle.getLicensePlate());
        intent.putExtra("vehicle_doc_id", vehicle.getId());
        intent.putExtra("vehicle_id_str", vehicle.getVehicleId());
        intent.putExtra("brand_model", vehicle.getBrandModel());
        intent.putExtra("year", String.valueOf(vehicle.getYear()));
        intent.putExtra("review_date", vehicle.getLastTechnicalReviewDate());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVehicles();
    }
}
