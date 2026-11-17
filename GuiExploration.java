import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Self Exercise / Exploration of Graphical User Interface in Java
 * ---------------------------------------------------------------
 * NIM  : F1D02411002
 * Name : Mohanad R. M. Abumattar
 * Class: 5A - Pemrograman Berorientasi Objek
 *
 * This program is a simple Student Course Registration form built with
 * Java Swing. It demonstrates the 5 GUI parts required by the exercise:
 *
 *  1. Top Level Container Class      -> JFrame
 *  2. Intermediate Container Class   -> JPanel (titlePanel, formPanel,
 *                                       buttonPanel, southPanel, genderPanel)
 *  3. Atomic Component Class         -> JLabel, JTextField, JComboBox,
 *                                       JRadioButton, JCheckBox, JButton,
 *                                       JTextArea (inside JScrollPane)
 *  4. Layout Classes (minimum 2)     -> BorderLayout, GridLayout, FlowLayout
 *  5. Event Listener                 -> ActionListener (interface, method
 *                                       override), MouseAdapter and
 *                                       WindowAdapter (adapter classes)
 */
public class GuiExploration extends JFrame {

    // ---------- Atomic components (fields, so listeners can reach them) ----------
    private JTextField nameField;
    private JTextField nimField;
    private JComboBox<String> courseBox;
    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;
    private JCheckBox agreeBox;
    private JTextArea outputArea;
    private JButton submitButton;
    private JButton clearButton;

    public GuiExploration() {
        // ================================================================
        // 1. TOP LEVEL CONTAINER : JFrame (this class extends JFrame)
        // ================================================================
        setTitle("Student Course Registration - GUI Exploration");
        setSize(560, 520);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // ================================================================
        // 4a. LAYOUT : BorderLayout for the whole window
        // ================================================================
        setLayout(new BorderLayout(8, 8));

        // ================================================================
        // 2. INTERMEDIATE CONTAINER : JPanel for the title
        // 4b. LAYOUT : FlowLayout
        // 3. ATOMIC COMPONENT : JLabel
        // ================================================================
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("COURSE REGISTRATION FORM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // ================================================================
        // 2. INTERMEDIATE CONTAINER : JPanel for the form
        // 4c. LAYOUT : GridLayout (6 rows x 2 columns)
        // 3. ATOMIC COMPONENTS : JLabel, JTextField, JComboBox,
        //    JRadioButton, JCheckBox
        // ================================================================
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        formPanel.add(new JLabel("Name :"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("NIM :"));
        nimField = new JTextField();
        formPanel.add(nimField);

        formPanel.add(new JLabel("Course :"));
        courseBox = new JComboBox<>(new String[]{
                "Pemrograman Berorientasi Objek",
                "Business Intelligence",
                "Kecerdasan Buatan",
                "Kriptografi",
                "E-Business"
        });
        formPanel.add(courseBox);

        formPanel.add(new JLabel("Gender :"));
        // Intermediate container inside the form: panel for the radio buttons
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        maleRadio = new JRadioButton("Male", true);
        femaleRadio = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        formPanel.add(genderPanel);

        formPanel.add(new JLabel("Agreement :"));
        agreeBox = new JCheckBox("I agree to the course rules");
        formPanel.add(agreeBox);

        add(formPanel, BorderLayout.CENTER);

        // ================================================================
        // 2. INTERMEDIATE CONTAINERS : southPanel (BorderLayout) holds
        //    buttonPanel (FlowLayout) and the output scroll pane
        // 3. ATOMIC COMPONENTS : JButton, JTextArea
        // ================================================================
        JPanel southPanel = new JPanel(new BorderLayout(6, 6));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        submitButton = new JButton("Submit");
        clearButton = new JButton("Clear");
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);
        southPanel.add(buttonPanel, BorderLayout.NORTH);

        outputArea = new JTextArea(7, 40);
        outputArea.setEditable(false);
        southPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);

        // ================================================================
        // 5. EVENT LISTENERS
        // ================================================================
        // (a) ActionListener INTERFACE - the handler class below overrides
        //     actionPerformed() from the ActionListener interface.
        ButtonHandler handler = new ButtonHandler();
        submitButton.addActionListener(handler);
        clearButton.addActionListener(handler);

        // (b) MouseAdapter CLASS - overrides mouseEntered/mouseExited to
        //     highlight the Submit button when the mouse hovers over it.
        submitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                submitButton.setBackground(new Color(180, 220, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                submitButton.setBackground(null);
            }
        });

        // (c) WindowAdapter CLASS - overrides windowClosing to ask for a
        //     confirmation before the window really closes.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int answer = JOptionPane.showConfirmDialog(
                        GuiExploration.this,
                        "Are you sure you want to exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Event handler class that IMPLEMENTS the ActionListener interface.
     * The actionPerformed method is an override of the interface method.
     */
    private class ButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == submitButton) {
                String name = nameField.getText().trim();
                String nim = nimField.getText().trim();
                if (name.isEmpty() || nim.isEmpty()) {
                    JOptionPane.showMessageDialog(GuiExploration.this,
                            "Please fill in Name and NIM first!",
                            "Incomplete Data", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!agreeBox.isSelected()) {
                    JOptionPane.showMessageDialog(GuiExploration.this,
                            "Please tick the agreement checkbox!",
                            "Incomplete Data", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String gender = maleRadio.isSelected() ? "Male" : "Female";
                outputArea.append("=== Registration Data ===\n");
                outputArea.append("Name   : " + name + "\n");
                outputArea.append("NIM    : " + nim + "\n");
                outputArea.append("Course : " + courseBox.getSelectedItem() + "\n");
                outputArea.append("Gender : " + gender + "\n\n");
            } else if (e.getSource() == clearButton) {
                nameField.setText("");
                nimField.setText("");
                courseBox.setSelectedIndex(0);
                maleRadio.setSelected(true);
                agreeBox.setSelected(false);
                outputArea.setText("");
                nameField.requestFocus();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiExploration frame = new GuiExploration();
            frame.setVisible(true);

            // Demo mode (used only to produce the README screenshot):
            // pre-fills the form and simulates one registration.
            if (args.length > 0 && args[0].equals("demo")) {
                frame.nameField.setText("Mohanad R. M. Abumattar");
                frame.nimField.setText("F1D02411002");
                frame.courseBox.setSelectedIndex(0);
                frame.agreeBox.setSelected(true);
                frame.submitButton.doClick();
            }
        });
    }
}
