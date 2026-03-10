package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.Speciality;
import edu.sjsu.cmpe172.salon.enums.UserRole;

import java.util.List;

public class Stylist extends User {
    private Speciality speciality;

    private List<Service> services;

    public Stylist() {
        super();
    }
    public Stylist(Speciality speciality) {
        super();
        this.speciality = speciality;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public UserRole getRole() {
        return UserRole.Stylist;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
