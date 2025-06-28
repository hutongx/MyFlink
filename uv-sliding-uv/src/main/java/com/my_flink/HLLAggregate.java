package com.my_flink;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLog;
import org.apache.flink.api.common.functions.AggregateFunction;

public class HLLAggregate implements AggregateFunction<Event, HyperLogLog, HyperLogLog> {
    private static final int P = 14; // 2^14 registers, ~1.6% 标准误差

    @Override
    public HyperLogLog createAccumulator() {
        return new HyperLogLog(P);
    }

    @Override
    public HyperLogLog add(Event value, HyperLogLog acc) {
        acc.offer(value.getUserId());
        return acc;
    }

    @Override
    public HyperLogLog getResult(HyperLogLog acc) {
        return acc;
    }

    @Override
    public HyperLogLog merge(HyperLogLog a, HyperLogLog b) {
        try {
            a.addAll(b);
        } catch (CardinalityMergeException e) {
            throw new RuntimeException(e);
        }
        return a;
    }
}

