package edu.sjsu.cmpe172.salon.enums;

public enum AppointmentStatus {
    Unknown(0),
    Booked(1),
    Complete(2),
    Canceled(3);

    private int value;

    AppointmentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return switch (this.value) {
            case 1 -> "Booked";
            case 2 -> "Completed";
            case 3 -> "Canceled";
            default -> "Unknown";
        };
    }

    public static AppointmentStatus fromValue(int value) {
        return switch (value) {
            case 1 -> Booked;
            case 2 -> Complete;
            case 3 -> Canceled;
            default -> Unknown;
        };
    }
}
