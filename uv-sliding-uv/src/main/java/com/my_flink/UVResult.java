package com.my_flink;

public class UVResult {
    private String productId;
    private long windowStart;
    private long windowEnd;
    private long uvCount;

    public UVResult() {}

    public UVResult(String productId, long windowStart, long windowEnd, long uvCount) {
        this.productId = productId;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.uvCount = uvCount;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public long getWindowStart() {
        return windowStart;
    }
    public void setWindowStart(long windowStart) {
        this.windowStart = windowStart;
    }
    public long getWindowEnd() {
        return windowEnd;
    }
    public void setWindowEnd(long windowEnd) {
        this.windowEnd = windowEnd;
    }
    public long getUvCount() {
        return uvCount;
    }
    public void setUvCount(long uvCount) {
        this.uvCount = uvCount;
    }

    @Override
    public String toString() {
        return "UVResult{" +
                "productId='" + productId + '\'' +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", uvCount=" + uvCount +
                '}';
    }
}

