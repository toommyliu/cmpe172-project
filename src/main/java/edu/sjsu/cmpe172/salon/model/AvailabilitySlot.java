package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;

import java.time.LocalDateTime;

// Availability_slot(id, stylist_user_id, start_datetime, end_datetime, status)

public class AvailabilitySlot {
    private int id;

    private int stylistUserId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private AppointmentStatus status;

    public AvailabilitySlot() {
    }

    public AvailabilitySlot(int id, int stylistUserId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.id = id;
        this.stylistUserId = stylistUserId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}