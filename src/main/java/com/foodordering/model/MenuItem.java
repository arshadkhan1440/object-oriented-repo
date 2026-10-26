package com.foodordering.model;

import java.math.BigDecimal;

public class MenuItem {
    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final BigDecimal price;

    public MenuItem(int id, String name, String description, String category, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description == null ? "" : description;
        this.category = category == null ? "" : category;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
}
