import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * InventoryApp - Swing window for a small shop inventory (CRUD on products).
 *
 * Case study pattern (Thread and Database):
 *
 *     button click -> start a worker Thread -> DAO talks to the database
 *                  -> SwingUtilities.invokeLater -> refresh the GUI
 *
 * The database is never touched on the Event Dispatch Thread, so the
 * window stays responsive while SQLite works. While a task runs, the
 * buttons are disabled and the status label shows what is happening
 * ("Saving...", "Loading..."); when the worker finishes, invokeLater
 * re-enables the buttons, updates the label and reloads the table.
 *
 * Author: MOHANAD R. M. ABUMATTAR (F1D02411002) - Class 5A, PBO
 */
public class InventoryApp extends JFrame {

    private final ProductDAO dao = new ProductDAO();

    private final JTextField nameField = new JTextField(18);
    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{"Accessories", "Audio", "Computers", "Gaming", "Other"});
    private final JTextField priceField = new JTextField(18);
    private final JTextField stockField = new JTextField(18);

    private final JButton addButton = new JButton("Add");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear");
    private final JButton refreshButton = new JButton("Refresh");

    private final JLabel statusLabel = new JLabel("Ready");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Price", "Stock"}, 0);
    private final JTable productTable = new JTable(tableModel);

    private int selectedId = -1;

    public InventoryApp() {
        super("Shop Inventory - Thread & Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(660, 470);
        setLocationRelativeTo(null);

        // ----- input form -----
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryBox);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);

        // ----- buttons + status -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(statusLabel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(formPanel, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // ----- events -----
        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> refreshTable());

        // Selecting a row loads that product into the form for editing.
        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                selectedId = (Integer) tableModel.getValueAt(row, 0);
                nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                categoryBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 2)));
                priceField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                stockField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
            }
        });

        // First database contact also happens off the EDT.
        runDbWork("Loading...", "Ready", () -> {
            dao.createTable();
            return dao.findAll();
        });
    }

    // ==================================================================
    //  The pattern: worker Thread does the DB work, invokeLater the GUI
    // ==================================================================

    /** A unit of database work that returns the fresh table content. */
    private interface DbWork {
        List<Product> execute() throws Exception;
    }

    /**
     * Runs work on a background thread.
     * Before: all buttons disabled, status shows busyText ("Saving...").
     * After : invokeLater back on the EDT - buttons enabled again, status
     *         shows doneText and the table shows the returned rows.
     */
    private void runDbWork(String busyText, String doneText, DbWork work) {
        setBusy(true, busyText);
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(400); // keep the busy state visible for the demo
                List<Product> rows = work.execute();
                SwingUtilities.invokeLater(() -> {
                    fillTable(rows);
                    setBusy(false, doneText);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false, "Error");
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
        worker.start();
    }

    private void setBusy(boolean busy, String message) {
        addButton.setEnabled(!busy);
        updateButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        statusLabel.setText(message);
    }

    private void fillTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStock()});
        }
    }

    private void refreshTable() {
        runDbWork("Loading...", "Ready", dao::findAll);
    }

    // ==================================================================
    //  CRUD actions - every one goes through runDbWork
    // ==================================================================

    private void addProduct() {
        Product p = readForm();
        if (p == null) {
            return;
        }
        runDbWork("Saving...", "Saved", () -> {
            dao.insert(p);
            return dao.findAll();
        });
        clearForm();
    }

    private void updateProduct() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Select a product in the table first.");
            return;
        }
        Product p = readForm();
        if (p == null) {
            return;
        }
        p.setId(selectedId);
        runDbWork("Updating...", "Updated", () -> {
            dao.update(p);
            return dao.findAll();
        });
        clearForm();
    }

    private void deleteProduct() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "Select a product in the table first.");
            return;
        }
        int id = selectedId;
        runDbWork("Deleting...", "Deleted", () -> {
            dao.delete(id);
            return dao.findAll();
        });
        clearForm();
    }

    // ==================================================================
    //  Form helpers
    // ==================================================================

    /** Reads the form; shows a message and returns null when invalid. */
    private Product readForm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name must not be empty.");
            return null;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            return new Product(name, String.valueOf(categoryBox.getSelectedItem()), price, stock);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be numbers.");
            return null;
        }
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        categoryBox.setSelectedIndex(0);
        priceField.setText("");
        stockField.setText("");
        productTable.clearSelection();
    }

    public static void main(String[] args) {
        // The window itself is created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new InventoryApp().setVisible(true));
    }
}
