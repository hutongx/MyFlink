package com.my_flink.model;

// 2. UV结果类
public class ProductUVResult {
    private String productId;
    private long uv;
    private long windowStart;
    private long windowEnd;

    public ProductUVResult() {}

    public ProductUVResult(String productId, long uv, long windowStart, long windowEnd) {
        this.productId = productId;
        this.uv = uv;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public long getUv() { return uv; }
    public void setUv(long uv) { this.uv = uv; }

    public long getWindowStart() { return windowStart; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }

    public long getWindowEnd() { return windowEnd; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }

    @Override
    public String toString() {
        return "ProductUVResult{" +
                "productId='" + productId + '\'' +
                ", uv=" + uv +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                '}';
    }
}
