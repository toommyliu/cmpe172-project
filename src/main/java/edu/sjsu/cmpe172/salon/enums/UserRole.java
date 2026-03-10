package edu.sjsu.cmpe172.salon.enums;

public enum UserRole {
    Customer(1),
    Stylist(2),
    Admin(100);

    UserRole(int value) {
        this.value = value;
    }

    private final int value;

    public String toString() {
        return switch (this.value) {
            case 1 -> "Customer";
            case 2 -> "Stylist";
            case 100 -> "Admin";
            default -> {
                throw new IllegalArgumentException("Invalid value");
            }
        };
    }

    public static UserRole fromValue(int value) {
        return switch (value) {
            case 1 -> Customer;
            case 2 -> Stylist;
            case 100 -> Admin;
            default -> {
                throw new IllegalArgumentException("Invalid value");
            }
        };
    }
}
