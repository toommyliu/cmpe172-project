package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.UserRole;

public abstract class User {
    private int id;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String password; // hashed

    public User() {}
    public User(int id, String firstName, String lastName, String emailAddress, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public abstract UserRole getRole();

    public boolean isCustomer() {
        return getRole() == UserRole.Customer;
    }

    public boolean isStylist() {
        return getRole() == UserRole.Stylist;
    }

    public boolean isAdmin() {
        return getRole() == UserRole.Admin;
    }
}
