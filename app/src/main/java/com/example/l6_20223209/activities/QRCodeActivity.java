package com.example.l6_20223209.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.l6_20223209.R;
import com.example.l6_20223209.models.FuelRecord;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.example.l6_20223209.services.FirestoreService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private MaterialToolbar toolbar;
    private TextView tvVehicleId, tvLicensePlate, tvBrandModel, tvYear, tvTechnicalReview, tvLastMileage;

    private FirestoreService firestoreService;
    private FirebaseAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();

        initializeViews();
        loadVehicleDetails();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvVehicleId = findViewById(R.id.tvVehicleId);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvBrandModel = findViewById(R.id.tvBrandModel);
        tvYear = findViewById(R.id.tvYear);
        tvTechnicalReview = findViewById(R.id.tvTechnicalReview);
        tvLastMileage = findViewById(R.id.tvLastMileage);

        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadVehicleDetails() {
        String licensePlate = getIntent().getStringExtra("license_plate");
        String vehicleIdStr = getIntent().getStringExtra("vehicle_id_str");
        String brandModel = getIntent().getStringExtra("brand_model");
        String year = getIntent().getStringExtra("year");
        String reviewDate = getIntent().getStringExtra("review_date");
        String docId = getIntent().getStringExtra("vehicle_doc_id");

        if (docId == null || licensePlate == null) {
            Toast.makeText(this, "Error: Datos del vehículo no disponibles", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar detalles del vehículo
        tvVehicleId.setText("ID: " + vehicleIdStr);
        tvLicensePlate.setText("Placa: " + licensePlate);
        tvBrandModel.setText("Marca/Modelo: " + brandModel);
        tvYear.setText("Año: " + year);
        tvTechnicalReview.setText("Revisión Técnica: " + reviewDate);

        String userId = authService.getCurrentUser().getUid();

        // Obtener el último kilometraje registrado
        firestoreService.getLastMileageForVehicle(userId, docId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double lastMileage = 0;
                    
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Encontrar el kilometraje máximo
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            FuelRecord record = doc.toObject(FuelRecord.class);
                            if (record != null && record.getMileage() > lastMileage) {
                                lastMileage = record.getMileage();
                            }
                        }
                    }

                    tvLastMileage.setText(String.format("Último Kilometraje: %.0f km", lastMileage));

                    String qrContent = String.format(
                            "Revisión Técnica Vehicular\n" +
                            "Placa: %s\n" +
                            "Último Kilometraje: %.0f km\n" +
                            "Fecha Última Revisión: %s",
                            licensePlate, lastMileage, reviewDate
                    );

                    try {
                        Bitmap qrBitmap = generateQRCodeBitmap(qrContent, 800, 800);
                        ivQRCode.setImageBitmap(qrBitmap);
                    } catch (WriterException e) {
                        Toast.makeText(this, "Error al generar código QR", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Bitmap generateQRCodeBitmap(String content, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
