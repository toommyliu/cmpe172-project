package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;

// Appointment(id, customer_user_id, stylist_userid, service_id, availabilty_slot_id,
// status, date_time)

public class Appointment {
    private int id;
    private int customerUserId;
    private int stylistUserId;
    private int serviceId;
    private int availabilitySlotId;
    private AppointmentStatus status = AppointmentStatus.Booked;

    public Appointment() {
    }

    public Appointment(int id, int customerUserId, int stylistUserId, int serviceId, int availabilitySlotId, AppointmentStatus status) {
        this.id = id;
        this.customerUserId = customerUserId;
        this.stylistUserId = stylistUserId;
        this.serviceId = serviceId;
        this.availabilitySlotId = availabilitySlotId;
        this.status = status;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
