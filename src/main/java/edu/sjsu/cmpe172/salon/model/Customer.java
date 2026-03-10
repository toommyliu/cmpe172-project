package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.UserRole;

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
