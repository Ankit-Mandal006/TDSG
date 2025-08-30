import java.awt.*;
import javax.swing.*;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final DatabaseManager db;

    public LoginWindow() {
        db = new DatabaseManager();

        setTitle("Game Login");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginBtn);
        add(registerBtn);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());

        setVisible(true);
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
            // auto-login after registration
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
