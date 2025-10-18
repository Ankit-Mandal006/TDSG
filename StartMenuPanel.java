import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

class StartMenuPanel extends JPanel {
    private final GameMain gameMain;
    private BufferedImage backgroundImage;
    private Timer animationTimer;
    private JButton loginButton, userButton;

    private double titleScale = 1.0;
    private double scaleDirection = 0.003;
    private int backgroundYOffset = 0;

    public StartMenuPanel(GameMain mainFrame) {
        this.gameMain = mainFrame;
        setPreferredSize(new Dimension(1024, 576));
        setLayout(new BorderLayout());
        setBackground(Color.decode("#181a20"));
        loadBackgroundImage();
        startAnimation();

        // Main menu panel
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(120, 0, 0, 0));

        // Title
        JLabel titleLabel = new JLabel("");
        titleLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 75));
        titleLabel.setForeground(new Color(230, 235, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(titleLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 100)));

        // Fonts and border
        Font menuFont = new Font("Segoe UI Semibold", Font.PLAIN, 28);
        int borderRadius = 20;
        int borderThickness = 4;

        

        // NEW GAME BUTTON
        JButton startBtn = minimalistMenuButton("NEW GAME", menuFont, borderRadius, borderThickness);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> {
            if (gameMain.getUsername() != null && !gameMain.getUsername().isEmpty()) {
                gameMain.showGamePanel();
                new LeaderboardWindow();
            } else {
                JOptionPane.showMessageDialog(this, "Please login first!");
            }
        });
        menuPanel.add(startBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // LEADERBOARD BUTTON
        JButton leaderboardBtn = minimalistMenuButton("LEADERBOARD", menuFont, borderRadius, borderThickness);
        leaderboardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardBtn.addActionListener(e -> new LeaderboardWindow());
        menuPanel.add(leaderboardBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        
        // SHOP BUTTON
        JButton shopBtn = minimalistMenuButton("SHOP", menuFont, borderRadius, borderThickness);
        shopBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        shopBtn.addActionListener(e -> {
            String username = gameMain.getUsername();
            DatabaseManager db = gameMain.getDatabaseManager();


            if (username != null && !username.isEmpty() && db != null) {
                new ShopWindow(username, db);
            } else {
                JOptionPane.showMessageDialog(this, "Please log in to access the shop.");
            }
        });
        menuPanel.add(shopBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 12)));



        // QUIT BUTTON
        JButton quitBtn = minimalistMenuButton("QUIT GAME", menuFont, borderRadius, borderThickness);
        quitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitBtn.addActionListener(e -> showExitConfirmation());
        menuPanel.add(quitBtn);

        add(menuPanel, BorderLayout.CENTER);

     // --- User/account (logout) button at bottom left, with icon + name ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 15, 0));

        // Use a font that supports the icon
        userButton = new JButton();
        userButton.setFocusPainted(false);
        userButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        userButton.setBackground(new Color(34, 36, 44, 160));
        userButton.setForeground(new Color(225, 230, 240));
        userButton.setPreferredSize(new Dimension(220, 38));
        userButton.setMaximumSize(new Dimension(260, 40));
        userButton.setOpaque(true);
        userButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userButton.setBorder(new RoundedBorder(20, new Color(94, 104, 120), 4));
        userButton.setToolTipText("Account");
        userButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                userButton.setBackground(new Color(54, 47, 76, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                userButton.setBackground(new Color(34, 36, 44, 160));
            }
        });
        userButton.addActionListener(e -> handleLoginLogout());
        bottomPanel.add(userButton);
        add(bottomPanel, BorderLayout.SOUTH);
        updateUserButton();

    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/assets/bg.png"));
        } catch (Exception e) {
            backgroundImage = null;
        }
    }

    private JButton minimalistMenuButton(String text, Font font, int radius, int thickness) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(font);
        btn.setBackground(new Color(34, 36, 44));
        btn.setForeground(new Color(220, 225, 239));
        btn.setPreferredSize(new Dimension(320, 60));
        btn.setMaximumSize(new Dimension(340, 64));
        btn.setMinimumSize(new Dimension(160, 44));
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedBorder(radius, new Color(64, 64, 90), thickness));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(56, 59, 76));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(34, 36, 44));
            }
        });
        return btn;
    }

    private JButton minimalistUserButton(Font font, int radius, int thickness) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setFont(font);
        btn.setBackground(new Color(34, 36, 44, 160));
        btn.setForeground(new Color(225, 230, 240));
        btn.setPreferredSize(new Dimension(180, 38));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedBorder(radius, new Color(94, 104, 120), thickness));
        btn.setToolTipText("Account");
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(54, 47, 76, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(34, 36, 44, 160));
            }
        });
        btn.addActionListener(e -> handleLoginLogout());
        return btn;
    }

    // Thicker, rounded border class
    static class RoundedBorder implements javax.swing.border.Border {
        private final int radius;
        private final Color color;
        private final int thickness;

        RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(thickness));
            g2.setColor(color);
            g2.drawRoundRect(x + thickness/2, y + thickness/2, w-thickness, h-thickness, radius, radius);
            g2.dispose();
        }
    }

    private void handleLoginLogout() {
        if (gameMain.getUsername() == null || gameMain.getUsername().isEmpty()) {
            new LoginWindow();
            SwingUtilities.getWindowAncestor(this).dispose();
        } else {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Logout from " + gameMain.getUsername() + "?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                gameMain.dispose();
                SwingUtilities.invokeLater(LoginWindow::new);
            }
        }
        updateLoginButton();
        updateUserButton();
    }

    private void updateLoginButton() {
        if (gameMain.getUsername() != null && !gameMain.getUsername().isEmpty()) {
            loginButton.setText("ACCOUNT: " + gameMain.getUsername().toUpperCase());
        } else {
            loginButton.setText("LOGIN");
        }
    }

    private void updateUserButton() {
        if (gameMain.getUsername() != null && !gameMain.getUsername().isEmpty()) {
            userButton.setText("👤 " + gameMain.getUsername());
            userButton.setVisible(true);
        } else {
            userButton.setText("");
            userButton.setVisible(false);
        }
    }


    private void startAnimation() {
        animationTimer = new Timer(30, e -> {
            titleScale += scaleDirection;
            if (titleScale > 1.04 || titleScale < 0.97) scaleDirection *= -1;
            backgroundYOffset += 1;
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
        Graphics2D g2d = (Graphics2D) g.create();

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, backgroundYOffset, getWidth(), getHeight(), this);
            g2d.drawImage(backgroundImage, 0, backgroundYOffset - getHeight(), getWidth(), getHeight(), this);
        } else {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Animated title shadow (centered, visually balanced)
        String title = "TRIGGER TRACKER";
        Font baseFont = new Font("Segoe UI Black", Font.BOLD, 85);
        Font scaledFont = baseFont.deriveFont((float) (baseFont.getSize() * (float)titleScale));
        FontMetrics fm = g2d.getFontMetrics(scaledFont);
        int titleWidth = fm.stringWidth(title);
        int x = (getWidth() - titleWidth) / 2;
        int y = getHeight() / 2 - 110;

        g2d.setFont(scaledFont);
        g2d.setColor(new Color(0,0,0, 88));
        g2d.drawString(title, x + 4, y + 8);
        g2d.setColor(new Color(210, 225, 247));
        g2d.drawString(title, x, y);
        g2d.dispose();
    }
}
