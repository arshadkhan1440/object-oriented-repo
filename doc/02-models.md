# Data model classes

Model classes hold the data the application works with. The line numbers refer to files in `src/main/java/com/foodordering/model`.

## `CartItem.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.model;` | Places this class with the other application data types. |
| 3 | `import java.math.BigDecimal;` | Imports precise decimal arithmetic for money values. |
| 5 | `public class CartItem {` | Declares a cart-entry class. The brace begins its body. |
| 6 | `private final MenuItem menuItem;` | Stores which food was selected. `final` keeps the item reference fixed after construction. |
| 7 | `private int quantity;` | Stores how many of that food are in the cart; it can change as the user edits the cart. |
| 9 | `public CartItem(MenuItem menuItem, int quantity) {` | Declares the constructor used to create a cart entry with its item and quantity. |
| 10 | `this.menuItem = menuItem;` | Saves the passed item in this object. `this` refers to the object being created. |
| 11 | `this.quantity = quantity;` | Saves the passed quantity. |
| 12 | `}` | Closes the constructor. |
| 14 | `public MenuItem getMenuItem() { return menuItem; }` | Returns the food item so other code can read its name, ID, or price. |
| 15 | `public int getQuantity() { return quantity; }` | Returns the current quantity. |
| 16 | `public void setQuantity(int quantity) { this.quantity = quantity; }` | Changes the quantity when the user adds or removes units. |
| 17 | `public BigDecimal getSubtotal() { return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)); }` | Calculates this line's cost using exact decimal multiplication rather than floating-point math. |
| 18 | `}` | Closes the `CartItem` class. |

## `Customer.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.model;` | Places this class with the other data models. |
| 3 | `public class Customer {` | Declares the customer model. The brace begins the class body. |
| 4 | `private final String name;` | Stores the customer's name and prevents reassignment after construction. |
| 5 | `private final String email;` | Stores the email used to identify the customer's orders. |
| 6 | `private final String phone;` | Stores the optional phone number. |
| 7 | `private final String address;` | Stores the delivery address for the order. |
| 9 | `public Customer(String name, String email, String phone, String address) {` | Declares a constructor that receives all customer details. |
| 10 | `this.name = name;` | Copies the provided name into the new object. |
| 11 | `this.email = email;` | Copies the provided email into the new object. |
| 12 | `this.phone = phone == null ? "" : phone;` | Converts a null phone to an empty string so later code can safely check it. `? :` is Java's conditional expression. |
| 13 | `this.address = address;` | Copies the delivery address into the object. |
| 14 | `}` | Closes the constructor. |
| 16 | `public String getName() { return name; }` | Lets repository code read the customer's name. |
| 17 | `public String getEmail() { return email; }` | Lets repository code read the customer's email. |
| 18 | `public String getPhone() { return phone; }` | Lets repository code read the customer's phone number. |
| 19 | `public String getAddress() { return address; }` | Lets repository code read the delivery address. |
| 20 | `}` | Closes the `Customer` class. |

## `MenuItem.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.model;` | Places the menu model in the model package. |
| 3 | `import java.math.BigDecimal;` | Imports precise decimal arithmetic for the menu price. |
| 5 | `public class MenuItem {` | Declares the class representing one food item. |
| 6 | `private final int id;` | Stores the database ID used to link this food to an order item. |
| 7 | `private final String name;` | Stores the food's display name. |
| 8 | `private final String description;` | Stores its optional description. |
| 9 | `private final String category;` | Stores its category, such as Pizza or Drinks. |
| 10 | `private final BigDecimal price;` | Stores its price precisely; `BigDecimal` avoids common rounding errors with money. |
| 12 | `public MenuItem(int id, String name, String description, String category, BigDecimal price) {` | Declares a constructor that receives a database row's values. |
| 13 | `this.id = id;` | Saves the database ID. |
| 14 | `this.name = name;` | Saves the name. |
| 15 | `this.description = description == null ? "" : description;` | Replaces a missing description with an empty string to simplify display code. |
| 16 | `this.category = category == null ? "" : category;` | Replaces a missing category with an empty string. |
| 17 | `this.price = price;` | Saves the precise price value. |
| 18 | `}` | Closes the constructor. |
| 20 | `public int getId() { return id; }` | Provides read access to the database ID. |
| 21 | `public String getName() { return name; }` | Provides read access to the name. |
| 22 | `public String getDescription() { return description; }` | Provides read access to the description. |
| 23 | `public String getCategory() { return category; }` | Provides read access to the category. |
| 24 | `public BigDecimal getPrice() { return price; }` | Provides read access to the price for totals and display. |
| 25 | `}` | Closes the `MenuItem` class. |

