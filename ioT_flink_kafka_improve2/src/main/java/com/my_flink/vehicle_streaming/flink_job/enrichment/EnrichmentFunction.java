package com.my_flink.vehicle_streaming.flink_job.enrichment;

import com.my_flink.vehicle_streaming.common.Telemetry;
import com.my_flink.vehicle_streaming.common.VehicleInfo;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.util.Collector;

// 维度 CDC 流在 Job 启动时通过 FlinkKafkaConsumer 加入，broadcast 处理
public class EnrichmentFunction extends BroadcastProcessFunction<
        Telemetry, VehicleInfo, Telemetry> {

    private final MapStateDescriptor<String, VehicleInfo> dimStateDescriptor =
            new MapStateDescriptor<>("dimState", String.class, VehicleInfo.class);

    @Override
    public void processElement(Telemetry value, ReadOnlyContext ctx, Collector<Telemetry> out) throws Exception {
        VehicleInfo info = ctx.getBroadcastState(dimStateDescriptor).get(value.getVehicleId());
        if (info != null) {
            value.setModel(info.getModel());
            value.setOwner(info.getOwner());
        }
        out.collect(value);
    }

    @Override
    public void processBroadcastElement(VehicleInfo value, Context ctx, Collector<Telemetry> out) throws Exception {
        ctx.getBroadcastState(dimStateDescriptor).put(value.getVehicleId(), value);
    }
}

