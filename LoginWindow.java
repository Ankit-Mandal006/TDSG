import java.awt.*;
import javax.swing.*;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final DatabaseManager db;

    public LoginWindow() {
        db = new DatabaseManager();

        setTitle("Trigger Tracker - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(370, 265);
        setResizable(false);
        setLocationRelativeTo(null);

        // ---- Main Layout Panel ----
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        // ---- Title ----
        JLabel titleLabel = new JLabel("Trigger Tracker Login");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0,18)));

        // ---- Username Row ----
        JPanel userRow = new JPanel(new BorderLayout());
        userRow.setOpaque(false);
        userRow.add(new JLabel("Username:"), BorderLayout.WEST);
        usernameField = new JTextField(15);
        userRow.add(usernameField, BorderLayout.CENTER);
        mainPanel.add(userRow);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // ---- Password Row ----
        JPanel passRow = new JPanel(new BorderLayout());
        passRow.setOpaque(false);
        passRow.add(new JLabel("Password:"), BorderLayout.WEST);
        passwordField = new JPasswordField(15);
        passRow.add(passwordField, BorderLayout.CENTER);
        mainPanel.add(passRow);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // ---- Buttons Row ----
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        buttonRow.add(loginBtn);
        buttonRow.add(registerBtn);
        mainPanel.add(buttonRow);

        // ---- Separator ----
        mainPanel.add(Box.createRigidArea(new Dimension(0, 11)));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        mainPanel.add(sep);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // ---- Guest Button ----
        JButton guestBtn = new JButton("Play as Guest");
        guestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        guestBtn.setForeground(new Color(30,80,160));
        guestBtn.setFont(new Font("Verdana", Font.ITALIC, 14));
        guestBtn.setBackground(new Color(227,237,255));
        guestBtn.setFocusPainted(false);
        mainPanel.add(guestBtn);

        // ---- Button Logic ----
        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());
        guestBtn.addActionListener(e -> playAsGuest());

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void playAsGuest() {
        dispose();
        new GameMain("Guest", null);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        boolean ok = db.loginUser(username, password);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            dispose();
            new GameMain(username, db);
        } else {
            String err = db.getLastError();
            if (err == null || err.isEmpty()) err = "Login failed.";
            JOptionPane.showMessageDialog(this, "Login failed: " + err);
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        boolean ok = db.registerUser(username, password);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Registered! Logging in...");
            dispose();
            new GameMain(username, db);
        } else {
            String err = db.getLastError();
            if (err == null || err.isEmpty()) err = "Registration failed.";
            JOptionPane.showMessageDialog(this, "Register failed: " + err);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}
