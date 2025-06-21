package com.myflink.utils;

import com.myflink.model.CarData;

import java.util.Random;
import java.util.UUID;

public class CarDataGenerator {
    private static final String[] POSSIBLE_STATUSES = {
            "RUNNING", "IDLE", "STOPPED", "MAINTENANCE"
    };
    private final Random random = new Random();

    // Bounds for generating realistic data
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double MAX_SPEED = 200.0; // km/h
    private static final int MAX_RPM = 8000;

    public CarData generateCarData(String carId) {
        CarData data = new CarData(carId != null ? carId : UUID.randomUUID().toString());

        // Generate random but realistic values
        data.setLatitude(generateRandomInRange(MIN_LATITUDE, MAX_LATITUDE));
        data.setLongitude(generateRandomInRange(MIN_LONGITUDE, MAX_LONGITUDE));
        data.setSpeed(generateRandomInRange(0, MAX_SPEED));
        data.setFuelLevel(generateRandomInRange(0, 100));
        data.setEngineTemperature(generateRandomInRange(80, 120));
        data.setStatus(POSSIBLE_STATUSES[random.nextInt(POSSIBLE_STATUSES.length)]);
        data.setHeading(generateRandomInRange(0, 360));
        data.setEngineRpm(random.nextInt(MAX_RPM));
        data.setAcceleration(generateRandomInRange(-5, 5));
        data.setBrakeEngaged(random.nextBoolean());
        data.setBatteryLevel(generateRandomInRange(11.5, 12.5));

        return data;
    }

    private double generateRandomInRange(double min, double max) {
        return min + (random.nextDouble() * (max - min));
    }

    // Generate correlated data based on previous state
    public CarData generateCorrelatedData(CarData previousData) {
        CarData newData = new CarData(previousData.getCarId());

        // Update position based on speed and heading
        double distance = previousData.getSpeed() * (1.0 / 3600.0); // Convert km/h to km/s
        double headingRad = Math.toRadians(previousData.getHeading());

        double newLat = previousData.getLatitude() +
                (distance * Math.cos(headingRad) / 111.32); // 1 degree = 111.32 km
        double newLon = previousData.getLongitude() +
                (distance * Math.sin(headingRad) / (111.32 * Math.cos(Math.toRadians(newLat))));

        newData.setLatitude(newLat);
        newData.setLongitude(newLon);

        // Gradually change other parameters
        newData.setSpeed(previousData.getSpeed() + generateRandomInRange(-5, 5));
        newData.setFuelLevel(previousData.getFuelLevel() - generateRandomInRange(0, 0.1));
        newData.setEngineTemperature(
                previousData.getEngineTemperature() + generateRandomInRange(-1, 1));

        return newData;
    }
}
