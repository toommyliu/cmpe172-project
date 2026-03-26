package edu.sjsu.cmpe172.salon.model;

import edu.sjsu.cmpe172.salon.enums.UserRole;

public class Stylist extends User {
    private int serviceId;

    private String serviceName;

    public Stylist() {
        super();
    }

    public Stylist(int serviceId) {
        super();
        this.serviceId = serviceId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public UserRole getRole() {
        return UserRole.Stylist;
    }
}
