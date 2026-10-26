# Repository interfaces and MySQL implementations

Repositories isolate SQL/database work from the rest of the app. Interfaces describe the operations; JDBC classes perform them. Line numbers refer to files under `src/main/java/com/foodordering/repository`.

## `MenuRepository.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.repository;` | Places database access contracts and implementations together. |
| 3 | `import com.foodordering.model.MenuItem;` | Makes the returned menu model available by its short name. |
| 4 | `import java.sql.SQLException;` | Imports the JDBC error type the database operation can report. |
| 5 | `import java.util.List;` | Imports the collection type used for multiple menu rows. |
| 7 | `public interface MenuRepository {` | Declares a contract for menu data access without specifying how data is retrieved. |
| 8 | `List<MenuItem> findAvailableItems() throws SQLException;` | Requires implementations to return available menu items and declare database failures. The method has no body because it is an interface contract. |
| 9 | `}` | Closes the interface. |

## `OrderRepository.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.repository;` | Places the order data contract in the repository package. |
| 3 | `import com.foodordering.model.Customer;` | Imports the customer data passed when placing an order. |
| 4 | `import com.foodordering.model.Order;` | Imports the order and its food lines. |
| 5 | `import com.foodordering.model.OrderSummary;` | Imports the model returned for each history row. |
| 6 | `import java.util.List;` | Imports the collection type used for multiple history records. |
| 7 | `import java.sql.SQLException;` | Imports the JDBC error type declared by repository operations. |
| 9 | `public interface OrderRepository {` | Declares operations that any order-storage implementation must provide. |
| 10 | `int placeOrder(Customer customer, Order order) throws SQLException;` | Defines the operation for saving a customer and order; it returns the generated order ID. |
| 11 | `List<OrderSummary> findOrdersByEmail(String email) throws SQLException;` | Defines the operation for retrieving a customer's orders by email. |
| 12 | `}` | Closes the interface. |

## `JdbcMenuRepository.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.repository;` | Places the MySQL menu implementation with the repository contracts. |
| 3 | `import com.foodordering.config.DatabaseConnection;` | Imports the shared helper that opens MySQL connections. |
| 4 | `import com.foodordering.model.MenuItem;` | Imports the model created for each row. |
| 6 | `import java.sql.Connection;` | Imports the live JDBC connection type. |
| 7 | `import java.sql.PreparedStatement;` | Imports parameterized SQL statements. |
| 8 | `import java.sql.ResultSet;` | Imports the rows returned by a SELECT query. |
| 9 | `import java.sql.SQLException;` | Imports SQL errors reported to callers. |
| 10 | `import java.util.ArrayList;` | Imports a list implementation to collect query results. |
| 11 | `import java.util.List;` | Imports the collection interface used by the method signature. |
| 13 | `public class JdbcMenuRepository implements MenuRepository {` | Declares the MySQL/JDBC implementation and promises it follows the menu interface. |
| 14 | `@Override` | Tells the compiler this method implements a method declared by the interface. |
| 15 | `public List<MenuItem> findAvailableItems() throws SQLException {` | Implements the operation that loads available foods; it lets SQL errors propagate to the caller. |
| 16 | `String sql = "SELECT item_id, name, description, category, price "` | Starts a query selecting only the menu columns needed by the application. |
| 17 | `+ "FROM menu_items WHERE is_available = TRUE ORDER BY category, name";` | Adds the table/filter/order portion of the SQL: only available foods are returned in category/name order. |
| 18 | `List<MenuItem> items = new ArrayList<>();` | Creates an empty list that will hold converted database rows. |
| 19 | `try (Connection connection = DatabaseConnection.open();` | Opens a connection and starts try-with-resources so Java closes it automatically. |
| 20 | `PreparedStatement statement = connection.prepareStatement(sql);` | Prepares the SELECT statement on that connection. |
| 21 | `ResultSet results = statement.executeQuery()) {` | Runs the query and opens the returned rows; the connection, statement, and results will all be closed at the end. |
| 22 | `while (results.next()) {` | Moves through query rows one at a time until there are no more. |
| 23 | `items.add(new MenuItem(results.getInt("item_id"), results.getString("name"),` | Reads the item ID and name from this row and begins constructing a Java menu object. |
| 24 | `results.getString("description"), results.getString("category"),` | Reads this row's description and category. |
| 25 | `results.getBigDecimal("price")));` | Reads the exact price and completes/adds the `MenuItem` object. |
| 26 | `}` | Closes the row-reading loop. |
| 27 | `}` | Closes the try-with-resources block, which closes JDBC resources. |
| 28 | `return items;` | Returns all converted menu rows to the caller. |
| 29 | `}` | Closes the method. |
| 30 | `}` | Closes the repository class. |

