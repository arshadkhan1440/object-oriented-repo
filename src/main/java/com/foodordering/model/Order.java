package com.foodordering.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final List<CartItem> items;

    public Order(List<CartItem> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public List<CartItem> getItems() { return items; }

    public BigDecimal getTotal() {
        return items.stream().map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
