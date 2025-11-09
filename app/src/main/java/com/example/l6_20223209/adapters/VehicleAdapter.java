package com.example.l6_20223209.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20223209.R;
import com.example.l6_20223209.models.Vehicle;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicles;
    private OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onEdit(Vehicle vehicle);
        void onDelete(Vehicle vehicle);
        void onGenerateQR(Vehicle vehicle);
    }

    public VehicleAdapter(List<Vehicle> vehicles, OnVehicleClickListener listener) {
        this.vehicles = vehicles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.bind(vehicle, listener);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleId, tvLicensePlate, tvBrandModel, tvYear, tvReviewDate;
        ImageButton btnEdit, btnDelete, btnQR;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleId = itemView.findViewById(R.id.tvVehicleId);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvBrandModel = itemView.findViewById(R.id.tvBrandModel);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnQR = itemView.findViewById(R.id.btnQR);
        }

        public void bind(Vehicle vehicle, OnVehicleClickListener listener) {
            tvVehicleId.setText(vehicle.getVehicleId());
            tvLicensePlate.setText("Placa: " + vehicle.getLicensePlate());
            tvBrandModel.setText(vehicle.getBrandModel());
            tvYear.setText("Año: " + vehicle.getYear());
            tvReviewDate.setText("Revisión: " + vehicle.getLastTechnicalReviewDate());

            btnEdit.setOnClickListener(v -> listener.onEdit(vehicle));
            btnDelete.setOnClickListener(v -> listener.onDelete(vehicle));
            btnQR.setOnClickListener(v -> listener.onGenerateQR(vehicle));
        }
    }
}
