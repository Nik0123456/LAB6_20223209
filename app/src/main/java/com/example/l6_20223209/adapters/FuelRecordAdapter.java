package com.example.l6_20223209.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20223209.R;
import com.example.l6_20223209.models.FuelRecord;

import java.util.List;
import java.util.Locale;

public class FuelRecordAdapter extends RecyclerView.Adapter<FuelRecordAdapter.RecordViewHolder> {

    private List<FuelRecord> records;
    private OnRecordClickListener listener;

    public interface OnRecordClickListener {
        void onEdit(FuelRecord record);
        void onDelete(FuelRecord record);
    }

    public FuelRecordAdapter(List<FuelRecord> records, OnRecordClickListener listener) {
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fuel_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        FuelRecord record = records.get(position);
        holder.bind(record, listener);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecordId, tvVehicle, tvDate, tvLiters, tvMileage, tvPrice, tvFuelType;
        ImageButton btnEdit, btnDelete;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecordId = itemView.findViewById(R.id.tvRecordId);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLiters = itemView.findViewById(R.id.tvLiters);
            tvMileage = itemView.findViewById(R.id.tvMileage);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvFuelType = itemView.findViewById(R.id.tvFuelType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(FuelRecord record, OnRecordClickListener listener) {
            tvRecordId.setText("Código: " + record.getRecordId());
            tvVehicle.setText(record.getVehicleName());
            tvDate.setText("Fecha: " + record.getDate());
            tvLiters.setText(String.format(Locale.getDefault(), "%.2f L", record.getLiters()));
            tvMileage.setText(String.format(Locale.getDefault(), "%.0f km", record.getMileage()));
            tvPrice.setText(String.format(Locale.getDefault(), "S/ %.2f", record.getTotalPrice()));
            tvFuelType.setText(record.getFuelType());

            btnEdit.setOnClickListener(v -> listener.onEdit(record));
            btnDelete.setOnClickListener(v -> listener.onDelete(record));
        }
    }
}
