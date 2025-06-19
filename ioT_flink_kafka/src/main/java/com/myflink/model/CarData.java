package com.myflink.model;

import lombok.Data;

@Data
public class CarData {
    private String carId;
    private long timestamp;
    private double latitude;
    private double longitude;
    private double speed;
    private double fuelLevel;
    private double engineTemperature;
    private String status;
    private double heading;

    // Constructor
    public CarData(String carId) {
        this.carId = carId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters (omitted for brevity)
    // Add standard getters/setters for all fields
}
