package com.foodordering.repository;

import com.foodordering.model.MenuItem;
import java.sql.SQLException;
import java.util.List;

public interface MenuRepository {
    List<MenuItem> findAvailableItems() throws SQLException;
}
