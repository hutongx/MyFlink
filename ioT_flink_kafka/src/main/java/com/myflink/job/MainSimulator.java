package com.myflink.job;

import com.myflink.model.CarData;
import com.myflink.utils.CarDataGenerator;
import com.myflink.utils.KafkaCarProducer;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class MainSimulator {
    private static final int NUM_CARS = 100000;
    private static final int GENERATION_INTERVAL_MS = 1000;

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topic = "car-data";

        CarDataGenerator generator = new CarDataGenerator();
        KafkaCarProducer producer = new KafkaCarProducer(bootstrapServers, topic);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        List<String> carIds = new ArrayList<>();

        // Generate car IDs
        for (int i = 0; i < NUM_CARS; i++) {
            carIds.add("CAR-" + i);
        }

        // Schedule periodic data generation and sending
        scheduler.scheduleAtFixedRate(() -> {
            try {
                for (String carId : carIds) {
                    CarData carData = generator.generateCarData();
                    producer.sendCarData(carData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, GENERATION_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            producer.close();
        }));
    }
}
