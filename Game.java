import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Main launcher for the game.
 */
public class Game {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameMain());
    }
}

/**
 * Main window of the game.
 */
class GameMain extends JFrame {
    private StartMenuPanel startMenuPanel;
    private GamePanel gamePanel;

    public GameMain() {
        super("Space Survivor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        SpriteManager.loadSprites();
        showStartMenu();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showStartMenu() {
        if (gamePanel != null) {
            getContentPane().remove(gamePanel);
            gamePanel.stopGame();
            gamePanel = null;
        }

        startMenuPanel = new StartMenuPanel(this);
        getContentPane().add(startMenuPanel);
        pack();
        revalidate();
        repaint();
    }

    public void showGamePanel() {
        if (startMenuPanel != null) {
            getContentPane().remove(startMenuPanel);
            startMenuPanel = null;
        }

        gamePanel = new GamePanel(this);
        getContentPane().add(gamePanel);
        pack();
        revalidate();
        repaint();
        gamePanel.startGame();
    }
}

/**
 * Animated Start Menu Panel.
 */
class StartMenuPanel extends JPanel {
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

/**
 * Game Panel with game logic.
 */
class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private GameMain mainFrame;

    private int score = 0;
    private boolean gameOver = false;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private int mouseX, mouseY;

    public GamePanel(GameMain mainFrame) {
        this.mainFrame = mainFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        player = new Player(WIDTH / 2, HEIGHT / 2);

        // Key listener (this)
        addKeyListener(this);

        // Mouse move tracking
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        // Mouse click to shoot
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // offset from center toward front of ship
                double bulletX = player.x + player.width / 2 + Math.cos(player.angle) * player.width / 2;
                double bulletY = player.y + player.height / 2 + Math.sin(player.angle) * player.height / 2;

                double angle = Math.atan2(
                    e.getY() - (player.y + player.height / 2.0),
                    e.getX() - (player.x + player.width / 2.0)
                );

                bullets.add(new Bullet(bulletX - 2, bulletY - 2, angle)); // -3 centers bullet
            }
        });
        
