# Java Coding Practice — Thread and Database

**Name:** MOHANAD R. M. ABUMATTAR
**NIM:** F1D02411002
**Class:** 5A — Pemrograman Berorientasi Objek (PBO)

A small **shop inventory manager** built with Java Swing. This case study
combines **multithreading** with a **database (JDBC)**: every database
operation runs on a background worker thread so the window never freezes,
and the result is brought back to the GUI with `SwingUtilities.invokeLater`.

## Screenshot

![Shop Inventory](screenshot.png)

The window has a product form (Name, Category, Price, Stock), the action
buttons (Add, Update, Delete, Clear, Refresh), a status label, and the
table of products loaded from the database.

## How the requirements are implemented

### 1. Database — SQL connector + CRUD with a DAO
- The SQL connector jar (**sqlite-jdbc**, the JDBC driver for SQLite) is
  imported into the project together with its **slf4j-api** jar. The
  database is one file, `inventory.db`, created automatically on first run.
- All database access lives in one DAO class, **`ProductDAO.java`**:
  - `Connection` — opened with `DriverManager.getConnection("jdbc:sqlite:inventory.db")`
  - `PreparedStatement` — every SQL statement binds values with `?` parameters
  - **Transaction** — insert/update/delete call `setAutoCommit(false)`,
    then `commit()` on success or `rollback()` on failure
  - `ResultSet` — `findAll()` walks the result set row by row and builds
    `Product` objects
- CRUD operations: **Create** (`insert`), **Read** (`findAll`), **Update**
  (`update`), **Delete** (`delete`) on the `products` table
  (`id, name, category, price, stock`).
- `Product.java` is the model class (POJO) for one row of the table.

### 2. The pattern: DB on a thread → invokeLater → UI
`InventoryApp.java` never touches the database on the Event Dispatch
Thread. Every action follows the same pattern:

```
click handler  ->  start a worker Thread  ->  DAO does the database work
               ->  SwingUtilities.invokeLater(...)  ->  update the GUI
```

### 3. GUI and thread behaviour
Example with the **Add** button (Update/Delete/Refresh work the same way):
1. The click handler **disables all buttons** and shows the label **"Saving..."**
2. A background thread inserts the product through the DAO
3. `invokeLater` returns to the UI thread: the buttons are enabled again,
   the label shows **"Saved"**, and the table is refreshed from the database

Clicking a row in the table copies the product into the form, so it can be
updated or deleted.

## Files

| File | Description |
|---|---|
| `InventoryApp.java` | Swing GUI + the thread / invokeLater pattern |
| `ProductDAO.java` | DAO: Connection, PreparedStatement, transactions, ResultSet, CRUD |
| `Product.java` | Model class for a product |
| `sqlite-jdbc.jar` | The SQL connector (JDBC driver) used by the project |
| `slf4j-api.jar` | Logging API jar required by the SQLite JDBC driver |
| `inventory.db` | The database file (created automatically if deleted) |
| `screenshot.png` | Screenshot of the application |

## How to run

You need a JDK (17 or newer). Then, in this folder:

```bash
javac -cp sqlite-jdbc.jar:slf4j-api.jar Product.java ProductDAO.java InventoryApp.java
java  -cp .:sqlite-jdbc.jar:slf4j-api.jar InventoryApp
```

(On Windows, replace the `:` in the classpath with `;`.)
