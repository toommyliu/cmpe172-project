package edu.sjsu.cmpe172.salon.enums;

public enum AvailabilitySlotStatus {
    Available(1),
    Booked(2),
    Cancelled(100);

    private final int value;

    AvailabilitySlotStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AvailabilitySlotStatus fromValue(int value) {
        return switch (value) {
            case 1 -> Available;
            case 2 -> Booked;
            case 100 -> Cancelled;
            default -> throw new IllegalArgumentException("Invalid availability slot status value: " + value);
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case Available -> "Available";
            case Booked -> "Booked";
            case Cancelled -> "Cancelled";
        };
    }
}
