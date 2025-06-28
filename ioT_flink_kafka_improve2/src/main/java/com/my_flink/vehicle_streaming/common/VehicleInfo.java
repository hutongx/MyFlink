package com.my_flink.vehicle_streaming.common;

import lombok.Data;
import org.apache.avro.reflect.Nullable;

@Data
public class VehicleInfo {
    private String vehicleId;
    private long timestamp;
    private double speed;
    private int soc;
    @Nullable
    private String event;
    private String model;  // 车辆型号
    private String owner;  // 车辆所有者
}