## `JdbcOrderRepository.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.repository;` | Places the JDBC order implementation in the repository package. |
| 3 | `import com.foodordering.config.DatabaseConnection;` | Imports the shared MySQL connection helper. |
| 4 | `import com.foodordering.model.Customer;` | Imports the customer details to save. |
| 5 | `import com.foodordering.model.Order;` | Imports the order and its cart lines. |
| 6 | `import com.foodordering.model.OrderSummary;` | Imports the value type used for order-history results. |
| 8 | `import java.sql.Connection;` | Imports the JDBC connection type. |
| 9 | `import java.sql.PreparedStatement;` | Imports parameterized SQL statements. |
| 10 | `import java.sql.ResultSet;` | Imports query results and generated-key results. |
| 11 | `import java.sql.SQLException;` | Imports JDBC errors that methods report to callers. |
| 12 | `import java.sql.Statement;` | Imports the constant used to request generated database keys. |
| 13 | `import java.sql.Timestamp;` | Imports the JDBC date/time type returned for an order timestamp. |
| 14 | `import java.util.ArrayList;` | Imports a list implementation used to collect history records. |
| 15 | `import java.util.List;` | Imports the list interface used by the repository methods. |
| 17 | `public class JdbcOrderRepository implements OrderRepository {` | Declares the MySQL implementation of the order repository contract. |
| 18 | `@Override` | Asks the compiler to verify this method implements the interface operation. |
| 19 | `public int placeOrder(Customer customer, Order order) throws SQLException {` | Starts the method that saves a customer and order, returning the new order ID if successful. |
| 20 | `String customerSql = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?) "` | Begins a parameterized insert for customer details. Each `?` is filled safely by JDBC. |
| 21 | `+ "ON DUPLICATE KEY UPDATE customer_id = LAST_INSERT_ID(customer_id), "` | If the unique email already exists, updates that row and makes MySQL return the existing customer ID as a generated key. |
| 22 | `+ "name = VALUES(name), phone = VALUES(phone), address = VALUES(address)";` | Updates the existing customer's current name, phone, and address with the submitted values. |
| 23 | `String orderSql = "INSERT INTO orders (customer_id, order_status, total_amount, delivery_address) "` | Begins the SQL statement for a new order record. |
| 24 | `+ "VALUES (?, 'PLACED', ?, ?)";` | Defines the values: customer ID, initial status `PLACED`, total, and delivery address. |
| 25 | `String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";` | Defines the statement for saving each food line linked to the new order. |
| 27 | `try (Connection connection = DatabaseConnection.open()) {` | Opens the connection in a resource block so it is closed even if saving fails. |
| 28 | `connection.setAutoCommit(false);` | Turns off automatic commits so all customer/order/item inserts can succeed or roll back together. |
| 29 | `try {` | Begins the transaction work block. Errors in it trigger rollback below. |
| 30 | `int customerId;` | Declares a variable to hold the existing or newly generated customer ID. |
| 31 | `try (PreparedStatement statement = connection.prepareStatement(customerSql,` | Prepares the customer insert and requests its generated key. |
| 32 | `Statement.RETURN_GENERATED_KEYS)) {` | Completes the prepared-statement setup with the request for auto-generated IDs. |
| 33 | `statement.setString(1, customer.getName());` | Places the customer's name in the first `?` parameter. |
| 34 | `statement.setString(2, customer.getEmail());` | Places the email in the second parameter. |
| 35 | `statement.setString(3, customer.getPhone().isBlank() ? null : customer.getPhone());` | Saves an empty optional phone as SQL `NULL`, otherwise saves its text. |
| 36 | `statement.setString(4, customer.getAddress());` | Places the address in the fourth parameter. |
| 37 | `statement.executeUpdate();` | Executes the insert or duplicate-email update. |
| 38 | `try (ResultSet keys = statement.getGeneratedKeys()) {` | Opens the generated-key results so the customer ID can be retrieved. |
| 39 | `if (!keys.next()) throw new SQLException("Could not get the customer ID.");` | Checks that a key was returned and reports a database error if not. |
| 40 | `customerId = keys.getInt(1);` | Reads the customer ID from the first generated-key column. |
| 41 | `}` | Closes the generated-key result set. |
| 42 | `}` | Closes the customer prepared statement. |
| 44 | `java.math.BigDecimal total = order.getTotal();` | Gets the exact total calculated from the order's item subtotals. The fully qualified name avoids another import. |
| 45 | `int orderId;` | Declares a variable for the generated order ID. |
| 46 | `try (PreparedStatement statement = connection.prepareStatement(orderSql,` | Prepares the order insert and requests its generated key. |
| 47 | `Statement.RETURN_GENERATED_KEYS)) {` | Completes the prepared-statement setup with generated-key retrieval enabled. |
| 48 | `statement.setInt(1, customerId);` | Sets the customer foreign key on the order. |
| 49 | `statement.setBigDecimal(2, total);` | Sets the calculated order total using exact decimal data. |
| 50 | `statement.setString(3, customer.getAddress());` | Sets the delivery address stored on the order record. |
| 51 | `statement.executeUpdate();` | Inserts the order row. |
| 52 | `try (ResultSet keys = statement.getGeneratedKeys()) {` | Opens the generated order ID results. |
| 53 | `if (!keys.next()) throw new SQLException("Could not get the new order ID.");` | Ensures MySQL returned an order ID or aborts the transaction with an error. |
| 54 | `orderId = keys.getInt(1);` | Reads and saves the new order ID. |
| 55 | `}` | Closes the order generated-key result set. |
| 56 | `}` | Closes the order prepared statement. |
| 58 | `try (PreparedStatement statement = connection.prepareStatement(itemSql)) {` | Prepares one reusable insert statement for the order's item rows. |
| 59 | `for (com.foodordering.model.CartItem item : order.getItems()) {` | Loops over each food/quantity pair in the order. The fully qualified type avoids an import. |
| 60 | `statement.setInt(1, orderId);` | Links this order-item row to the saved order. |
| 61 | `statement.setInt(2, item.getMenuItem().getId());` | Sets the foreign key to the selected menu item. |
| 62 | `statement.setInt(3, item.getQuantity());` | Sets how many of that food were ordered. |
| 63 | `statement.setBigDecimal(4, item.getMenuItem().getPrice());` | Saves the unit price at purchase time so future menu price changes do not alter old orders. |
| 64 | `statement.addBatch();` | Queues this row for batch insertion, reducing repeated database calls. |
| 65 | `}` | Closes the loop over items. |
| 66 | `statement.executeBatch();` | Inserts all queued order-item rows. |
| 67 | `}` | Closes the item prepared statement. |
| 68 | `connection.commit();` | Commits all customer, order, and item inserts as one successful transaction. |
| 69 | `return orderId;` | Returns the order ID so the app can show confirmation. |
| 70 | `} catch (SQLException | RuntimeException exception) {` | Handles database errors and unexpected runtime errors from the transaction work. |
| 71 | `connection.rollback();` | Undoes all uncommitted inserts so the database does not keep a partial order. |
| 72 | `throw exception;` | Sends the failure back to the caller so it can show an error. |
| 73 | `} finally {` | Starts cleanup that runs whether the transaction succeeds or fails. |
| 74 | `connection.setAutoCommit(true);` | Restores the connection's default behavior before it is closed. |
| 75 | `}` | Closes the cleanup block. |
| 76 | `}` | Closes the connection resource block, which closes the connection. |
| 77 | `}` | Closes `placeOrder`. |
| 79 | `@Override` | Marks the history method as an implementation of the interface contract. |
| 80 | `public List<OrderSummary> findOrdersByEmail(String email) throws SQLException {` | Starts a method that retrieves prior orders matching the customer's email. |
| 81 | `String sql = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount "` | Selects the history fields needed for display from the orders table. |
| 82 | `+ "FROM orders o JOIN customers c ON c.customer_id = o.customer_id "` | Joins orders to customers so the query can filter by customer email. |
| 83 | `+ "WHERE c.email = ? ORDER BY o.order_date DESC";` | Filters by a parameterized email and sorts newest orders first. |
| 84 | `List<OrderSummary> orders = new ArrayList<>();` | Creates an empty list to collect history rows. |
| 85 | `try (Connection connection = DatabaseConnection.open();` | Opens a database connection and starts automatic resource cleanup. |
| 86 | `PreparedStatement statement = connection.prepareStatement(sql)) {` | Prepares the history query on that connection. |
| 87 | `statement.setString(1, email);` | Sets the email parameter safely rather than placing user text directly in SQL. |
| 88 | `try (ResultSet results = statement.executeQuery()) {` | Runs the SELECT and opens its rows in an auto-closed result set. |
| 89 | `while (results.next()) {` | Loops through each order returned by the query. |
| 90 | `Timestamp timestamp = results.getTimestamp("order_date");` | Reads the database timestamp in JDBC's SQL date/time type. |
| 91 | `orders.add(new OrderSummary(results.getInt("order_id"),` | Begins constructing a summary using the order ID and adds it to the list. |
| 92 | `timestamp == null ? null : timestamp.toLocalDateTime(),` | Converts a non-null SQL timestamp to Java's `LocalDateTime`; preserves null if the database value is missing. |
| 93 | `results.getString("order_status"), results.getBigDecimal("total_amount")));` | Reads the status and precise total, then completes the `OrderSummary`. |
| 94 | `}` | Closes the result-row loop. |
| 95 | `}` | Closes the result-set block. |
| 96 | `}` | Closes the connection/statement block, cleaning them up. |
| 97 | `return orders;` | Returns the collected summaries to the caller. |
| 98 | `}` | Closes the history method. |
| 99 | `}` | Closes the repository class. |
