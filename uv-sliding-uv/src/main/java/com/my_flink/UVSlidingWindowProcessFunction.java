package com.my_flink;

import com.clearspring.analytics.stream.cardinality.HyperLogLog;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Iterator;

public class UVSlidingWindowProcessFunction
        extends KeyedProcessFunction<String, HLLBucket, UVResult> {

    private final long windowSize;
    private final long allowedLate;

    private MapState<Long, HyperLogLog> buckets;

    public UVSlidingWindowProcessFunction(long windowSize, long allowedLate) {
        this.windowSize = windowSize;
        this.allowedLate = allowedLate;
    }

    @Override
    public void open(Configuration parameters) {
        MapStateDescriptor<Long, HyperLogLog> desc =
                new MapStateDescriptor<>(
                        "buckets",
                        Types.LONG,
                        Types.POJO(HyperLogLog.class)
                );

        StateTtlConfig ttl = StateTtlConfig
                .newBuilder(Time.milliseconds(windowSize + allowedLate))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();
        desc.enableTimeToLive(ttl);

        buckets = getRuntimeContext().getMapState(desc);
    }

    @Override
    public void processElement(HLLBucket bucket,
                               Context ctx,
                               Collector<UVResult> out) throws Exception {
        long bucketStart = bucket.getWindowEnd() - Time.minutes(1).toMilliseconds();
        // 写入 MapState
        buckets.put(bucketStart, bucket.getHll());
        // 注册定时器做清理
        ctx.timerService()
                .registerEventTimeTimer(bucketStart + windowSize + allowedLate);

        // 合并当前 1 小时内所有桶
        HyperLogLog merged = new HyperLogLog(14);
        long lowerBound = bucket.getWindowEnd() - windowSize;
        for (Iterator<Long> it = buckets.keys().iterator(); it.hasNext(); ) {
            long start = it.next();
            if (start >= lowerBound) {
                merged.addAll(buckets.get(start));
            }
        }

        // 输出 UVResult
        out.collect(new UVResult(
                ctx.getCurrentKey(),
                bucket.getWindowEnd() - windowSize,
                bucket.getWindowEnd(),
                merged.cardinality()
        ));
    }

    @Override
    public void onTimer(long timestamp,
                        OnTimerContext ctx,
                        Collector<UVResult> out) throws Exception {
        long expiredBucket = timestamp - windowSize - allowedLate;
        buckets.remove(expiredBucket);
    }
}