        timer = new Timer(16, this); // ~60fps
    }

    public void startGame() {
        // reset state if needed
        bullets.clear();
        enemies.clear();
        score = 0;
        gameOver = false;
        player.x = WIDTH / 2.0;
        player.y = HEIGHT / 2.0;
        player.health = player.maxHealth;
        timer.start();
        requestFocusInWindow(); // Ensure focus for key events
    }
    
    public void stopGame() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // update player (rotation uses current mouse pos)
            player.update(mouseX, mouseY);

            // update bullets (safe removal using index loop)
            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet b = bullets.get(i);
                b.update();
                if (b.isOffScreen(WIDTH, HEIGHT)) bullets.remove(i);
            }

            // spawn enemies randomly from any edge (low chance each frame)
            if (Math.random() < 0.02) {
                spawnEnemyAtEdge();
            }

            // update enemies and make them chase player
            for (Enemy en : enemies) {
                en.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);
            }

            // bullet-enemy collisions (safe removal with iterators)
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy en = enemies.get(i);
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);

                boolean destroyed = false;
                for (int j = bullets.size() - 1; j >= 0; j--) {
                    Bullet b = bullets.get(j);
                    Rectangle bulletRect = new Rectangle((int) b.x, (int) b.y, b.size, b.size);
                    if (enemyRect.intersects(bulletRect)) {
                        // remove both
                        enemies.remove(i);
                        bullets.remove(j);
                        score += 10;
                        destroyed = true;
                        break;
                    }
                }
                if (destroyed) continue;
            }

            // player-enemy collision & enemy reaches bottom -> game over
            Rectangle playerRect = new Rectangle((int) player.x, (int) player.y, player.width, player.height);
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy en = enemies.get(i);
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);
                if (enemyRect.intersects(playerRect)) {
                    player.takeDamage(20); // lose 20 HP per hit (tweak as you like)

                    // remove the enemy so it doesn't keep draining health every frame
                    enemies.remove(i);
                    break;
                }
                if (en.y > HEIGHT) {
                    gameOver = true;
                    break;
                }
            }

            // check if player health is 0
            if (player.health <= 0) {
                gameOver = true;
            }

            if (gameOver) {
                timer.stop();
            }
        }

        repaint();
    }

    private void spawnEnemyAtEdge() {
        // choose random edge: 0=top,1=right,2=bottom,3=left
        int edge = (int) (Math.random() * 4);
        int ex = 0, ey = 0;
        switch (edge) {
            case 0: // top
                ex = (int) (Math.random() * (WIDTH - 40));
                ey = -40;
                break;
            case 1: // right
                ex = WIDTH;
                ey = (int) (Math.random() * (HEIGHT - 40));
                break;
            case 2: // bottom
                ex = (int) (Math.random() * (WIDTH - 40));
                ey = HEIGHT;
                break;
            case 3: // left
                ex = -40;
                ey = (int) (Math.random() * (HEIGHT - 40));
                break;
        }
        enemies.add(new Enemy(ex, ey));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        if (SpriteManager.bgSprite != null) {
            g.drawImage(SpriteManager.bgSprite, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // draw player
        player.draw(g);

        // draw bullets
        for (Bullet b : bullets) b.draw(g);

        // draw enemies
        for (Enemy en : enemies) {
            en.draw(g, player); // pass player so they rotate toward player
        }

        // HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 20);

        // Health bar
        int barWidth = 150, barHeight = 20;
        int xPos = WIDTH - barWidth - 20, yPos = 20;
        g.setColor(Color.GRAY);
        g.fillRect(xPos, yPos, barWidth, barHeight);
        g.setColor(Color.RED);
        int healthWidth = (int)((player.health / (double)player.maxHealth) * barWidth);
        g.fillRect(xPos, yPos, healthWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(xPos, yPos, barWidth, barHeight);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER to restart", WIDTH / 2 - 110, HEIGHT / 2 + 40);
        }
    }

    // key listener methods (movement + restart)
    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
        // restart on Enter
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            startGame();
        }
        // return to menu on ESC
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            stopGame();
            mainFrame.showStartMenu();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

/**
 * Player class
 */
class Player {
    double x, y;
    int width = 40, height = 20;
    double angle = 0;
    int health = 100;
    int maxHealth = 100;
    boolean up, down, left, right;
    double speed = 4.0;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY) {
        // Update angle to face mouse
        angle = Math.atan2(mouseY - (y + height/2.0), mouseX - (x + width/2.0));

        // Movement
        if (up) y -= speed;
        if (down) y += speed;
        if (left) x -= speed;
        if (right) x += speed;

        // Keep player on screen
        x = Math.max(0, Math.min(GamePanel.WIDTH - width, x));
        y = Math.max(0, Math.min(GamePanel.HEIGHT - height, y));
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Save original transform
        AffineTransform old = g2d.getTransform();
        
        // Rotate to face mouse
        g2d.rotate(angle, x + width/2, y + height/2);
        
        // Draw player ship
        g2d.setColor(Color.CYAN);
        g2d.fillRect((int)x, (int)y, width, height);
        
        // Draw a triangle at the front for the ship's nose
        int[] xPoints = {(int)x + width, (int)x + width + 15, (int)x + width};
        int[] yPoints = {(int)y, (int)y + height/2, (int)y + height};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Restore transform
        g2d.setTransform(old);
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = false;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
}

/**
 * Bullet class
 */
class Bullet {
    double x, y;
    double dx, dy;
    int size = 4;
    double speed = 10.0;

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public boolean isOffScreen(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)x, (int)y, size, size);
    }
}

/**
 * Enemy class
 */
class Enemy {
    double x, y;
    int width = 30, height = 30;
    double speed = 2.0;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void chase(double targetX, double targetY) {
        double dx = targetX - (x + width/2);
        double dy = targetY - (y + height/2);
        double dist = Math.sqrt(dx*dx + dy*dy);
        
        if (dist > 0) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }
    }

    public void draw(Graphics g, Player player) {
        // Calculate angle to face player
        double angle = Math.atan2(
            (player.y + player.height/2) - (y + height/2),
            (player.x + player.width/2) - (x + width/2)
        );
        
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();
        
        // Rotate to face player
        g2d.rotate(angle, x + width/2, y + height/2);
        
        // Draw enemy
        g2d.setColor(Color.RED);
        g2d.fillRect((int)x, (int)y, width, height);
        
        // Draw a triangle at the front
        int[] xPoints = {(int)x + width, (int)x + width + 10, (int)x + width};
        int[] yPoints = {(int)y, (int)y + height/2, (int)y + height};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        g2d.setTransform(old);
    }
}

/**
 * Sprite manager with background loading.
 */
class SpriteManager {
    public static BufferedImage bgSprite;
    
    public static void loadSprites() {
        try {
            // Try to load from file
            bgSprite = ImageIO.read(new File("bg.png"));
            System.out.println("Loaded background sprite from file");
        } catch (IOException e) {
            System.err.println("Could not load 'bg.png' from file. Creating a default background.");
            // Create a default background if file loading fails
            bgSprite = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bgSprite.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 800, 600);
            
            // Add some stars
            g.setColor(Color.WHITE);
            for (int i = 0; i < 100; i++) {
                int x = (int)(Math.random() * 800);
                int y = (int)(Math.random() * 600);
                g.fillRect(x, y, 2, 2);
            }
            g.dispose();
        }
    }
} 