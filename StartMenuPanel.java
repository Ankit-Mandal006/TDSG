import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

class StartMenuPanel extends JPanel {
    private GameMain gameMain;
    private BufferedImage backgroundImage;
    private Timer animationTimer;

    private double titleScale = 1.0;
    private double scaleDirection = 0.005;
    private int backgroundYOffset = 0;

    private JButton loginButton; // ✅ Now a JButton

    public StartMenuPanel(GameMain mainFrame) {
        this.gameMain = mainFrame;
        setPreferredSize(new Dimension(1024, 576));
        loadBackgroundImage();
        initComponents();
        startAnimation();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("assets/bg.png"));
        } catch (IOException e) {
            System.err.println("Could not load 'menu_background.png'.");
            backgroundImage = null;
        }
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        JPanel menuItemsPanel = new JPanel();
        menuItemsPanel.setOpaque(false);
        menuItemsPanel.setLayout(new BoxLayout(menuItemsPanel, BoxLayout.Y_AXIS));

        // === LOGIN / LOGOUT BUTTON ===
        loginButton = new JButton();
        updateLoginButton(); // set initial state (Login or Logout)
        loginButton.setFont(new Font("Impact", Font.PLAIN, 28));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLoginLogout());
        menuItemsPanel.add(loginButton);

        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // === NEW GAME ===
        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setFont(new Font("Impact", Font.PLAIN, 28));
        newGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameBtn.addActionListener(e -> {
            if (gameMain.getUsername() != null && !gameMain.getUsername().isEmpty()) {
                gameMain.showGamePanel();              // ✅ Start game
                new LeaderboardWindow();               // ✅ Open leaderboard beside game
            } else {
                JOptionPane.showMessageDialog(this, "Please login first!");
            }
        });
        menuItemsPanel.add(newGameBtn);

        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // === QUIT ===
        JButton quitBtn = new JButton("Quit");
        quitBtn.setFont(new Font("Impact", Font.PLAIN, 28));
        quitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitBtn.addActionListener(e -> showExitConfirmation());
        menuItemsPanel.add(quitBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 300, 0, 0);
        add(menuItemsPanel, gbc);
    }

    private void handleLoginLogout() {
        if (gameMain.getUsername() == null || gameMain.getUsername().isEmpty()) {
            // Open login window
            new LoginWindow();
            SwingUtilities.getWindowAncestor(this).dispose(); // close GameMain
        } else {
            // Logout
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Logout from " + gameMain.getUsername() + "?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // reset user session
                gameMain.dispose();
                SwingUtilities.invokeLater(LoginWindow::new);
            }
        }
    }

    private void updateLoginButton() {
        if (gameMain.getUsername() != null && !gameMain.getUsername().isEmpty()) {
            loginButton.setText("Logout (" + gameMain.getUsername() + ")");
        } else {
            loginButton.setText("Login");
        }
    }

    private void startAnimation() {
        animationTimer = new Timer(30, e -> {
            titleScale += scaleDirection;
            if (titleScale > 1.05 || titleScale < 0.95) scaleDirection *= -1;

            backgroundYOffset++;
            if (backgroundYOffset >= getHeight()) backgroundYOffset = 0;

            repaint();
        });
        animationTimer.start();
    }

    private void showExitConfirmation() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, backgroundYOffset, getWidth(), getHeight(), this);
            g2d.drawImage(backgroundImage, 0, backgroundYOffset - getHeight(), getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        String title = "Trigger Tracker";
        Font baseFont = new Font("Impact", Font.BOLD, 90);
        Font scaledFont = baseFont.deriveFont((float) (baseFont.getSize() * (float)titleScale));
        g2d.setFont(scaledFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int x = (getWidth() - titleWidth) / 2;
        int y = getHeight() / 2 - 120;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(title, x + 5, y + 5);
        g2d.setColor(new Color(170, 210, 255));
        g2d.drawString(title, x, y);
    }
}
