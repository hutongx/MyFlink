package com.myflink.utils;

import com.myflink.agg.AverageSpeedAggregator;
import com.myflink.model.CarData;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.windowing.time.Time;
import java.util.Properties;

public class CarDataProcessor {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "car-data-processor");

        FlinkKafkaConsumer<String> consumer =
                new FlinkKafkaConsumer<>("car-data", new SimpleStringSchema(), properties);

        // Start from the latest message
        // consumer.setStartFromLatest();
        consumer.setStartFromEarliest();

        DataStream<String> stream = env.addSource(consumer);

        // Parse JSON and process data
        DataStream<CarData> carDataStream = stream
                .map(json -> new ObjectMapper().readValue(json, CarData.class));

        // Example: Calculate average speed per 1 minute window
        /**
        carDataStream
                .keyBy(CarData::getCarId)
                .timeWindow(Time.minutes(1))
                .aggregate(new AverageSpeedAggregator())
                .print();*/
        carDataStream
                .keyBy(CarData::getCarId)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .aggregate(new AverageSpeedAggregator())
                .print();

        env.execute("Car Data Processing");
    }
}