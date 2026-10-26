# FoodOrder

FoodOrder is a Java 17 desktop application for a sample food-ordering flow. It uses Swing for the interface, MySQL for customer account registration and login, and Java collections for the sample restaurant menus and current order. The interface demonstrates choosing a restaurant, adding menu items to a cart, selecting a demo payment method, viewing delivery progress, and leaving a rating.

This README explains the concepts used in the project and points to where they appear, following the Java and OOP syllabus supplied with the project. It also distinguishes the launched application from a second, repository-based implementation that is present in the source tree.

## Run the application

Requirements: JDK 17 or newer, Maven, and MySQL.

1. Start MySQL and create the database/account table by running [`database/setup.sql`](database/setup.sql) in MySQL Workbench. The database is named `food_ordering`.
2. Run `mvn compile` once to download MySQL Connector/J.
3. Launch [`Start FoodOrder.bat`](Start%20FoodOrder.bat), enter the MySQL connection URL and username (or accept the defaults), and enter the password. The launcher compiles and starts the app.

The launcher uses `FOOD_DB_URL`, `FOOD_DB_USER`, and `FOOD_DB_PASSWORD` environment variables for the database connection. Registration and login require MySQL. The app's sample restaurant menus, cart, demo payment, delivery steps, and rating are held in memory and are not saved to the database. Payment choices are demonstrations; the app does not process payments.

## Project layout

```text
src/main/java/com/foodordering/
  Main.java                         Application entry point
  FoodOrderingApp.java              Launched Swing application and ordering flow
  config/DatabaseConnection.java     MySQL connection helper
  model/                             Customer, menu, cart, and order data classes
  repository/                        Account, menu, and order persistence code
  ui/FoodOrderingFrame.java          Alternate repository-backed Swing interface
database/
  setup.sql                          Database and customer account table
  seed_menu.sql                      Example menu data for the alternate UI
doc/                                 Additional implementation notes
```

`Main.main` opens `FoodOrderingApp` on Swing's Event Dispatch Thread. `FoodOrderingFrame` is another UI in the project: it uses `MenuRepository` and `OrderRepository` to load menu data from MySQL and save orders. The launcher does not open this frame. Its expected menu, customer, order, and order-item tables are not created by the current `database/setup.sql`; its data layer therefore describes an alternate/incomplete path and needs the corresponding schema before it can be used end-to-end.

## Syllabus concepts and where they appear

### Section 1: Java introduction and OOP concepts

| Syllabus concept | How this project uses it |
| --- | --- |
| Java program structure and `main` | Packages, imports, classes, and methods organize the code. `Main.java` declares `public static void main(String[] args)` as the starting point. |
| Java platform, JVM, JRE, JDK | The source targets Java 17 (`pom.xml` and the launcher use `--release 17`). A JDK compiles the source; the JVM runs the compiled classes. |
| Data types, variables, and operators | Fields and local variables use `String`, `int`, `boolean`, arrays, collections, and other types. Comparisons, arithmetic, assignment, and boolean operators appear throughout `FoodOrderingApp` and the model/repository classes. |
| Control structures | `if` validation and error handling, `switch` in `goBack()`, and `for` loops building menu cards and rating stars are in `FoodOrderingApp.java`. Loops also process SQL results and cart items in the repository and model classes. |
| Classes and objects | The application creates Swing components and domain objects such as `Customer`, `MenuItem`, `CartItem`, and `Order`. |
| Constructors | Constructors initialize data objects and UI classes, for example `Customer(...)`, `MenuItem(...)`, `CartItem(...)`, and `FoodOrderingApp()`. |
| Encapsulation and access modifiers | Domain fields are `private`; public getters expose selected state. Classes and members use `public`, `private`, and `static` to control access and ownership. |
| `this` keyword | Constructors and instance methods use `this` where needed to refer to the current object (for example when a parameter has the same name as a field). |
| Static members | `FoodOrderingApp` defines shared restaurant/menu data and helper methods; `AuthRepository` defines shared hashing settings and a `SecureRandom`; `Main.main` is static. |
| Method overloading | The project mainly uses distinct method names and does not demonstrate a substantial overload set. Swing/Java library APIs may be overloaded, but that is not a project-authored OOP example. |
| Destructor / `finalize` concept | Not implemented. Java manages memory with garbage collection; the project uses try-with-resources to close database resources instead of a destructor. |

