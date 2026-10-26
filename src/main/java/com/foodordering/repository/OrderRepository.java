package com.foodordering.repository;

import com.foodordering.model.Customer;
import com.foodordering.model.Order;
import com.foodordering.model.OrderSummary;
import java.util.List;
import java.sql.SQLException;

public interface OrderRepository {
    int placeOrder(Customer customer, Order order) throws SQLException;
    List<OrderSummary> findOrdersByEmail(String email) throws SQLException;
}
