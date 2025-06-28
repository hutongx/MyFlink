package com.my_flink.window;

// 4. 窗口处理函数
import com.my_flink.model.ProductUVResult;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class ProductUVProcessFunction extends ProcessWindowFunction<Long, ProductUVResult, String, TimeWindow> {

    @Override
    public void process(String productId, Context context, Iterable<Long> elements, Collector<ProductUVResult> out) {
        TimeWindow window = context.window();
        Long uv = elements.iterator().next();

        ProductUVResult result = new ProductUVResult(
                productId,
                uv,
                window.getStart(),
                window.getEnd()
        );

        out.collect(result);
    }
}
