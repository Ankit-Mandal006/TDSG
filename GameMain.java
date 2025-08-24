import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class StartMenuPanel extends JPanel {
    private GameMain gameMain;
    private BufferedImage backgroundImage;
    private Timer animationTimer;

    private double titleScale = 1.0;
    private double scaleDirection = 0.005;
    private int backgroundYOffset = 0;

    public StartMenuPanel(GameMain mainFrame) {
        this.gameMain = mainFrame;
        setPreferredSize(new Dimension(1024, 576));
        loadBackgroundImage();
        initComponents();
        startAnimation();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("menu_background.png"));
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

        menuItemsPanel.add(createMenuItem("New Game", () -> gameMain.showGamePanel()));
        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuItemsPanel.add(createMenuItem("Load Game", null));
        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuItemsPanel.add(createMenuItem("Options", null));
        menuItemsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuItemsPanel.add(createMenuItem("Quit", this::showExitConfirmation));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 300, 0, 0);
        add(menuItemsPanel, gbc);
    }

    private JLabel createMenuItem(String text, Runnable action) {
        Font defaultFont = new Font("Impact", Font.PLAIN, 40);
        JLabel label = new JLabel(text);
        label.setFont(defaultFont);
        label.setForeground(new Color(170, 210, 255));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            Font hoverFont = new Font("Impact", Font.BOLD, 42);

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.WHITE);
                label.setFont(hoverFont);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(new Color(170, 210, 255));
                label.setFont(defaultFont);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                label.setFont(new Font("Impact", Font.PLAIN, 38));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                label.setFont(hoverFont);
                if (action != null) action.run();
            }
        });
        return label;
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

        String title = "Space Survivor";
        Font baseFont = new Font("Impact", Font.BOLD, 90);
        Font scaledFont = baseFont.deriveFont((float) (baseFont.getSize() * titleScale));
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
