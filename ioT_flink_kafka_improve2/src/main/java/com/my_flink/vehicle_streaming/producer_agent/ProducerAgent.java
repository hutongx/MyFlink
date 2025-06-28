package com.my_flink.vehicle_streaming.producer_agent;

import com.my_flink.vehicle_streaming.common.Telemetry;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.*;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ProducerAgent {
    public static void main(String[] args) throws Exception {
        // 加载配置（省略 YAML 解析）
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "1048576");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(props);
        SpecificDatumWriter<Telemetry> writer = new SpecificDatumWriter<>(Telemetry.class);
        Random rand = new Random();

        long intervalNanos = TimeUnit.SECONDS.toNanos(1) / 200000; // 20万条/s
        while (true) {
            long batchStart = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                Telemetry t = Telemetry.of(
                        "vehicle-" + rand.nextInt(200000),
                        Instant.now().toEpochMilli(),
                        rand.nextDouble() * 120,
                        rand.nextInt(100),
                        null
                );
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                writer.write(t, encoder);
                encoder.flush();
                producer.send(new ProducerRecord<>("vehicle-telemetry-raw", t.getVehicleId(), out.toByteArray()));
                Thread.sleep(0, (int)(intervalNanos));
            }
            long elapsed = System.nanoTime() - batchStart;
            // 省略动态流控
        }
    }
}