### Inheritance, polymorphism, abstraction, and object relationships

| Syllabus concept | How this project uses it |
| --- | --- |
| Inheritance | `FoodOrderingApp` extends `JFrame`; `FoodOrderingFrame` also extends `JFrame`. An anonymous `DefaultTableModel` subclass in `FoodOrderingFrame` overrides `isCellEditable`. |
| Method overriding and dynamic dispatch | The `DefaultTableModel.isCellEditable` override is used to make table cells read-only. Swing also calls overridden behavior through its base-class references. |
| Polymorphism | The repository interfaces allow JDBC implementations to be supplied through interface types in `FoodOrderingFrame`. The launched `FoodOrderingApp` currently uses `AuthRepository` directly, so repository substitution is not used in its order flow. |
| Abstract classes | No project-authored abstract class. Java/Swing provides abstract types internally. |
| `super` | `super(...)` calls initialize the Swing `JFrame` superclass in the window constructors. |
| Composition and aggregation | An `Order` contains a list of `CartItem` objects; each `CartItem` refers to a `MenuItem`. The UIs hold panels, fields, buttons, and models as component objects. These are practical “has-a” relationships. |
| Association | The repository classes use the database connection helper and exchange model objects with the UI. |
| Final classes/members | `DatabaseConnection` is final and has a private constructor so it acts as a static utility. Several model fields are final to keep identity/data fixed after construction. |

### Section 2: Packages, interfaces, and access protection

| Syllabus concept | How this project uses it |
| --- | --- |
| Packages and imports | Code is divided among `com.foodordering`, `.model`, `.repository`, `.config`, and `.ui`; Java and JDBC/Swing packages are imported where used. |
| Interfaces | `MenuRepository` defines menu-reading behavior and `OrderRepository` defines order-saving/history behavior. `JdbcMenuRepository` and `JdbcOrderRepository` implement those interfaces. |
| Access protection | `private` fields and helpers keep implementation details inside their class; `public` constructors and methods expose the operations needed elsewhere. The package structure provides a modular organization. |
| Java standard packages | The source uses `java.util` collections, `java.math.BigDecimal` for prices, `java.sql` JDBC, `java.time` date/time, `javax.swing` UI components, and `javax.crypto`/`java.security` for password hashing. |
| Constructor chaining | Swing windows call `super(title)` to initialize `JFrame`. There is no broad example of `this(...)` constructor chaining among project constructors. |
| Interface inheritance | No project interface extends another interface. |

### Exceptions, files, and database I/O

| Syllabus concept | How this project uses it |
| --- | --- |
| `try` / `catch` / `finally` | `FoodOrderingApp.authenticate()` handles SQL and configuration errors and clears the temporary password in `finally`. `AuthRepository` catches cryptography failures and wraps them as `SQLException`. |
| Multiple catches and multi-catch | Multi-catch appears in authentication and the JDBC order transaction (`SQLException | RuntimeException`). |
| `throw` and `throws` | Repository methods declare `throws SQLException`; invalid or failed operations propagate exceptions to the UI for a message. |
| Built-in and unchecked exceptions | JDBC failures use `SQLException`; missing configuration throws `IllegalStateException`; parsing and input validation may raise `NumberFormatException`. |
| Custom exceptions | No custom exception subclass is defined. |
| Chained exceptions | `AuthRepository.hash` wraps a security provider failure in a `SQLException` with the original exception as its cause. |
| File handling and streams | The Java app does not read or write application data files. The SQL setup/seed scripts and PowerShell launcher are project files, not Java file-stream examples. Database persistence uses JDBC instead. |
| Resource handling | JDBC `Connection`, `PreparedStatement`, and `ResultSet` objects are managed with try-with-resources in the repositories. |
| Database transaction | `JdbcOrderRepository.placeOrder()` disables auto-commit, inserts customer/order/items, commits on success, and rolls back on SQL/runtime errors. This path belongs to the alternate `FoodOrderingFrame` flow, not the launched `FoodOrderingApp`. |

