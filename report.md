# FoodOrder project report

## Purpose and reading guide

This report explains the folders and files in the project, the code concepts they use, the methods and constructors they define, and the way the parts connect. “Initialization” means values assigned when a field is declared or when a constructor runs. The existing `doc/` line tables are included below for startup, model, and repository source files; for the two large Swing windows, the code is explained by its declaration and method blocks so related statements are understandable together.

The project is a Java 17 desktop food ordering demonstration. The runnable path launched by `Main` is `FoodOrderingApp`: it uses MySQL only for account registration/login, and keeps its restaurant menu, cart, simulated payment, delivery progress, and rating in memory. `FoodOrderingFrame` is a second, alternate UI that loads menu rows and saves order data through JDBC repositories. It is not launched by the current `Main`, and its expected menu/order schema is not created by the supplied setup script.

## Folder and file map

| Folder/file | Purpose |
|---|---|
| `src/main/java/com/foodordering/` | Java application source, organized by package. |
| `FoodOrderingApp.java` | Launched multi-screen Swing food ordering demo. |
| `Main.java` | JVM entry point; schedules and opens the launched UI. |
| `config/DatabaseConnection.java` | Reads DB environment variables and opens JDBC connections. |
| `model/` | Plain Java objects for customers, menu items, cart rows, orders, and order summaries. |
| `repository/` | Authentication, persistence contracts, and JDBC implementations. |
| `ui/FoodOrderingFrame.java` | Alternate repository-backed Swing ordering UI, not currently started by `Main`. |
| `database/setup.sql` | Creates the `food_ordering` database and account table. |
| `database/seed_menu.sql` | Inserts sample rows into an assumed `menu_items` table. |
| `doc/` | Earlier focused implementation notes; their line-by-line source tables are reproduced later in this report. |
| `pom.xml` | Maven coordinates, Java release, encoding, and MySQL Connector/J dependency. |
| `Start FoodOrder.bat` | Windows double-click entry point for the PowerShell launcher. |
| `start-food-ordering.ps1` | Collects DB configuration, compiles the Java sources, and launches `Main`. |
| `README.md` | Project overview, run instructions, architecture notes, and concept index. |
| `target/classes/` | Generated `.class` bytecode from a previous compile; not hand-written source. |
| `target/test-classes/` | Maven's generated/test output location; currently empty in the inspected tree. |

## How the parts work together

```text
Start FoodOrder.bat -> start-food-ordering.ps1 -> javac/java -> Main.main
                                                        |
                                        Swing event-dispatch thread
                                                        |
                                              FoodOrderingApp
                                      /          |           \\
                              AuthRepository   model objects   in-memory UI state
                                    |
                          DatabaseConnection -> MySQL

Alternate path (not launched by Main): FoodOrderingFrame
       -> MenuRepository / OrderRepository interfaces
       -> JdbcMenuRepository / JdbcOrderRepository
       -> DatabaseConnection -> MySQL
```

The launch script sets `FOOD_DB_URL`, `FOOD_DB_USER`, and `FOOD_DB_PASSWORD`. `Main` creates the UI on Swing's event thread. `FoodOrderingApp.authenticate()` constructs `AuthRepository` only when needed; account SQL reaches `DatabaseConnection.open()`. Successful login or registration moves the UI through restaurant selection, menu/cart, demo checkout, simulated delivery, and rating. `MenuItem` and `CartItem` objects represent the selected food and quantities, and `BigDecimal` calculations produce totals. The order is not written to MySQL in this path.

The alternate frame receives repository objects through its constructor. `JdbcMenuRepository` fetches menu rows; `JdbcOrderRepository` writes customer/order/order-item rows in a transaction and queries order history. Repository interfaces hide storage details from the frame. This path requires tables beyond `customer_accounts` that are absent from `database/setup.sql`.

## Java source files

### `src/main/java/com/foodordering/Main.java`