## `Order.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.model;` | Places the order model with the other data models. |
| 3 | `import java.math.BigDecimal;` | Imports exact decimal arithmetic for the order total. |
| 4 | `import java.util.ArrayList;` | Imports a mutable list implementation used to make a defensive copy of items. |
| 5 | `import java.util.Collections;` | Imports collection helpers used to make the copied list read-only. |
| 6 | `import java.util.List;` | Imports the list interface used for order items. |
| 8 | `public class Order {` | Declares an order model containing cart lines. |
| 9 | `private final List<CartItem> items;` | Holds the food and quantities in the order; the list reference is fixed. |
| 11 | `public Order(List<CartItem> items) {` | Declares a constructor receiving the selected cart lines. |
| 12 | `this.items = Collections.unmodifiableList(new ArrayList<>(items));` | Copies the caller's list and wraps it as read-only so later edits to the cart list do not change the order. |
| 13 | `}` | Closes the constructor. |
| 15 | `public List<CartItem> getItems() { return items; }` | Returns the order lines for saving them to the database. |
| 17 | `public BigDecimal getTotal() {` | Begins a method that calculates the order's full price. |
| 18 | `return items.stream().map(CartItem::getSubtotal)` | Turns the list into a stream and gets each line's subtotal. `CartItem::getSubtotal` is a method reference. |
| 19 | `.reduce(BigDecimal.ZERO, BigDecimal::add);` | Adds all subtotals, starting from zero, and returns the exact total. |
| 20 | `}` | Closes the total method. |
| 21 | `}` | Closes the `Order` class. |

## `OrderSummary.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.model;` | Places this database result model with the other models. |
| 3 | `import java.math.BigDecimal;` | Imports the exact numeric type used for the saved order total. |
| 4 | `import java.time.LocalDateTime;` | Imports a date-and-time type for the order timestamp. |
| 6 | `public class OrderSummary {` | Declares a small model for a row shown in order history. |
| 7 | `private final int id;` | Stores the order's database ID. |
| 8 | `private final LocalDateTime date;` | Stores when the order was created. |
| 9 | `private final String status;` | Stores the order status such as `PLACED` or `DELIVERED`. |
| 10 | `private final BigDecimal total;` | Stores the saved order total precisely. |
| 12 | `public OrderSummary(int id, LocalDateTime date, String status, BigDecimal total) {` | Declares a constructor receiving the values returned by the order-history query. |
| 13 | `this.id = id;` | Saves the order ID. |
| 14 | `this.date = date;` | Saves the order date and time. |
| 15 | `this.status = status;` | Saves the current status. |
| 16 | `this.total = total;` | Saves the total amount. |
| 17 | `}` | Closes the constructor. |
| 19 | `public int getId() { return id; }` | Lets other code read the order ID. |
| 20 | `public LocalDateTime getDate() { return date; }` | Lets other code read the date. |
| 21 | `public String getStatus() { return status; }` | Lets other code read the status. |
| 22 | `public BigDecimal getTotal() { return total; }` | Lets other code read the total. |
| 23 | `}` | Closes the `OrderSummary` class. |
