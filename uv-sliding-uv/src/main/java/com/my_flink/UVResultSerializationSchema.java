package com.my_flink;

import com.alibaba.fastjson.JSON;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;

public class UVResultSerializationSchema
        implements KafkaSerializationSchema<UVResult> {

    private final String topic;

    public UVResultSerializationSchema(String topic) {
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(UVResult element,
                                                    @Nullable Long timestamp) {
        byte[] key = element.getProductId().getBytes(StandardCharsets.UTF_8);
        byte[] value = JSON.toJSONString(element).getBytes(StandardCharsets.UTF_8);
        return new ProducerRecord<>(topic, null, timestamp, key, value);
    }
}