`main(String[] args)` is the Java entry point. `SwingUtilities.invokeLater` schedules all window setup on Swing's Event Dispatch Thread. It attempts to apply the platform look and feel; if that fails, it ignores the exception and proceeds. It then constructs `FoodOrderingApp` and makes it visible. The constructor initializes the window and all screen components. Concepts: static entry point, lambda, exception handling, Swing threading, object construction, and inheritance through the window class.

### `src/main/java/com/foodordering/FoodOrderingApp.java`

This is the launched UI and the main orchestration class. It extends `JFrame`. Imports bring together Swing/AWT widgets and layouts, domain model classes, `AuthRepository`, exact decimal arithmetic, lists, locale-normalized email handling, and SQL exceptions.

**Field initialization and state:** Color constants define a consistent green/neutral theme. `RESTAURANTS`, `EMOJIS`, and nested immutable `MENUS` define three sample restaurant menus; `item(...)` constructs each `MenuItem`. `cards`, `screens`, and `root` establish a `CardLayout` screen switcher and the outer border layout. Labels/buttons are initialized with starting text; `backButton` is created through `softButton`. `cart` starts as a mutable `ArrayList`. `accounts` and `customer` are populated after authentication. `restaurant` starts at `-1` (no restaurant selected); `rating` and `deliveryStep` start at zero; `orderId` is assigned when the demo order is confirmed. Text fields are created by `field(...)`, except the password field, and `registerMode` defaults to false. Several panel/label fields are assigned as screen builders create them.

**Constructor `FoodOrderingApp()`:** Calls `super(title)` to initialize the `JFrame`; sets close behavior, minimum and starting dimensions, and centered placement. It styles `root`, adds the header, creates the six screen panels (`auth`, `restaurants`, `menu`, `payment`, `delivery`, `rating`) and registers them with names in the `CardLayout`, installs the root content pane, and shows the authentication screen first. The screens are built once and switched rather than recreated on every navigation.

**Screen creation methods:**

- `makeHeader()` builds the brand mark/title, step text, greeting, and Back button. The Back listener calls `goBack()`.
- `makeAuthScreen()` creates the sign-in/create-account card using `GridBagLayout`, fields, tabs, and a Continue button connected to `authenticate()`. The name block begins hidden and appears in registration mode.
- `makeRestaurantScreen()` loops through the three restaurant choices and creates a tile for each. A click stores the selected index, clears the cart, updates restaurant-dependent copy, switches to the menu screen, then rebuilds menu/cart display.
- `makeMenuScreen()` builds side-by-side menu and cart cards with scroll panes. Checkout rejects an empty cart; otherwise it updates the displayed total and opens the payment screen.
- `makePaymentScreen()` shows the address/phone fields and radio-button demo payment options. Only address is validated. Confirming creates a temporary order ID and resets delivery progress; no payment processor or persistent order insert is called.
- `makeDeliveryScreen()` creates confirmation text, progress bar, named stages, and a button that advances the simulated delivery state. At the last state the button goes to rating.
- `makeRatingScreen()` creates five clickable stars and a comment box. The selected rating is required; submission displays a thank-you dialog and resets cart/order journey fields before returning to restaurant selection. The comment is not saved.

**Behavior and data methods:**

