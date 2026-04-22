package edu.sjsu.cmpe172.salon.dto;

public class AppointmentConfirmationDispatchResponse {
    private int appointmentId;
    private String externalNotificationId;
    private String deliveryStatus;
    private String provider;

    public AppointmentConfirmationDispatchResponse() {
    }

    public AppointmentConfirmationDispatchResponse(int appointmentId,
                                                   String externalNotificationId,
                                                   String deliveryStatus,
                                                   String provider) {
        this.appointmentId = appointmentId;
        this.externalNotificationId = externalNotificationId;
        this.deliveryStatus = deliveryStatus;
        this.provider = provider;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getExternalNotificationId() {
        return externalNotificationId;
    }

    public void setExternalNotificationId(String externalNotificationId) {
        this.externalNotificationId = externalNotificationId;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
