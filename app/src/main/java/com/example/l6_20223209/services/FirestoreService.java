package com.example.l6_20223209.services;

import com.example.l6_20223209.models.FuelRecord;
import com.example.l6_20223209.models.Vehicle;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreService {
    
    private static FirestoreService instance;
    private FirebaseFirestore db;
    
    private static final String VEHICLES_COLLECTION = "vehicles";
    private static final String FUEL_RECORDS_COLLECTION = "fuelRecords";

    private FirestoreService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    // ==================== VEHICLES ====================
    
    public Task<DocumentReference> addVehicle(Vehicle vehicle) {
        return db.collection(VEHICLES_COLLECTION).add(vehicle);
    }

    public Task<Void> updateVehicle(String vehicleId, Vehicle vehicle) {
        return db.collection(VEHICLES_COLLECTION).document(vehicleId).set(vehicle);
    }

    public Task<Void> deleteVehicle(String vehicleId) {
        return db.collection(VEHICLES_COLLECTION).document(vehicleId).delete();
    }

    public Query getVehiclesByUserId(String userId) {
        return db.collection(VEHICLES_COLLECTION)
                .whereEqualTo("userId", userId);
    }

    public Task<QuerySnapshot> getVehiclesByUserIdTask(String userId) {
        return db.collection(VEHICLES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();
    }

    // ==================== FUEL RECORDS ====================
    
    public Task<DocumentReference> addFuelRecord(FuelRecord record) {
        return db.collection(FUEL_RECORDS_COLLECTION).add(record);
    }

    public Task<Void> updateFuelRecord(String recordId, FuelRecord record) {
        return db.collection(FUEL_RECORDS_COLLECTION).document(recordId).set(record);
    }

    public Task<Void> deleteFuelRecord(String recordId) {
        return db.collection(FUEL_RECORDS_COLLECTION).document(recordId).delete();
    }

    public Query getFuelRecordsByUserId(String userId) {
        return db.collection(FUEL_RECORDS_COLLECTION)
                .whereEqualTo("userId", userId);
    }

    public Query getFuelRecordsByVehicle(String userId, String vehicleId) {
        return db.collection(FUEL_RECORDS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("vehicleId", vehicleId);
    }

    public Query getFuelRecordsByDateRange(String userId, String startDate, String endDate) {
        return db.collection(FUEL_RECORDS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate);
    }

    public Task<QuerySnapshot> getLastMileageForVehicle(String userId, String vehicleId) {
        return db.collection(FUEL_RECORDS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("vehicleId", vehicleId)
                .get();
    }

    public Task<QuerySnapshot> getFuelRecordsForSummary(String userId) {
        return db.collection(FUEL_RECORDS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();
    }
}
