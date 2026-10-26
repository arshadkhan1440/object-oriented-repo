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
