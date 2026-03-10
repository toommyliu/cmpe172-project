package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.UserRole;

public class Admin extends User {
    public UserRole getRole() {
        return UserRole.Admin;
    }
}
