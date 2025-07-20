package com.psu.sweng888.gthenewapp.data;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private String brand;
    private double price;
    private String description;

    public Product() {}

    public Product(String name, String brand, double price, String description) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
} 