package com.my_flink.vehicle_streaming.flink_job;

import com.my_flink.vehicle_streaming.common.Telemetry;
import com.my_flink.vehicle_streaming.flink_job.aggregate.AvgSpeedAgg;
import com.my_flink.vehicle_streaming.flink_job.cep.OverSpeedPattern;
import com.my_flink.vehicle_streaming.flink_job.enrichment.EnrichmentFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.*;

import java.time.Duration;
import java.util.Properties;

/**
public class VehicleTelemetryJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
        env.setParallelism(400);
        // Checkpoint & StateBackend
        env.enableCheckpointing(30000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(10000);
        env.setStateBackend(new FsStateBackend("hdfs://namenode:8020/flink/checkpoints", true));

        // Kafka Source
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "broker1:9092,broker2:9092");
        kafkaProps.put("group.id", "flink-telemetry");
        FlinkKafkaConsumer<Telemetry> source = new FlinkKafkaConsumer<>(
                "vehicle-telemetry-raw",
                new AvroDeserializationSchema<>(Telemetry.class),
                kafkaProps
        );
        source.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Telemetry>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((t, ts) -> t.getTimestamp())
        );
        // 数据流
        env
                .addSource(source)
                .keyBy(Telemetry::getVehicleId)
                // 异常检测：超速 CEP
                .process(new OverSpeedPattern(80.0))
                // 维度广播关联
                .keyBy(t -> t.getVehicleId())
                .process(new EnrichmentFunction())
                // 1 分钟窗口聚合
                .keyBy(t -> t.getVehicleId())
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new AvgSpeedAgg())
                // sink to Kafka
                .addSink(new FlinkKafkaProducer<>(
                        "vehicle-speed-agg",
                        new JsonSerializationSchema<>(),
                        kafkaProps,
                        FlinkKafkaProducer.Semantic.EXACTLY_ONCE
                ));

        env.execute("Vehicle Telemetry Real-Time Processing");
    }
}*/

