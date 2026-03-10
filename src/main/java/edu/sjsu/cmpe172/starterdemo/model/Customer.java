package edu.sjsu.cmpe172.starterdemo.model;

import edu.sjsu.cmpe172.starterdemo.enums.UserRole;

public class Customer extends User {
    private String phoneNumber;

    public Customer()  {
        super();
    }

    public Customer(String phoneNumber) {
        super();
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return UserRole.Customer;
    }
}
