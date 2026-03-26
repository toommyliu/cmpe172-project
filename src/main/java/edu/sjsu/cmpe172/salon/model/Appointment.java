package edu.sjsu.cmpe172.salon.model;

import java.time.LocalDateTime;

// Appointment(id, customer_user_id, stylist_userid, service_id, availabilty_slot_id,
// status, date_time)

public class Appointment {
    private int id;
    private int customerUserId;
    private int stylistUserId;
    private int serviceId;
    private int availabilitySlotId;

    private String serviceName;

    private String customerName;

    private String stylistName;

    private LocalDateTime slotStartDateTime;

    private LocalDateTime slotEndDateTime;

    public Appointment() {
    }

    public Appointment(int id, int customerUserId, int stylistUserId, int serviceId, int availabilitySlotId) {
        this.id = id;
        this.customerUserId = customerUserId;
        this.stylistUserId = stylistUserId;
        this.serviceId = serviceId;
        this.availabilitySlotId = availabilitySlotId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerUserId() {
        return customerUserId;
    }

    public void setCustomerUserId(int customerUserId) {
        this.customerUserId = customerUserId;
    }

    public int getStylistUserId() {
        return stylistUserId;
    }

    public void setStylistUserId(int stylistUserId) {
        this.stylistUserId = stylistUserId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getAvailabilitySlotId() {
        return availabilitySlotId;
    }

    public void setAvailabilitySlotId(int availabilitySlotId) {
        this.availabilitySlotId = availabilitySlotId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public LocalDateTime getSlotStartDateTime() {
        return slotStartDateTime;
    }

    public void setSlotStartDateTime(LocalDateTime slotStartDateTime) {
        this.slotStartDateTime = slotStartDateTime;
    }

    public LocalDateTime getSlotEndDateTime() {
        return slotEndDateTime;
    }

    public void setSlotEndDateTime(LocalDateTime slotEndDateTime) {
        this.slotEndDateTime = slotEndDateTime;
    }
}
