package com.myflink.job;

import com.myflink.kafka.CarDataKafkaProducer;

public class CarSimulationApplication {
    public static void main(String[] args) {
        final String bootstrapServers = "localhost:9092";
        final String topic = "car-data";
        final int numCars = 100000;

        // Start the Kafka producer
        CarDataKafkaProducer producer = new CarDataKafkaProducer(bootstrapServers, topic, numCars);
        producer.startProducing();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(producer::stop));

        // The Flink job (CarDataProcessor) should be started separately
        System.out.println("Car data simulation started. Press Ctrl+C to stop.");
    }
}
