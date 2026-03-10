package edu.sjsu.cmpe172.starterdemo.model;

import edu.sjsu.cmpe172.starterdemo.enums.UserRole;

public class Admin extends User {
    public UserRole getRole() {
        return UserRole.Admin;
    }
}
