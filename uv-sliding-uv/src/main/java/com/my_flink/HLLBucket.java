package com.my_flink;

import com.clearspring.analytics.stream.cardinality.HyperLogLog;

public class HLLBucket {
    private String productId;
    private long windowEnd;
    private HyperLogLog hll;

    public HLLBucket() {}

    public HLLBucket(String productId, long windowEnd, HyperLogLog hll) {
        this.productId = productId;
        this.windowEnd = windowEnd;
        this.hll = hll;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public long getWindowEnd() {
        return windowEnd;
    }
    public void setWindowEnd(long windowEnd) {
        this.windowEnd = windowEnd;
    }
    public HyperLogLog getHll() {
        return hll;
    }
    public void setHll(HyperLogLog hll) {
        this.hll = hll;
    }
}

