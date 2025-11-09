package com.example.l6_20223209.models;

import com.google.firebase.firestore.DocumentId;

public class FuelRecord {
    @DocumentId
    private String id;
    private String recordId; // Código de 5 dígitos
    private String userId;
    private String vehicleId;
    private String vehicleName; // Para mostrar en el listado
    private String date;
    private double liters;
    private double mileage;
    private double totalPrice;
    private String fuelType; // Gasolina, GLP, GNV

    public FuelRecord() {
        // Constructor vacío requerido por Firestore
    }

    public FuelRecord(String recordId, String userId, String vehicleId, String vehicleName,
                      String date, double liters, double mileage, double totalPrice, String fuelType) {
        this.recordId = recordId;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.date = date;
        this.liters = liters;
        this.mileage = mileage;
        this.totalPrice = totalPrice;
        this.fuelType = fuelType;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
}
