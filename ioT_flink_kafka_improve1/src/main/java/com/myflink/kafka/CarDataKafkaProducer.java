package com.myflink.kafka;

import com.myflink.model.CarData;
import com.myflink.utils.CarDataGenerator;
import org.apache.kafka.clients.producer.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class CarDataKafkaProducer {
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final CarDataGenerator generator;
    private final List<String> activeCarIds;
    private volatile boolean running = true;

    public CarDataKafkaProducer(String bootstrapServers, String topic, int numCars) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "1");
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);

        this.producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.objectMapper = new ObjectMapper();
        this.generator = new CarDataGenerator();
        this.executorService = Executors.newFixedThreadPool(10);
        this.activeCarIds = new ArrayList<>();

        // Initialize car IDs
        for (int i = 0; i < numCars; i++) {
            activeCarIds.add("CAR-" + i);
        }
    }

    public void startProducing() {
        // Create initial state for all cars
        Map<String, CarData> carStates = new ConcurrentHashMap<>();
        for (String carId : activeCarIds) {
            carStates.put(carId, generator.generateCarData(carId));
        }

        // Start periodic data generation and sending
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            for (String carId : activeCarIds) {
                CarData previousState = carStates.get(carId);
                CarData newState = generator.generateCorrelatedData(previousState);
                carStates.put(carId, newState);

                sendCarData(newState);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void sendCarData(CarData carData) {
        executorService.submit(() -> {
            try {
                String jsonData = objectMapper.writeValueAsString(carData);
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(topic, carData.getCarId(), jsonData);

                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        producer.close();
    }
}
