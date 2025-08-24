import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * A menu styled after modern games like Dragon Age.
 * Features a high-quality background and interactive text labels.
 */
public class StartMenuPanel extends JPanel {

    private GameMain gameMain;
    private BufferedImage backgroundImage;
    private Timer animationTimer;
    private double titleScale = 1.0;
    private double scaleDirection = 0.005;

    public StartMenuPanel(GameMain mainFrame) {
        this.gameMain = mainFrame;
        // Set the panel size to match the background image aspect ratio
        setPreferredSize(new Dimension(1024, 576));

        loadBackgroundImage();
        initComponents();
        startAnimation();
    }

    private void loadBackgroundImage() {
        try {
            // Make sure your new background image is named this and is in the 'res' folder
            URL imageUrl = getClass().getResource("/res/menu_background.jpg");
            if (imageUrl == null) {
                System.err.println("Error: Could not find background image at /res/menu_background.jpg");
                backgroundImage = null;
            } else {
                backgroundImage = ImageIO.read(imageUrl);
            }
        } catch (IOException e) {
            System.err.println("Error loading background image.");
            e.printStackTrace();
            backgroundImage = null;
        }
    }

    private void initComponents() {
        // Use GridBagLayout for flexible positioning
        setLayout(new GridBagLayout());

        // This panel will hold our menu labels (e.g., "New Game", "Quit")
        JPanel menuItemsPanel = new JPanel();
        menuItemsPanel.setOpaque(false); // Make the panel transparent
        // Use BoxLayout to stack our labels vertically
        menuItemsPanel.setLayout(new BoxLayout(menuItemsPanel, BoxLayout.Y_AXIS));

        // Create menu items using a helper method that adds style and actions
        JLabel newGameLabel = createMenuItem("New Game", () -> gameMain.showGamePanel());
        JLabel loadGameLabel = createMenuItem("Load Game", null); // 'null' action means it does nothing yet
        JLabel optionsLabel = createMenuItem("Options", null);
        JLabel quitLabel = createMenuItem("Quit", () -> System.exit(0));

        // Add the labels to our vertical menu panel
        menuItemsPanel.add(newGameLabel);
        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Adds a small space between items
        menuItemsPanel.add(loadGameLabel);
        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuItemsPanel.add(optionsLabel);
        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuItemsPanel.add(quitLabel);
        
        // --- Position the Menu Panel on the Main Screen ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Use weights to push the panel
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        // Use insets to push the menu to the right of the center point
        gbc.insets = new Insets(0, 300, 0, 0); 
        
        add(menuItemsPanel, gbc);
    }
    
    /**
     * Helper method to create a JLabel that acts as a menu item.
     * It styles the label and adds hover and click listeners.
     * @param text The text for the menu item.
     * @param action The code to run when the item is clicked.
     * @return A styled, interactive JLabel.
     */
    private JLabel createMenuItem(String text, Runnable action) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Serif", Font.PLAIN, 28));
        label.setForeground(Color.GRAY); // Default, unselected color
        label.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Show a hand pointer on hover
        
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.WHITE); // Highlight in white on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.GRAY); // Return to gray when mouse leaves
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.run(); // Run the assigned action on click
                }
            }
        });
        
        return label;
    }

    private void startAnimation() {
        animationTimer = new Timer(30, e -> {
            titleScale += scaleDirection;
            if (titleScale > 1.05 || titleScale < 0.95) {
                scaleDirection *= -1;
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the background image to fill the panel
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // --- Draw Animated Title (as a logo) ---
        String title = "Space Survivor";
        Font baseFont = new Font("Serif", Font.BOLD, 60);
        Font scaledFont = baseFont.deriveFont((float) (baseFont.getSize() * titleScale));
        
        g2d.setFont(scaledFont);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        // Position the title in the horizontal center, and vertically above the menu items
        int x = (getWidth() - titleWidth) / 2;
        int y = getHeight() / 2 - 100;
        
        g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black for shadow
        g2d.drawString(title, x + 4, y + 4);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, x, y);
    }
}