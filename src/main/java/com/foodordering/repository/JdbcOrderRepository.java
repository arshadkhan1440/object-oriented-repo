package com.foodordering.repository;

import com.foodordering.config.DatabaseConnection;
import com.foodordering.model.Customer;
import com.foodordering.model.Order;
import com.foodordering.model.OrderSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderRepository implements OrderRepository {
    @Override
    public int placeOrder(Customer customer, Order order) throws SQLException {
        String customerSql = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE customer_id = LAST_INSERT_ID(customer_id), "
                + "name = VALUES(name), phone = VALUES(phone), address = VALUES(address)";
        String orderSql = "INSERT INTO orders (customer_id, order_status, total_amount, delivery_address) "
                + "VALUES (?, 'PLACED', ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                int customerId;
                try (PreparedStatement statement = connection.prepareStatement(customerSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, customer.getName());
                    statement.setString(2, customer.getEmail());
                    statement.setString(3, customer.getPhone().isBlank() ? null : customer.getPhone());
                    statement.setString(4, customer.getAddress());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Could not get the customer ID.");
                        customerId = keys.getInt(1);
                    }
                }

                java.math.BigDecimal total = order.getTotal();
                int orderId;
                try (PreparedStatement statement = connection.prepareStatement(orderSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, customerId);
                    statement.setBigDecimal(2, total);
                    statement.setString(3, customer.getAddress());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Could not get the new order ID.");
                        orderId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(itemSql)) {
                    for (com.foodordering.model.CartItem item : order.getItems()) {
                        statement.setInt(1, orderId);
                        statement.setInt(2, item.getMenuItem().getId());
                        statement.setInt(3, item.getQuantity());
                        statement.setBigDecimal(4, item.getMenuItem().getPrice());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<OrderSummary> findOrdersByEmail(String email) throws SQLException {
        String sql = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount "
                + "FROM orders o JOIN customers c ON c.customer_id = o.customer_id "
                + "WHERE c.email = ? ORDER BY o.order_date DESC";
        List<OrderSummary> orders = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Timestamp timestamp = results.getTimestamp("order_date");
                    orders.add(new OrderSummary(results.getInt("order_id"),
                            timestamp == null ? null : timestamp.toLocalDateTime(),
                            results.getString("order_status"), results.getBigDecimal("total_amount")));
                }
            }
        }
        return orders;
    }
}
