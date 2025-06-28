package com.my_flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
//import org.apache.flink.runtime.state.rocksdb.RocksDBStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;

import java.time.Duration;
import java.util.Properties;

public class SlidingWindowUVJob {

    private static final long WINDOW_SIZE_MS = 60 * 60 * 1000L;
    private static final long ALLOWED_LATENESS_MS = 60 * 1000L;

    public static void main(String[] args) throws Exception {
        // 1. 环境 & Checkpoint
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 2. 配置状态后端和检查点
        /**
        env.enableCheckpointing(120_000L, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(60_000L);
        env.getCheckpointConfig().setCheckpointTimeout(60_000L);
        env.getCheckpointConfig().enableExternalizedCheckpoints(
                org.apache.flink.streaming.api.CheckpointingOptions.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
        );
        env.setStateBackend(
                new RocksDBStateBackend("hdfs://namenode:8020/flink/checkpoints", true)
        );*/
        env.setStateBackend(new RocksDBStateBackend("hdfs://namenode:port/flink/checkpoints"));
        env.enableCheckpointing(60000); // 1分钟检查点
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
        env.getCheckpointConfig().setCheckpointTimeout(600000); // 10分钟超时

        // 2. Kafka Source
        Properties consProps = new Properties();
        consProps.put("bootstrap.servers", "localhost:9092");
        consProps.put("group.id", "uv-sliding-job");
        consProps.put("isolation.level", "read_committed");

        FlinkKafkaConsumer<Event> source = new FlinkKafkaConsumer<>(
                "click-events",
                new EventDeserializationSchema(),
                consProps
        );
        source.setStartFromLatest();

        // 3. 预聚合：1min Tumbling Window → HLLBucket
        DataStream<HLLBucket> buckets = env
                .addSource(source)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((e, ts) -> e.getEventTime())
                )
                .keyBy(Event::getProductId)
                .window(SlidingEventTimeWindows.of(
                        Time.minutes(1), Time.minutes(1)))
                .allowedLateness(Time.seconds(5))
                .aggregate(new HLLAggregate(), new HLLWindowFunction());

        // 4. 滑动窗口合并
        DataStream<UVResult> uvStream = buckets
                .keyBy(HLLBucket::getProductId)
                .process(new UVSlidingWindowProcessFunction(
                        WINDOW_SIZE_MS, ALLOWED_LATENESS_MS));

        // 5. 写回 Kafka (Exactly-Once)
        Properties prodProps = new Properties();
        prodProps.put("bootstrap.servers", "localhost:9092");

        FlinkKafkaProducer<UVResult> sink = new FlinkKafkaProducer<>(
                "uv-results",
                new UVResultSerializationSchema("uv-results"),
                prodProps,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE
        );
        uvStream.addSink(sink);

        env.execute("SlidingWindowUVJob");
    }
}

