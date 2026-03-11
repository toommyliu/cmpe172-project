package edu.sjsu.cmpe172.salon.enums;

public enum Speciality {
    None(0),
    Coloring(1),
    Cutting(2),
    Extensions(3),
    ChemicalTreatments(4),
    Styling(5),
    Barbering(6);

    private int value = 0;

    Speciality(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Speciality fromValue(int value) {
        for (Speciality s : Speciality.values()) {
            if (s.value == value) {
                return s;
            }
        }
        return None;
    }

    public String toString() {
        return switch (this.value) {
            case 0 -> "None";
            case 1 -> "Coloring";
            case 2 -> "Cutting";
            case 3 -> "Extensions";
            case 4 -> "Chemical Treatments";
            case 5 -> "Styling";
            case 6 -> "Barbering";
            default -> "Unknown";
        };
    }
}
