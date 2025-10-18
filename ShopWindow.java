import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ShopWindow extends JFrame {
    private final DatabaseManager db;
    private final String username;
    private JLabel currencyLabel;

    public ShopWindow(String username, DatabaseManager db) {
        super("Shop - Player Skins");
        this.username = username;
        this.db = db;

        setSize(900, 560);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel content = new JPanel();
        content.setBackground(new Color(24, 26, 32));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 36, 36, 36));
        setContentPane(content);

        JLabel title = new JLabel("SKIN VAULT");
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 44));
        title.setForeground(new Color(235, 240, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel skinPanelHolder = new JPanel(new GridLayout(1, 4, 26, 0));
        skinPanelHolder.setOpaque(false);

        skinPanelHolder.add(createSkinPanel("Default", "default", 0, SpriteManager.playerDefaultSprite));
        skinPanelHolder.add(createSkinPanel("Player2", "skin1", 100, SpriteManager.playerSkin1));
        skinPanelHolder.add(createSkinPanel("Player3", "skin2", 200, SpriteManager.playerSkin2));
        skinPanelHolder.add(createSkinPanel("Player4", "skin3", 300, SpriteManager.playerSkin3));

        content.add(skinPanelHolder);
        content.add(Box.createVerticalGlue());

        currencyLabel = new JLabel();
        currencyLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        currencyLabel.setForeground(new Color(230, 240, 255));
        currencyLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        updateCurrencyLabel();
        content.add(currencyLabel);

        content.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton backBtn = new JButton("Back");
        stylizeButton(backBtn, 20, 4);
        backBtn.setMaximumSize(new Dimension(150, 52));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> dispose());
        content.add(backBtn);

        setVisible(true);
    }

    private JPanel createSkinPanel(String skinName, String skinId, int price, BufferedImage image) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(36, 38, 52));
        panel.setBorder(new RoundedBorder(24, new Color(80, 100, 120, 120), 4));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(200, 380));

        boolean purchased = skinId.equals("default") || db.isSkinPurchased(username, skinId);
        String equipped = db.getSelectedSkin(username);
        int userCoins = db.getCurrency(username);

        // Image panel with optional lock overlay
        JPanel imagePanel = new JPanel();
        imagePanel.setOpaque(false);
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setMaximumSize(new Dimension(120, 120));
        imagePanel.setPreferredSize(new Dimension(120, 120));

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (image != null) {
            ImageIcon scaledIcon = new ImageIcon(image.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
            imgLabel.setIcon(scaledIcon);
        } else {
            imgLabel.setText("NO IMAGE");
            imgLabel.setForeground(Color.WHITE);
        }
        imagePanel.add(imgLabel, BorderLayout.CENTER);

        // Add lock emoji or text if skin is locked
        if (!purchased && price > 0) {
            JLabel lockLabel = new JLabel("🔒");
            lockLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            lockLabel.setForeground(new Color(255, 255, 255, 200));
            lockLabel.setHorizontalAlignment(SwingConstants.CENTER);
            lockLabel.setVerticalAlignment(SwingConstants.CENTER);
            imagePanel.add(lockLabel, BorderLayout.CENTER);
        }

        panel.add(Box.createRigidArea(new Dimension(0, 26)));
        panel.add(imagePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel nameLabel = new JLabel(skinName);
        nameLabel.setForeground(new Color(230, 235, 255));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel priceLabel = new JLabel();
        if (price == 0) {
            priceLabel.setText("Default skin");
        } else if (purchased) {
            priceLabel.setText("Owned");
        } else if (userCoins < price) {
            priceLabel.setText("🔒 Locked");
        } else {
            priceLabel.setText("Price: " + price + " coins");
        }
        priceLabel.setForeground(new Color(200, 210, 225));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(priceLabel);

        panel.add(Box.createVerticalGlue());

        JButton buyBtn = new JButton();
        stylizeButton(buyBtn, 18, 4);
        buyBtn.setMaximumSize(new Dimension(160, 40));
        buyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (purchased) {
            if (equipped != null && equipped.equalsIgnoreCase(skinId)) {
                buyBtn.setText("Selected");
                buyBtn.setEnabled(false);
            } else {
                buyBtn.setText("Select");
                buyBtn.setEnabled(true);
                buyBtn.addActionListener(e -> {
                    db.setSelectedSkin(username, skinId);
                    SpriteManager.selectedPlayerSprite = image;
                    JOptionPane.showMessageDialog(this, skinName + " skin equipped!");
                    dispose();
                });
            }
        } else {
            buyBtn.setText("Buy");
            if (userCoins < price) {
                buyBtn.setEnabled(false);
            } else {
                buyBtn.addActionListener(e -> {
                    if (db.getCurrency(username) >= price) {
                        db.buySkin(username, skinId, price);
                        db.setSelectedSkin(username, skinId);
                        SpriteManager.selectedPlayerSprite = image;
                        updateCurrencyLabel();
                        JOptionPane.showMessageDialog(this, skinName + " purchased and equipped!");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Not enough coins!");
                    }
                });
            }
        }

        panel.add(buyBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 26)));

        return panel;
    }

    private void updateCurrencyLabel() {
        int coins = db.getCurrency(username);
        currencyLabel.setText("💰 Coins: " + coins);
    }

    private void stylizeButton(JButton btn, int radius, int thickness) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setOpaque(true);
        btn.setForeground(new Color(230, 245, 245));
        btn.setBackground(new Color(59, 59, 90));
        btn.setBorder(new RoundedBorder(radius, new Color(74, 95, 145, 180), thickness));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(86, 90, 130));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(59, 59, 90));
            }
        });
    }

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

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(thickness));
            g2.setColor(color);
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2, w - thickness, h - thickness, radius, radius);
            g2.dispose();
        }
    }
}
