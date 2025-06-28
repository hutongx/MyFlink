package com.my_flink.config;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// 6. 性能优化配置类
public class FlinkOptimizationConfig {

    public static void configureOptimizations(StreamExecutionEnvironment env) {

        // 网络缓冲区配置
        env.getConfig().setLatencyTrackingInterval(1000L);

        // 对象重用以减少GC压力
        env.getConfig().enableObjectReuse();

        // 设置合适的并行度
        env.setParallelism(Runtime.getRuntime().availableProcessors() * 4);

        // 内存配置建议
        // taskmanager.memory.process.size: 8GB
        // taskmanager.memory.managed.fraction: 0.4
        // taskmanager.memory.network.fraction: 0.1

        // RocksDB状态后端优化
        // state.backend.rocksdb.block.cache-size: 256MB
        // state.backend.rocksdb.thread.num: 4
        // state.backend.rocksdb.write-buffer-size: 64MB
        // state.backend.rocksdb.max-write-buffer-number: 3
    }
}