package com.myflink.utils;

import com.myflink.model.CarData;
import org.apache.kafka.clients.producer.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import java.util.concurrent.*;

public class KafkaCarProducer {
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 10;

    public KafkaCarProducer(String bootstrapServers, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);

        this.producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void sendCarData(CarData carData) {
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

    public void close() {
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