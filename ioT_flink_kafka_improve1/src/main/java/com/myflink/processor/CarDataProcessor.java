package com.myflink.processor;

import com.myflink.model.CarData;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;

public class CarDataProcessor {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Configure Kafka consumer
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "car-data-processor");

        FlinkKafkaConsumer<String> consumer =
                new FlinkKafkaConsumer<>("car-data", new SimpleStringSchema(), properties);

        // Start from the latest message
        // consumer.setStartFromLatest();
        consumer.setStartFromEarliest();

        DataStream<String> stream = env.addSource(consumer);
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse JSON to CarData objects
        DataStream<CarData> carDataStream = stream
                .map(new MapFunction<String, CarData>() {
                    @Override
                    public CarData map(String json) throws Exception {
                        return objectMapper.readValue(json, CarData.class);
                    }
                });

        // Process speed violations
        carDataStream
                .filter(data -> data.getSpeed() > 120.0) // Speed limit 120 km/h
                .map(data -> new Tuple2<>(data.getCarId(), data.getSpeed()))
                .keyBy(tuple -> tuple.f0)
                .timeWindow(Time.minutes(5))
                .sum(1)
                .print();

        // Process engine temperature alerts
        carDataStream
                .filter(data -> data.getEngineTemperature() > 110.0)
                .map(data -> String.format("High temperature alert for car %s: %.2f°C",
                        data.getCarId(), data.getEngineTemperature()))
                .print();

        // Calculate average speed per car
        carDataStream
                .keyBy(CarData::getCarId)
                .timeWindow(Time.minutes(5))
                .aggregate(new AverageSpeedAggregator())
                .print();

        env.execute("Car Data Processing");
    }
}

class AverageSpeedAggregator implements AggregateFunction<CarData, Tuple2<Double, Long>, Double> {
    @Override
    public Tuple2<Double, Long> createAccumulator() {
        return new Tuple2<>(0.0, 0L);
    }

    @Override
    public Tuple2<Double, Long> add(CarData data, Tuple2<Double, Long> acc) {
        return new Tuple2<>(acc.f0 + data.getSpeed(), acc.f1 + 1);
    }

    @Override
    public Double getResult(Tuple2<Double, Long> acc) {
        return acc.f0 / acc.f1;
    }

    @Override
    public Tuple2<Double, Long> merge(Tuple2<Double, Long> a, Tuple2<Double, Long> b) {
        return new Tuple2<>(a.f0 + b.f0, a.f1 + b.f1);
    }
}
