import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO - the only class that talks to the database (JDBC).
 *
 * Required JDBC pieces, all in one DAO:
 *   Connection        - DriverManager opens the SQLite database file
 *                       through the SQL connector (sqlite-jdbc.jar).
 *   PreparedStatement - every statement binds its values with "?"
 *                       parameters instead of string concatenation.
 *   Transaction       - insert/update/delete run with auto-commit off:
 *                       commit() when the work succeeds, rollback()
 *                       when anything fails.
 *   ResultSet         - findAll() reads the query result row by row
 *                       and maps each row to a Product object.
 *
 * Author: MOHANAD R. M. ABUMATTAR (F1D02411002) - Class 5A, PBO
 */
public class ProductDAO {

    private static final String DB_URL = "jdbc:sqlite:inventory.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // register the SQL connector driver
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "sqlite-jdbc.jar is missing from the classpath: " + e.getMessage());
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /** Creates the products table the first time the app runs. */
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS products ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "category TEXT, "
                + "price REAL, "
                + "stock INTEGER)";
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /** CREATE - one new product, wrapped in a transaction. */
    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, category, price, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, product.getName());
                ps.setString(2, product.getCategory());
                ps.setDouble(3, product.getPrice());
                ps.setInt(4, product.getStock());
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** READ - every product, mapped from the ResultSet one row at a time. */
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, category, price, stock FROM products ORDER BY id";
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                products.add(p);
            }
        }
        return products;
    }

    /** UPDATE - changes the product with the given id, in a transaction. */
    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, category = ?, price = ?, stock = ? WHERE id = ?";
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, product.getName());
                ps.setString(2, product.getCategory());
                ps.setDouble(3, product.getPrice());
                ps.setInt(4, product.getStock());
                ps.setInt(5, product.getId());
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** DELETE - removes the product with the given id, in a transaction. */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
