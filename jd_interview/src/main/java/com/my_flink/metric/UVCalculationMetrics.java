package com.my_flink.metric;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.runtime.metrics.DescriptiveStatisticsHistogram;

// 7. 监控和告警
public class UVCalculationMetrics {

    private Counter processedEvents;
    private Gauge currentUVCount;
    private Histogram processingLatency;

    public UVCalculationMetrics(RuntimeContext runtimeContext) {
        this.processedEvents = runtimeContext
                .getMetricGroup()
                .addGroup("uv-calculation")
                .counter("processed-events");

        this.currentUVCount = runtimeContext
                .getMetricGroup()
                .addGroup("uv-calculation")
                .gauge("current-uv-count", () -> getCurrentUVCount());

        this.processingLatency = runtimeContext
                .getMetricGroup()
                .addGroup("uv-calculation")
                .histogram("processing-latency", new DescriptiveStatisticsHistogram(1000));
    }

    private Long getCurrentUVCount() {
        // 返回当前UV统计值
        return 0L;
    }
}
