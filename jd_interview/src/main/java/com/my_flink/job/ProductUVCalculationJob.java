package com.my_flink.job;

import com.my_flink.aggregate.HyperLogLogAggregateFunction;
import com.my_flink.model.ProductUVResult;
import com.my_flink.model.UserBehaviorEvent;
import com.my_flink.window.ProductUVProcessFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.time.Duration;

public class ProductUVCalculationJob {

    public static void main(String[] args) throws Exception {

        // 1. 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 配置状态后端和检查点
        env.setStateBackend(new RocksDBStateBackend("hdfs://namenode:port/flink/checkpoints"));
        env.enableCheckpointing(60000); // 1分钟检查点
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
        env.getCheckpointConfig().setCheckpointTimeout(600000); // 10分钟超时

        // 3. 设置并行度 - 根据Kafka分区数和集群资源调整
        env.setParallelism(200);

        // 4. Kafka消费者配置
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "kafka1:9092,kafka2:9092,kafka3:9092");
        kafkaProps.setProperty("group.id", "product-uv-calculation");
        kafkaProps.setProperty("auto.offset.reset", "latest");
        kafkaProps.setProperty("enable.auto.commit", "false");
        // 优化配置
        kafkaProps.setProperty("fetch.min.bytes", "1048576"); // 1MB
        kafkaProps.setProperty("fetch.max.wait.ms", "500");

        // 5. 创建Kafka数据源
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                "user-behavior-topic",
                new SimpleStringSchema(),
                kafkaProps
        );

        // 6. 数据流处理
        DataStream<UserBehaviorEvent> eventStream = env
                .addSource(kafkaConsumer)
                .map(json -> parseUserBehaviorEvent(json)) // 解析JSON
                .filter(event -> event != null && event.getUserId() != null && event.getProductId() != null)
                .assignTimestampsAndWatermarks(
                        new BoundedOutOfOrdernessTimestampExtractor<UserBehaviorEvent>(Time.seconds(30)) {
                            @Override
                            public long extractTimestamp(UserBehaviorEvent event) {
                                return event.getTimestamp();
                            }
                        }
                );

        // 7. UV计算
        DataStream<ProductUVResult> uvResults = eventStream
                .keyBy(UserBehaviorEvent::getProductId) // 按商品ID分组
                .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(1))) // 1小时滑动窗口，每分钟计算一次
                .aggregate(new HyperLogLogAggregateFunction(), new ProductUVProcessFunction());

        // 8. 输出结果到Kafka
        Properties sinkProps = new Properties();
        sinkProps.setProperty("bootstrap.servers", "kafka1:9092,kafka2:9092,kafka3:9092");

        /**
        FlinkKafkaProducer<ProductUVResult> kafkaProducer = new FlinkKafkaProducer<>(
                "product-uv-results",
                (element, timestamp) -> element.toString().getBytes(),
                sinkProps
        );*/
        FlinkKafkaProducer<ProductUVResult> kafkaProducer =
                new FlinkKafkaProducer<>(
                        "product-uv-results",
                        // 需要返回 ProducerRecord，并指定语义
                        (KafkaSerializationSchema<ProductUVResult>) (element, timestamp) ->
                                new ProducerRecord<>(
                                        "product-uv-results",
                                        element.toString().getBytes(StandardCharsets.UTF_8)
                                ),
                        sinkProps,
                        FlinkKafkaProducer.Semantic.AT_LEAST_ONCE
                );

        uvResults.addSink(kafkaProducer);

        // 9. 也可以输出到控制台用于调试
        uvResults.print("UV Results");

        // 10. 执行任务
        env.execute("Product UV Calculation Job");
    }

    // 解析JSON的辅助方法
    private static UserBehaviorEvent parseUserBehaviorEvent(String json) {
        try {
            // 这里使用Jackson或Gson解析JSON
            // 示例实现
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, UserBehaviorEvent.class);
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + json + ", error: " + e.getMessage());
            return null;
        }
    }
}
