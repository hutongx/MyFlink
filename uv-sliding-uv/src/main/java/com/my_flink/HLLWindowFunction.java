package com.my_flink;

import com.clearspring.analytics.stream.cardinality.HyperLogLog;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class HLLWindowFunction
        extends ProcessWindowFunction<HyperLogLog, HLLBucket, String, TimeWindow> {

    @Override
    public void process(String key,
                        Context ctx,
                        Iterable<HyperLogLog> elements,
                        Collector<HLLBucket> out) throws Exception {
        HyperLogLog acc = elements.iterator().next();
        out.collect(new HLLBucket(key, ctx.window().getEnd(), acc));
    }
}

