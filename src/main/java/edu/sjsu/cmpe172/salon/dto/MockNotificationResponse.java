package edu.sjsu.cmpe172.salon.dto;

import java.time.LocalDateTime;

public class MockNotificationResponse {
    private String notificationId;
    private String status;
    private String provider;
    private LocalDateTime acceptedAt;
    private String message;

    public MockNotificationResponse() {
    }

    public MockNotificationResponse(String notificationId, String status, String provider, LocalDateTime acceptedAt, String message) {
        this.notificationId = notificationId;
        this.status = status;
        this.provider = provider;
        this.acceptedAt = acceptedAt;
        this.message = message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
