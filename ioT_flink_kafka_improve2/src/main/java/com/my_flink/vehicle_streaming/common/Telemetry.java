package com.my_flink.vehicle_streaming.common;

import lombok.Data;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.reflect.Nullable;

@Data
public class Telemetry extends SpecificRecordBase {
    private String vehicleId;
    private long timestamp;
    private double speed;
    private int soc;
    @Nullable private String event;
    private String model;  // 车辆型号
    private String owner;  // 车辆所有者

    // 构造、getter/setter 省略（IDE 生成）
    public static Telemetry of(String vid, long ts, double sp, int soc, String ev) {
        Telemetry t = new Telemetry();
        t.vehicleId = vid;
        t.timestamp = ts;
        t.speed = sp;
        t.soc = soc;
        t.event = ev;
        return t;
    }

    public static final Schema SCHEMA$ = new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"Telemetry\",\"namespace\":\"com.example.common\","
                    + "\"fields\":["
                    + "{\"name\":\"vehicleId\",\"type\":\"string\"},"
                    + "{\"name\":\"timestamp\",\"type\":\"long\"},"
                    + "{\"name\":\"speed\",\"type\":\"double\"},"
                    + "{\"name\":\"soc\",\"type\":\"int\"},"
                    + "{\"name\":\"event\",\"type\":[\"null\",\"string\"],\"default\":null}"
                    + "]}");

    @Override
    public Schema getSchema() { return SCHEMA$; }

    @Override
    public Object get(int field) {
        switch (field) {
            case 0: return vehicleId;
            case 1: return timestamp;
            case 2: return speed;
            case 3: return soc;
            case 4: return event;
            default: throw new AvroRuntimeException("Bad index");
        }
    }

    @Override
    public void put(int field, Object value) {
        switch (field) {
            case 0: vehicleId = value.toString(); break;
            case 1: timestamp = (Long) value; break;
            case 2: speed = (Double) value; break;
            case 3: soc = (Integer) value; break;
            case 4: event = (String) value; break;
            default: throw new AvroRuntimeException("Bad index");
        }
    }
}

