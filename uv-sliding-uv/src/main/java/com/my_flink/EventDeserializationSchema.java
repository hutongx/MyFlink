package com.my_flink;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EventDeserializationSchema implements DeserializationSchema<Event> {

    @Override
    public Event deserialize(byte[] message) throws IOException {
        String json = new String(message, StandardCharsets.UTF_8);
        return JSON.parseObject(json, Event.class);
    }

    @Override
    public boolean isEndOfStream(Event nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Event> getProducedType() {
        return TypeInformation.of(Event.class);
    }
}