- `setRegisterMode(boolean)` stores the selected auth mode and shows/hides the name block, then revalidates/repaints the panel.
- `authenticate()` lowercases and trims email, checks a simple email pattern and nonempty password, then registers or logs in through `AuthRepository`. Registration additionally validates name and an eight-character minimum. Duplicate email error code 1062 gets a friendly message. A valid customer updates the header and navigates forward. SQL/config errors are shown in dialogs. A `finally` block overwrites the temporary password char array and clears the password field.
- `refreshMenu()` updates the menu heading to match the selected restaurant, removes old rows, and creates a row per item with name/category/description/price/add button. It calls `refreshCart()` and revalidates/repaints. `Component[] comps` is obtained but unused.
- `addItem(MenuItem)` searches cart with a stream for the same item ID; it adds a new `CartItem` or increments the existing quantity.
- `refreshCart()` reconstructs cart display rows, attaches plus/minus listeners, removes a row when quantity falls to zero, updates total label, and refreshes Swing layout/painting.
- `total()` maps cart items to subtotals and reduces them with `BigDecimal::add` starting at zero.
- `updateDelivery()` maps `deliveryStep` to status text, shows generated order ID/restaurant/total, updates progress, and changes the button label.
- `updateStars()` fills the first `rating` stars and leaves remaining stars empty.
- `show(String screen, String step)` records the screen name, tells `CardLayout` which panel to show, updates the progress heading, and hides Back on auth/delivery screens.
- `goBack()` uses a switch over the current screen to navigate to its predecessor for supported screens.
- `updateText(Component, oldText, newText)` recursively visits Swing components and changes matching `JLabel` text. It uses `instanceof` pattern matching and recursion through nested containers.
- `error(String)` shows a warning dialog.

**UI helper methods:** `page()` creates a vertical `BoxLayout` container; `panel()` creates a white bordered/padded card; `constraints()` provides common `GridBagConstraints`; `eyebrow`, `heading`, `headingSmall`, `body`, `sectionTitle`, `fieldLabel`, and `pill` construct labels with standardized roles/styles. `field()` creates a tooltip-bearing text field and calls `styleField()`. `styleField()` sets font/colors/border/size. `primaryButton()`, `softButton()`, and `miniButton()` build button variants. `radio()` builds a styled payment choice. `item()` constructs a menu item with a `BigDecimal` parsed from a string. `money()` formats amount to two decimal places with a rupee prefix.

**Concepts used:** Swing component composition and layout managers; event-driven programming through action/mouse listeners and lambdas; mutable application state; collections and streams; model objects; `BigDecimal`; validation; exception handling; recursion; `switch`; `final` fields and static constants. GUI progress, payments, feedback, restaurant menus, and orders are demo/in-memory functionality. There is no order persistence in this class.

### `src/main/java/com/foodordering/config/DatabaseConnection.java`

Utility class with a private constructor (so it is not instantiated) and static `open()`. `open()` reads URL/user via `required(key)`, reads password directly, substitutes an empty string if password is unset, then calls `DriverManager.getConnection`. `required` rejects missing or blank URL/user via `IllegalStateException`. This centralizes JDBC configuration and avoids hardcoded credentials. The launcher supplies the variables; repositories use `open()` in try-with-resources blocks. See the line table below.

### `src/main/java/com/foodordering/model/` files

- `MenuItem.java`: immutable item identity/name/description/category/price. Its constructor normalizes null description/category to empty strings. Getters expose fields without mutation. `BigDecimal` stores currency.
- `CartItem.java`: stores a fixed `MenuItem` reference and mutable quantity. `getSubtotal()` multiplies item price by quantity using `BigDecimal`.
- `Customer.java`: stores name/email/phone/address; constructor normalizes null phone to empty string; getters provide read access.
- `Order.java`: takes a defensive copy of cart lines and exposes an unmodifiable list. `getTotal()` stream-reduces item subtotals using exact decimal addition.
- `OrderSummary.java`: immutable read model for order history: database ID, `LocalDateTime`, status, total, with constructor and getters.

These are domain/data-transfer objects: they separate business values from Swing widgets and SQL row parsing. Their line-level tables follow.

### `src/main/java/com/foodordering/repository/AuthRepository.java`

