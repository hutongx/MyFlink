package com.my_flink.aggregate;

// 3. HyperLogLog状态聚合函数
import com.my_flink.model.UserBehaviorEvent;
import org.apache.flink.api.common.functions.AggregateFunction;
import net.agkn.hll.HLL;

public class HyperLogLogAggregateFunction implements AggregateFunction<UserBehaviorEvent, HLL, Long> {

    @Override
    public HLL createAccumulator() {
        // 创建HyperLogLog，log2m=14表示使用16384个桶，标准差约为1.04/sqrt(16384) ≈ 0.8%
        return new HLL(14, 5);
    }

    @Override
    public HLL add(UserBehaviorEvent event, HLL hll) {
        // 将用户ID添加到HyperLogLog中
        hll.addRaw(event.getUserId().hashCode());
        return hll;
    }

    @Override
    public Long getResult(HLL hll) {
        // 返回基数估算结果
        return hll.cardinality();
    }

    @Override
    public HLL merge(HLL hll1, HLL hll2) {
        // 合并两个HyperLogLog
        hll1.union(hll2);
        return hll1;
    }
}
