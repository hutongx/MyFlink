package com.my_flink.vehicle_streaming.flink_job.aggregate;

import com.my_flink.vehicle_streaming.common.Telemetry;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class AvgSpeedAgg
        implements AggregateFunction<
            Telemetry,                     // 输入类型
            Tuple2<Double, Long>,         // 累加器类型
            AvgSpeedAgg.Output            // 输出类型（静态内部类，全限定名）
            > {

    @Override
    public Tuple2<Double, Long> createAccumulator() {
        return Tuple2.of(0.0, 0L);
    }

    @Override
    public Tuple2<Double, Long> add(Telemetry value,
                                    Tuple2<Double, Long> acc) {
        return Tuple2.of(acc.f0 + value.getSpeed(),
                acc.f1 + 1);
    }

    @Override
    public Output getResult(Tuple2<Double, Long> acc) {
        double avg = (acc.f1 == 0 ? 0.0 : acc.f0 / acc.f1);
        return new Output(avg, acc.f1);
    }

    @Override
    public Tuple2<Double, Long> merge(Tuple2<Double, Long> a,
                                      Tuple2<Double, Long> b) {
        return Tuple2.of(a.f0 + b.f0,
                a.f1 + b.f1);
    }

    /** 静态内部类，做为聚合函数的输出类型 */
    public static class Output {
        private double avgSpeed;
        private long count;

        /** Flink POJO 要求的无参构造器 */
        public Output() {}

        public Output(double avgSpeed, long count) {
            this.avgSpeed = avgSpeed;
            this.count = count;
        }

        public double getAvgSpeed() { return avgSpeed; }
        public void setAvgSpeed(double avgSpeed) {
            this.avgSpeed = avgSpeed;
        }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        @Override
        public String toString() {
            return "Output{" +
                    "avgSpeed=" + avgSpeed +
                    ", count=" + count +
                    '}';
        }
    }
}


