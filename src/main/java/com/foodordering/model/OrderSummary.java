package com.foodordering.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummary {
    private final int id;
    private final LocalDateTime date;
    private final String status;
    private final BigDecimal total;

    public OrderSummary(int id, LocalDateTime date, String status, BigDecimal total) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.total = total;
    }

    public int getId() { return id; }
    public LocalDateTime getDate() { return date; }
    public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
}