This repository handles account persistence and password verification. It uses `DatabaseConnection` and JDBC. `AuthRepository()` ensures the account table exists by running a `CREATE TABLE IF NOT EXISTS` statement. `register(name,email,password)` creates a random per-account salt, hashes the supplied password using PBKDF2-HMAC-SHA256, and inserts name/email/salt/hash with a parameterized `PreparedStatement`. `login(email,password)` selects the stored salt/hash by email, recomputes the hash, compares using `MessageDigest.isEqual`, and returns a `Customer` for a match or `null` for no match. `hash(...)` configures `PBEKeySpec`, invokes `SecretKeyFactory`, returns encoded hash bytes, wraps provider errors as `SQLException`, and clears the key spec password. Hex conversion helpers encode/decode salt/hash strings. Constants hold hash iteration/key sizes and a `SecureRandom` supplies salts. Try-with-resources closes DB statements/results. Concepts: parameterized SQL, password hashing/salting, cryptographic random values, secure comparison, exceptions, and resource management. Registration/login use only fields in the provided account table.

### Repository contracts and JDBC implementations

- `MenuRepository.java` defines `findAvailableItems()`, a contract returning a list of menu models and possibly throwing `SQLException`.
- `OrderRepository.java` defines `placeOrder(Customer, Order)` and `findOrdersByEmail(String)`. It makes the UI depend on operations rather than a concrete database implementation.
- `JdbcMenuRepository.java` implements menu loading: opens connection, executes a parameterless query for available items, maps each `ResultSet` row into `MenuItem`, and returns a list. Resources close automatically.
- `JdbcOrderRepository.java` implements writes/history. `placeOrder` opens a connection, disables auto-commit, inserts/finds a customer, inserts an order, inserts each order item, commits and returns generated order ID. SQL/runtime failures trigger rollback and propagate. `findOrdersByEmail` queries orders and maps rows to `OrderSummary` using JDBC date/decimal conversions. These classes expect menu/customer/order/order-item tables that are not all created in supplied `setup.sql`.

### `src/main/java/com/foodordering/ui/FoodOrderingFrame.java`

Alternate Swing UI extending `JFrame`; not referenced by `Main`. Constructor accepts `MenuRepository` and `OrderRepository`, stores dependencies, loads menu data, initializes UI, populates tables, and may throw `SQLException`. This constructor-based dependency injection is an example of programming against interfaces.

Fields initialize menu/cart table models, tables, text fields, labels, mutable menu/cart lists, a currency formatter, and theme state. `buildWindow()` creates title/header actions, menu/cart/checkout panels, installs layouts, hooks dark-theme/history actions, then applies theme. `makeMenuPanel()` configures a read-only sortable table and add-selected action. `makeCartPanel()` builds cart controls and total. `makeCheckoutPanel()` builds customer/delivery form and Place Order action.

Operation methods: `addSelectedMenuItem()` validates selection and quantity, then adds/increments a cart model; `changeQuantity(int)` increments or decrements selected line and removes at zero; `removeSelectedCartItem()` removes selected line; `selectedCartRow()` validates cart/table selection and converts view index to model index. `placeOrder()` validates cart/customer/email, requests confirmation, creates `Customer` and defensive-copy `Order`, calls repository transaction, clears cart on success, and reports errors. `showOrderHistory()` gets email, queries repository, maps summaries into a dialog table. `refreshMenu()`/`refreshCart()` rebuild table model rows; `cartTotal()` reduces subtotals.

UI support methods: `model(String...)` creates a `DefaultTableModel` subclass whose `isCellEditable` returns false; `cardPanel`, `configureTable`, and `stylePrimary` style common widgets; `applyTheme()` and recursive `applyThemeTo(Component)` style the whole component tree according to `darkTheme`; `styleTableHeader` supplies a custom header renderer; `styleScrollBar` installs themed scrollbar UI; `showMessage` wraps `JOptionPane`. Concepts: inheritance/overriding, Swing tables/renderers, interface polymorphism, transaction-backed repository use, recursion, event callbacks, input validation, and theme state. This alternate path is more database-backed than `FoodOrderingApp` but lacks supplied schema for end-to-end use.

## Database files

### `database/setup.sql`

