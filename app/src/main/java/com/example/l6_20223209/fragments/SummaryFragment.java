package com.example.l6_20223209.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.l6_20223209.R;
import com.example.l6_20223209.models.FuelRecord;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.example.l6_20223209.services.FirestoreService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvTotalLiters, tvTotalSpent, tvAverageMileage;

    private FirestoreService firestoreService;
    private FirebaseAuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();

        initializeViews(view);
        loadData();

        return view;
    }

    private void initializeViews(View view) {
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        tvTotalLiters = view.findViewById(R.id.tvTotalLiters);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvAverageMileage = view.findViewById(R.id.tvAverageMileage);

        setupCharts();
    }

    private void setupCharts() {
        // Configurar BarChart
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Configurar PieChart
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawEntryLabels(true);
    }

    private void loadData() {
        String userId = authService.getCurrentUser().getUid();

        firestoreService.getFuelRecordsForSummary(userId).addOnSuccessListener(queryDocumentSnapshots -> {
            List<FuelRecord> records = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FuelRecord record = doc.toObject(FuelRecord.class);
                records.add(record);
            }

            calculateStatistics(records);
            setupBarChart(records);
            setupPieChart(records);
        });
    }

    private void calculateStatistics(List<FuelRecord> records) {
        double totalLiters = 0;
        double totalSpent = 0;
        double totalMileage = 0;

        for (FuelRecord record : records) {
            totalLiters += record.getLiters();
            totalSpent += record.getTotalPrice();
            totalMileage += record.getMileage();
        }

        double averageMileage = records.isEmpty() ? 0 : totalMileage / records.size();

        tvTotalLiters.setText(String.format(Locale.getDefault(), "%.2f L", totalLiters));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "S/ %.2f", totalSpent));
        tvAverageMileage.setText(String.format(Locale.getDefault(), "%.0f km", averageMileage));
    }

    private void setupBarChart(List<FuelRecord> records) {
        Map<String, Float> monthlyData = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

        for (FuelRecord record : records) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(record.getDate());
                if (date != null) {
                    String monthKey = sdf.format(date);
                    float liters = (float) record.getLiters();
                    monthlyData.put(monthKey, monthlyData.getOrDefault(monthKey, 0f) + liters);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        // Obtener los últimos 6 meses
        Calendar calendar = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.MONTH, -i);
            String monthKey = sdf.format(calendar.getTime());
            String monthLabel = monthFormat.format(calendar.getTime());
            
            float value = monthlyData.getOrDefault(monthKey, 0f);
            entries.add(new BarEntry(index, value));
            labels.add(monthLabel);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Litros por Mes");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.invalidate();
    }

    private void setupPieChart(List<FuelRecord> records) {
        Map<String, Float> fuelTypeData = new HashMap<>();

        for (FuelRecord record : records) {
            String fuelType = record.getFuelType();
            float liters = (float) record.getLiters();
            fuelTypeData.put(fuelType, fuelTypeData.getOrDefault(fuelType, 0f) + liters);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : fuelTypeData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Tipo de Combustible");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
