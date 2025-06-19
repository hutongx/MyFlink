package com.myflink.agg;

import org.apache.flink.api.common.functions.AggregateFunction;
import com.myflink.model.CarData;

/**
 * AverageSpeedAggregator可以实现一个Flink的AggregateFunction接口，用于计算每个车辆在指定时间窗口内的平均速度。原理是通过累积速度和计数来计算平均值。  以下是实现的步骤：
 * 定义一个累加器类，用于存储累积的速度和计数。
 * 实现AggregateFunction接口的三个主要方法：
 * createAccumulator()：初始化累加器。
 * add()：将新的速度值添加到累加器中。
 * getResult()：从累加器中计算平均速度。
 * merge()：合并两个累加器（在并行计算中可能需要）。
 * 代码如下.
 * -----------------------------------------------------------------------------------------------------------------------------
 * 原理说明：
 * 累加器：Accumulator存储累积的速度和计数。
 * 数据添加：每次接收到新的CarData对象时，将其速度值添加到累加器中。
 * 结果计算：通过累加器中的总速度和计数计算平均速度。
 * 合并累加器：在分布式计算中，多个累加器可能需要合并。
 * 将此类添加到项目中后，可以直接在CarDataProcessor中使用它来计算平均速度。
 */
public class AverageSpeedAggregator implements AggregateFunction<CarData, AverageSpeedAggregator.Accumulator, Double> {

    // 定义累加器类
    public static class Accumulator {
        private double totalSpeed = 0.0;
        private long count = 0;

        public void add(double speed) {
            totalSpeed += speed;
            count++;
        }

        public double getAverage() {
            return count == 0 ? 0.0 : totalSpeed / count;
        }
    }

    @Override
    public Accumulator createAccumulator() {
        return new Accumulator();
    }

    @Override
    public Accumulator add(CarData carData, Accumulator accumulator) {
        accumulator.add(carData.getSpeed());
        return accumulator;
    }

    @Override
    public Double getResult(Accumulator accumulator) {
        return accumulator.getAverage();
    }

    @Override
    public Accumulator merge(Accumulator a, Accumulator b) {
        a.totalSpeed += b.totalSpeed;
        a.count += b.count;
        return a;
    }
}