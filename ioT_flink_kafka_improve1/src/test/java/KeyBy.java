import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KeyBy {
    public static void main(String[] args) throws Exception {
        // 1. 创建本地执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 为了输出顺序可读，我们把并行度设为1
        env.setParallelism(1);

        // 2. 构造输入：一组 (key, value) 元组
        DataStream<Tuple2<String, Integer>> input = env.fromElements(
                Tuple2.of("A", 1),
                Tuple2.of("B", 2),
                Tuple2.of("A", 3),
                Tuple2.of("B", 4),
                Tuple2.of("C", 5)
        );

        // 3. keyBy：按第一个字段（f0，也就是 key）做 hash 分区，输出 KeyedStream
        KeyedStream<Tuple2<String, Integer>, String> keyed = input
                .keyBy(tuple -> tuple.f0);

        keyed.print("afterKeyBy: ");

        // 4. 在 KeyedStream 上做 sum(1)：对第二个字段（f1，也就是 value）做累加
        DataStream<Tuple2<String, Integer>> summed = keyed
                .sum(1);

        // 5. 打印结果到控制台
        summed.print();

        // 6. 启动作业
        env.execute("KeyBy 测试");
    }
}
