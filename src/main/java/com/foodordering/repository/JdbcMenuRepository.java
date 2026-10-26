package com.foodordering.repository;

import com.foodordering.config.DatabaseConnection;
import com.foodordering.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcMenuRepository implements MenuRepository {
    @Override
    public List<MenuItem> findAvailableItems() throws SQLException {
        String sql = "SELECT item_id, name, description, category, price "
                + "FROM menu_items WHERE is_available = TRUE ORDER BY category, name";
        List<MenuItem> items = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                items.add(new MenuItem(results.getInt("item_id"), results.getString("name"),
                        results.getString("description"), results.getString("category"),
                        results.getBigDecimal("price")));
            }
        }
        return items;
    }
}