`CREATE DATABASE IF NOT EXISTS food_ordering` is idempotent database creation. `USE food_ordering` sets the default schema. The `customer_accounts` table defines an auto-increment `BIGINT` primary key, required name/email, unique email, salt and hash strings, and creation timestamp defaulted by MySQL. It supports `AuthRepository`. It does not define `menu_items`, `customers`, `orders`, or `order_items` tables used by the alternate JDBC repositories.

### `database/seed_menu.sql`

Selects `food_ordering` and inserts six example menu rows with name, description, category, decimal price, and availability. It assumes a `menu_items` table with compatible columns already exists; the current setup script does not create that table. Re-running the seed may duplicate rows unless the table has additional uniqueness rules.

## Build and documentation files

### `pom.xml`

Maven project descriptor: XML namespace/schema identify Maven's project format; `modelVersion` is Maven model 4.0.0; group/artifact/version identify this project. Properties target Java release 17 and UTF-8 source encoding. The sole dependency is MySQL Connector/J 9.4.0 so JDBC can connect to MySQL. It does not configure a custom packaging or test framework.

### `Start FoodOrder.bat`

Turns off command echo, invokes PowerShell without a profile using execution-policy bypass and the script in the same folder (`%~dp0` is the batch file's directory), then pauses if the script exits with an error so the console remains visible.

### `start-food-ordering.ps1`

Stops on PowerShell errors; calculates project, class output, and Maven connector JAR paths; recursively finds Java source files. It checks for `javac` and the downloaded connector, creates `target/classes`, prompts for DB URL/user/password with defaults for URL/user, and reads the password as a `SecureString`. It exports the three environment variables expected by Java. In the project directory it runs `javac --release 17 -encoding UTF-8`, then launches `com.foodordering.Main` with compiled classes and connector in the classpath. `try/finally` restores the location, zeroes the unmanaged password buffer, and removes the environment variables. Concepts: process environment, paths, validation, secure prompt handling, compilation, classpaths, and cleanup.

### `README.md` and `doc/README.md`

`README.md` introduces the app, provides run instructions and a concept map, and documents the distinction between launched and alternate UI paths. `doc/README.md` indexes the focused notes. The three focused docs describe startup/database connection, data models, and repositories. Their line tables are included below.

### `target/`

Build output, not source. `target/classes` contains JVM bytecode (`.class`) generated from Java source (including compiler-generated anonymous/inner classes such as `$1`). It can be recreated by compilation; inspect/edit the `.java` files instead. `target/test-classes` is the conventional test output directory and is empty in this workspace.

## Source line tables from `doc/`

The following tables retain the detailed line-by-line code explanations for startup, models, and repositories.

# Application startup and database connection

## `Main.java`

Source: `src/main/java/com/foodordering/Main.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering;` | Places the class in the application's top-level package so Java can organize and find it. |
| 3 | `import com.foodordering.repository.JdbcMenuRepository;` | Makes the MySQL menu repository available by its short class name. |
| 4 | `import com.foodordering.repository.JdbcOrderRepository;` | Makes the MySQL order repository available in this startup class. |
| 5 | `import com.foodordering.ui.FoodOrderingFrame;` | Imports the main window class so `Main` can create the GUI. |
| 7 | `import javax.swing.JOptionPane;` | Provides popup dialogs for startup errors. |
| 8 | `import javax.swing.SwingUtilities;` | Provides Swing's event queue helper. Swing windows should be created on this thread. |
| 9 | `import javax.swing.UIManager;` | Lets the app request the platform's standard Swing appearance. |
| 10 | `import java.sql.SQLException;` | Names the checked exception JDBC can report for database failures. |
| 12 | `public class Main {` | Declares the program's startup class. The opening brace begins its body. |
| 13 | `public static void main(String[] args) {` | Declares Java's entry point. The JVM calls this method when the program starts. |
| 14 | `SwingUtilities.invokeLater(() -> {` | Schedules GUI creation on Swing's event-dispatch thread to avoid unsafe GUI updates. The arrow begins a short lambda task. |
| 15 | `try {` | Starts a block where startup errors will be caught and shown to the user. |
| 16 | `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());` | Selects the operating system's standard Swing control style. |
| 17 | `new FoodOrderingFrame(new JdbcMenuRepository(), new JdbcOrderRepository()).setVisible(true);` | Creates database repository implementations, passes them to the main window, and shows the window. |
| 18 | `} catch (SQLException exception) {` | Handles SQL failures separately, such as failing to load the menu during startup. |
| 19 | `JOptionPane.showMessageDialog(null,` | Opens an error dialog not attached to a parent window. The message arguments continue on the following lines. |
| 20 | `"Could not load the food menu from MySQL.\n" + exception.getMessage(),` | Tells the user what operation failed, adds a line break, and appends the database's error message. |
| 21 | `"Database connection error", JOptionPane.ERROR_MESSAGE);` | Sets the dialog title and selects the error icon/style. |
| 22 | `} catch (Exception exception) {` | Catches other startup exceptions not specifically represented by `SQLException`. |
| 23 | `JOptionPane.showMessageDialog(null, exception.getMessage(),` | Shows the exception's message in a dialog. The title and error style continue on the next line. |
| 24 | `"Application error", JOptionPane.ERROR_MESSAGE);` | Names the dialog and marks it as an application error. |
| 25 | `}` | Closes the general exception handler. |
| 26 | `});` | Closes the scheduled lambda and submits it to Swing's event queue. |
| 27 | `}` | Closes the `main` method. |
| 28 | `}` | Closes the `Main` class. |

## `DatabaseConnection.java`

Source: `src/main/java/com/foodordering/config/DatabaseConnection.java`

| Line | Code | What it does and why it is used |
|---:|---|---|
| 1 | `package com.foodordering.config;` | Places shared configuration code in the `config` package. |
| 3 | `import java.sql.Connection;` | Imports the JDBC connection type returned to database code. |
| 4 | `import java.sql.DriverManager;` | Imports JDBC's utility for opening a connection from a URL and login. |
| 5 | `import java.sql.SQLException;` | Imports the JDBC exception type that can be thrown when opening a connection fails. |
| 7 | `/** Opens connections to the MySQL database using environment configuration. */` | Documents the purpose of the class for readers and IDE help. |
| 8 | `public final class DatabaseConnection {` | Declares a public utility class and prevents subclassing with `final`. |
| 9 | `private DatabaseConnection() {}` | Makes the constructor private so callers cannot create pointless instances of this utility class. |
| 11 | `public static Connection open() throws SQLException {` | Declares a class-level method that opens and returns a database connection; callers handle SQL errors. |
| 12 | `String url = required("FOOD_DB_URL");` | Reads the JDBC connection URL and rejects it if it was not configured. |
| 13 | `String user = required("FOOD_DB_USER");` | Reads the MySQL username, using the same required-value check. |
| 14 | `String password = System.getenv("FOOD_DB_PASSWORD");` | Reads the password from an environment variable rather than storing it in source code. |
| 15 | `if (password == null) password = "";` | Uses an empty password if the environment variable is absent, allowing local MySQL accounts with no password. |
| 16 | `return DriverManager.getConnection(url, user, password);` | Asks JDBC to connect to MySQL and returns the live connection to the caller. |
| 17 | `}` | Closes the `open` method. |
| 19 | `private static String required(String key) {` | Starts a helper that retrieves a required environment variable by its name. |
| 20 | `String value = System.getenv(key);` | Gets the variable's current value from the process environment. |
| 21 | `if (value == null || value.isBlank()) {` | Checks both missing and whitespace-only values so an invalid setting fails early. |
| 22 | `throw new IllegalStateException("Set the " + key + " environment variable before starting the app.");` | Stops startup with a useful message when a required setting is missing. |
| 23 | `}` | Closes the missing-value check. |
| 24 | `return value;` | Returns the verified value to the caller. |
| 25 | `}` | Closes the helper method. |
| 26 | `}` | Closes the utility class. |

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

