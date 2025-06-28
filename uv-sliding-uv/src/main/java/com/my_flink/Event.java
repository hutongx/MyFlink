package com.my_flink;

public class Event {
    private String userId;
    private String productId;
    private long eventTime;

    public Event() {}

    public Event(String userId, String productId, long eventTime) {
        this.userId = userId;
        this.productId = productId;
        this.eventTime = eventTime;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public long getEventTime() {
        return eventTime;
    }
    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "Event{" +
                "userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