## What the main classes do

- [`Main.java`](src/main/java/com/foodordering/Main.java): configures the Swing look and feel and starts the GUI on the Event Dispatch Thread.
- [`FoodOrderingApp.java`](src/main/java/com/foodordering/FoodOrderingApp.java): builds the launched multi-screen flow, validates login/registration, keeps the cart and demo order state in memory, computes totals, and displays demo checkout, delivery, and rating screens. Its nested screens use Swing layouts, buttons, fields, labels, and listeners.
- [`Customer.java`](src/main/java/com/foodordering/model/Customer.java): customer name, email, phone, and address.
- [`MenuItem.java`](src/main/java/com/foodordering/model/MenuItem.java): menu item identity, description, category, and `BigDecimal` price.
- [`CartItem.java`](src/main/java/com/foodordering/model/CartItem.java): a menu item plus quantity; computes the line subtotal.
- [`Order.java`](src/main/java/com/foodordering/model/Order.java): holds an unmodifiable copy of cart items and calculates an order total.
- [`OrderSummary.java`](src/main/java/com/foodordering/model/OrderSummary.java): compact order-history data.
- [`DatabaseConnection.java`](src/main/java/com/foodordering/config/DatabaseConnection.java): reads environment configuration and opens a JDBC connection.
- [`AuthRepository.java`](src/main/java/com/foodordering/repository/AuthRepository.java): creates the account table if necessary, registers customers, and checks passwords using per-account random salts and PBKDF2-HMAC-SHA256 hashes.
- [`MenuRepository.java`](src/main/java/com/foodordering/repository/MenuRepository.java) and [`OrderRepository.java`](src/main/java/com/foodordering/repository/OrderRepository.java): contracts for menu and order data access.
- [`JdbcMenuRepository.java`](src/main/java/com/foodordering/repository/JdbcMenuRepository.java) and [`JdbcOrderRepository.java`](src/main/java/com/foodordering/repository/JdbcOrderRepository.java): JDBC implementations for menu loading, order persistence, and order history, used by the alternate `FoodOrderingFrame`.
- [`setup.sql`](database/setup.sql) and [`seed_menu.sql`](database/seed_menu.sql): create the database/account table and provide sample menu insert statements respectively. The seed script assumes a `menu_items` table exists.

## Important Java and application ideas

- **Money:** `BigDecimal` is used for prices and totals instead of floating point values, avoiding common rounding errors in decimal arithmetic.
- **Event handling:** Swing buttons and rating stars respond through action and mouse listeners. Swing UI startup is scheduled with `SwingUtilities.invokeLater`.
- **Collections and streams:** `List` stores menus/cart items, and stream operations calculate subtotals and totals.
- **SQL safety:** JDBC statements use placeholders and bound parameters in the repositories, rather than joining user input into SQL text.
- **Password storage:** passwords are hashed with PBKDF2 and a per-account random salt. The login comparison uses `MessageDigest.isEqual`; the UI clears the password field and temporary character array after authentication.
- **Validation:** the UI checks email shape, required values, password length during registration, nonempty cart, and quantity range before proceeding.

## Topics not demonstrated by this project

The syllabus is broader than this application. The code does not provide explicit demonstrations of `switch` expressions beyond a conventional switch statement, `finalize`, custom exception subclasses, Java file byte/character streams, packages imported from a separately authored library, interface-to-interface inheritance, or a complete range of inheritance forms (single/multilevel/hierarchical). It also does not persist the launched app's menu, orders, payment, delivery, or ratings. The alternate JDBC order code is included, but its required schema is not part of the current setup script.

## Build manually

The Windows launcher compiles all Java sources and includes the Maven-downloaded Connector/J jar. To compile with Maven:

```powershell
mvn compile
```

To compile with the JDK after the connector has been downloaded, see [`start-food-ordering.ps1`](start-food-ordering.ps1), which contains the full compiler and runtime classpath setup.
