package edu.sjsu.cmpe172.starterdemo.enums;

public enum Speciality {
    None(0), // nothing specific
    Dye(1); // Hair dye

    private int value = 0;

    Speciality(int value) {
        this.value = value;
    }

    public String toString() {
        return switch (this.value) {
            case 0 -> "None";
            case 1 -> "Dye";
            default -> "Unknown";
        };
    }
}
