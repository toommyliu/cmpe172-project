package edu.sjsu.cmpe172.salon.model;

// Service(id, name, description, price)

public class Service {
    private int id;

    private String name;

    private String description;

    private double price;

    public Service() {
    }

    public Service(int id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
