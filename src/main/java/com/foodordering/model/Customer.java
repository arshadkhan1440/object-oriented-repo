package com.foodordering.model;

public class Customer {
    private final String name;
    private final String email;
    private final String phone;
    private final String address;

    public Customer(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone == null ? "" : phone;
        this.address = address;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}
