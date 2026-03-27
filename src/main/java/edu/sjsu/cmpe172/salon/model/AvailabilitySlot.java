package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;

import java.time.LocalDateTime;

// Availability_slot(id, stylist_user_id, start_datetime, end_datetime, status)

public class AvailabilitySlot {
    private int id;

    private int stylistUserId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private AvailabilitySlotStatus status;

    private int version;

    public AvailabilitySlot() {
    }

    public AvailabilitySlot(int id, int stylistUserId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.id = id;
        this.stylistUserId = stylistUserId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = AvailabilitySlotStatus.Available;
        this.version = 0;
    }

    public AvailabilitySlot(int id, int stylistUserId, LocalDateTime startDateTime,
                            LocalDateTime endDateTime, AvailabilitySlotStatus status) {
        this.id = id;
        this.stylistUserId = stylistUserId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
        this.version = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStylistUserId() {
        return stylistUserId;
    }

    public void setStylistUserId(int stylistUserId) {
        this.stylistUserId = stylistUserId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public AvailabilitySlotStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilitySlotStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
