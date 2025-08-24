import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;

    private int score;
    private boolean gameOver;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private int mouseX, mouseY;
    private GameMain mainFrame;

    // === Constructor ===
    public GamePanel(GameMain mainFrame) {
        this.mainFrame = mainFrame;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        player = new Player(WIDTH / 2, HEIGHT / 2);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();

        addKeyListener(this);

        // Track mouse position for rotation
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        // Shoot bullets on mouse click
        addMouseListener(new MouseAdapter() {
            @Override
public void mousePressed(MouseEvent e) {
    double bulletX = player.x + player.width / 2 + Math.cos(player.angle) * player.width / 2;
    double bulletY = player.y + player.height / 2 + Math.sin(player.angle) * player.height / 2;

    double angle = Math.atan2(
            e.getY() - (player.y + player.height / 2.0),
            e.getX() - (player.x + player.width / 2.0)
    );

    Bullet newBullet = new Bullet(bulletX - 2, bulletY - 2, angle);
    bullets.add(newBullet);

    // ✅ Debug
    System.out.println("Bullet fired at: (" + bulletX + "," + bulletY + ") angle=" + angle 
                        + " | bullets=" + bullets.size());
}

        });

        timer = new Timer(16, this); // ~60 FPS
    }

    // === Start / Stop ===
    public void startGame() {
        bullets.clear();
        enemies.clear();
        score = 0;
        gameOver = false;
        player.x = WIDTH / 2.0;
        player.y = HEIGHT / 2.0;
        player.health = player.maxHealth;
        timer.start();
    }

    public void stopGame() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    // === Game Loop ===
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Update player
            player.update(mouseX, mouseY);

            // Update bullets
            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet b = bullets.get(i);
                b.update();
                if (b.isOffScreen(WIDTH, HEIGHT)) bullets.remove(i);
            }

            // Random enemy spawn
            if (Math.random() < 0.02) {
                spawnEnemyAtEdge();
            }

            // Update enemies
            for (Enemy en : enemies) {
                en.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);
            }

            // Bullet-Enemy collision
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy en = enemies.get(i);
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);

                for (int j = bullets.size() - 1; j >= 0; j--) {
                    Bullet b = bullets.get(j);
                    Rectangle bulletRect = new Rectangle((int) b.x, (int) b.y, b.size, b.size);
                    if (enemyRect.intersects(bulletRect)) {
                        enemies.remove(i);
                        bullets.remove(j);
                        score += 10;
                        break;
                    }
                }
            }

            // Player-Enemy collision
            Rectangle playerRect = new Rectangle((int) player.x, (int) player.y, player.width, player.height);
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy en = enemies.get(i);
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);

                if (enemyRect.intersects(playerRect)) {
                    player.takeDamage(20);
                    enemies.remove(i);
                }
            }

            // Game Over check
            if (player.health <= 0) {
                gameOver = true;
                timer.stop();
            }
        }
        repaint();
    }

    // === Enemy Spawning ===
    private void spawnEnemyAtEdge() {
        int edge = (int)(Math.random() * 4);
        int ex = 0, ey = 0;
    
        switch (edge) {
            case 0: ex = (int)(Math.random() * (WIDTH - 40)); ey = 0; break;        // top
            case 1: ex = WIDTH - 40; ey = (int)(Math.random() * (HEIGHT - 40)); break; // right
            case 2: ex = (int)(Math.random() * (WIDTH - 40)); ey = HEIGHT - 40; break; // bottom
            case 3: ex = 0; ey = (int)(Math.random() * (HEIGHT - 40)); break;        // left
        }
        enemies.add(new Enemy(ex, ey));
    }
    

    // === Rendering ===
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        if (SpriteManager.bgSprite != null) {
            g.drawImage(SpriteManager.bgSprite, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Player
        player.draw(g);

        // Bullets
        for (Bullet b : bullets) b.draw(g);

        // Enemies
        for (Enemy en : enemies) en.draw(g, player);

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 20);

        // Health Bar
        int barWidth = 150, barHeight = 20;
        int xPos = WIDTH - barWidth - 20, yPos = 20;
        g.setColor(Color.GRAY);
        g.fillRect(xPos, yPos, barWidth, barHeight);
        g.setColor(Color.RED);
        int healthWidth = (int)((player.health / (double)player.maxHealth) * barWidth);
        g.fillRect(xPos, yPos, healthWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(xPos, yPos, barWidth, barHeight);

        // Game Over Text
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER to restart", WIDTH / 2 - 110, HEIGHT / 2 + 40);
        }
    }

    // === Input ===
    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) startGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public boolean isFocusable() {
        return true;
    }
}
