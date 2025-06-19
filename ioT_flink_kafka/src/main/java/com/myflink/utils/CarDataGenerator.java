package com.myflink.utils;

import com.myflink.model.CarData;

import java.util.Random;
import java.util.UUID;

public class CarDataGenerator {
    private final Random random = new Random();

    public CarData generateCarData() {
        String carId = UUID.randomUUID().toString();
        CarData data = new CarData(carId);

        // Generate random but realistic values
        data.setLatitude(generateRandomLatitude());
        data.setLongitude(generateRandomLongitude());
        data.setSpeed(random.nextDouble() * 120); // 0-120 km/h
        data.setFuelLevel(random.nextDouble() * 100); // 0-100%
        data.setEngineTemperature(80 + random.nextDouble() * 40); // 80-120 degrees
        data.setStatus(generateRandomStatus());
        data.setHeading(random.nextDouble() * 360); // 0-360 degrees

        return data;
    }

    private double generateRandomLatitude() {
        // Generate latitude between -90 and 90
        return random.nextDouble() * 180 - 90;
    }

    private double generateRandomLongitude() {
        // Generate longitude between -180 and 180
        return random.nextDouble() * 360 - 180;
    }

    private String generateRandomStatus() {
        String[] statuses = {"RUNNING", "IDLE", "STOPPED", "MAINTENANCE"};
        return statuses[random.nextInt(statuses.length)];
    }
}
