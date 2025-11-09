package com.example.l6_20223209.models;

import com.google.firebase.firestore.DocumentId;

public class Vehicle {
    @DocumentId
    private String id;
    private String userId;
    private String vehicleId; // Nickname
    private String licensePlate;
    private String brandModel;
    private int year;
    private String lastTechnicalReviewDate;

    public Vehicle() {
        // Constructor vacío requerido por Firestore
    }

    public Vehicle(String userId, String vehicleId, String licensePlate, 
                   String brandModel, int year, String lastTechnicalReviewDate) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.brandModel = brandModel;
        this.year = year;
        this.lastTechnicalReviewDate = lastTechnicalReviewDate;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrandModel() {
        return brandModel;
    }

    public void setBrandModel(String brandModel) {
        this.brandModel = brandModel;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getLastTechnicalReviewDate() {
        return lastTechnicalReviewDate;
    }

    public void setLastTechnicalReviewDate(String lastTechnicalReviewDate) {
        this.lastTechnicalReviewDate = lastTechnicalReviewDate;
    }
}
