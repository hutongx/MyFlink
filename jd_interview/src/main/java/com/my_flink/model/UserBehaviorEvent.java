package com.my_flink.model;

// 1. 用户行为事件类
public class UserBehaviorEvent {
    private String userId;
    private String productId;
    private long timestamp;
    private String eventType; // view, click, etc.

    public UserBehaviorEvent() {}

    public UserBehaviorEvent(String userId, String productId, long timestamp, String eventType) {
        this.userId = userId;
        this.productId = productId;
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    @Override
    public String toString() {
        return "UserBehaviorEvent{" +
                "userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
