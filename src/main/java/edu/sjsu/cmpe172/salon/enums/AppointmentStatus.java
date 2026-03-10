package edu.sjsu.cmpe172.salon.enums;

public enum AppointmentStatus {
    Unknown(0),
    Booked(1),
    Pending(2),
    Complete(3),
    Canceled(100);

    private int value;

    AppointmentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return switch (this.value) {
            case 1 -> "pending";
            case 2 -> "in progress";
            default -> "unknown";
        };
    }

    public static AppointmentStatus fromValue(int value) {
        return switch (value) {
            case 1 -> Booked;
            case 2 -> Pending;
            default -> Unknown;
        };
    }
}
