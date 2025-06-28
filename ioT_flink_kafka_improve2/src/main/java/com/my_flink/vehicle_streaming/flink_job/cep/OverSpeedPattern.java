package com.my_flink.vehicle_streaming.flink_job.cep;

import com.my_flink.vehicle_streaming.common.Telemetry;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.cep.*;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

// 简单 CEP：2 次以上超速（> threshold）视为异常
public class OverSpeedPattern extends ProcessFunction<Telemetry, Telemetry> {
    private final double threshold;
    public OverSpeedPattern(double threshold) { this.threshold = threshold; }

    @Override
    public void processElement(Telemetry value, ProcessFunction<Telemetry, Telemetry>.Context ctx, Collector<Telemetry> out) throws Exception {
        if (value.getSpeed() > threshold) {
            // 触发超速事件
            value.setEvent("over_speed");
            out.collect(value);
        } else {
            // 非超速，清除事件
            value.setEvent(null);
        }
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        super.open(openContext);
    }
    // 省略：定义 PatternStream、select 输出告警
}

