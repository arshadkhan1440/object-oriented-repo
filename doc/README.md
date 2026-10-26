# FoodOrder code guide

This folder explains the non-UI Java source line by line. `FoodOrderingFrame.java` is intentionally excluded because it contains the Swing user interface.

## Guides

- [Application startup and database connection](01-startup-and-connection.md)
- [Data model classes](02-models.md)
- [Repository interfaces and MySQL implementations](03-repositories.md)

Blank lines in Java files are omitted from the line tables because they only add spacing. The line numbers refer to the current source files under `src/main/java`.

## How the pieces work together

1. `Main` starts the Swing app and gives it JDBC repository objects.
2. `DatabaseConnection` reads the database settings from environment variables and opens JDBC connections.
3. Model classes represent menu items, customers, cart items, and orders as Java objects.
4. Repository interfaces define database operations; JDBC repository classes implement those operations with SQL.
5. The UI calls those repository operations. The UI code itself is not covered in these documents.
