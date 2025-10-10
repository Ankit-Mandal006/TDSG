import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // === From GameMain ===
    private final GameMain mainFrame;
    private final String username;
    private final DatabaseManager db;

    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;

    private int score;
    private boolean gameOver;
    private boolean savedStatsOnGameOver = false; // avoid double DB writes

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private ArrayList<Explosion> explosions = new ArrayList<>();
    private int screenshakeFrames = 0;

    private int mouseX, mouseY;

    // === Constructor exactly matching GameMain call ===
    public GamePanel(GameMain mainFrame, String username, DatabaseManager db) {
        this.mainFrame = mainFrame;
        this.username = username;
        this.db = db;

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
            @Override public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        // Shoot bullets on mouse click
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                double bulletX = player.x + player.width / 2 + Math.cos(player.angle) * player.width / 2;
                double bulletY = player.y + player.height / 2 + Math.sin(player.angle) * player.height / 2;

                double angle = Math.atan2(
                        e.getY() - (player.y + player.height / 2.0),
                        e.getX() - (player.x + player.width / 2.0)
                );

                bullets.add(new Bullet(bulletX - 2, bulletY - 2, angle));
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
        savedStatsOnGameOver = false;
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
                        // Spawn explosion at center of enemy
                        explosions.add(new Explosion(en.x + en.width / 2.0, en.y + en.height / 2.0));
                        screenshakeFrames = 12; // shake for some frames
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
                    // Spawn explosion at center of enemy
                    explosions.add(new Explosion(en.x + en.width / 2.0, en.y + en.height / 2.0));
                    screenshakeFrames = 12; // shake for some frames
                    enemies.remove(i);
                }

            }
            
            // Update explosions
            for (int i = explosions.size() - 1; i >= 0; i--) {
                explosions.get(i).age++;
                if (!explosions.get(i).isAlive())
                    explosions.remove(i);
            }

            // Update screenshake
            if (screenshakeFrames > 0) screenshakeFrames--;

            
            // Game Over check
            if (player.health <= 0) {
                gameOver = true;
            }

            if (gameOver && !savedStatsOnGameOver) {
                savedStatsOnGameOver = true;
                handleGameOverAndSave();
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
            case 0: ex = (int)(Math.random() * (WIDTH - 40)); ey = -40; break;          // top (spawn just above)
            case 1: ex = WIDTH;                       ey = (int)(Math.random() * (HEIGHT - 40)); break; // right (off-screen)
            case 2: ex = (int)(Math.random() * (WIDTH - 40)); ey = HEIGHT; break;        // bottom
            case 3: ex = -40;                         ey = (int)(Math.random() * (HEIGHT - 40)); break; // left
        }
        enemies.add(new Enemy(ex, ey));
    }

    // === Persist stats to DB at game over ===
    private void handleGameOverAndSave() {
        if (db != null && username != null && !username.equalsIgnoreCase("Guest")) {
            db.updateHighscore(username, score);

            int coinsEarned = score / 10;
            if (coinsEarned > 0) {
                db.updateCurrency(username, coinsEarned);
            }
        }
        // Optionally, you can display a message or skip DB logic for guests
    }


    // === Rendering ===
    @Override
    public void paintComponent(Graphics g) {
    	// Screen shake
    	int shakeX = 0, shakeY = 0;
    	if (screenshakeFrames > 0) {
    	    shakeX = (int)(Math.random() * 8 - 4);
    	    shakeY = (int)(Math.random() * 8 - 4);
    	}
    	Graphics2D g2 = (Graphics2D) g.create();
    	g2.translate(shakeX, shakeY);

    	// Draw the background, player, bullets, enemies, etc. (replace all 'g' with 'g2')

    	
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
        g.drawString("Player: " + username, 10, 20);
        g.drawString("Score: " + score, 10, 45);

        // Health Bar (top-right)
        int barWidth = 150, barHeight = 20;
        int xPos = WIDTH - barWidth - 20, yPos = 20;
        g.setColor(Color.GRAY);
        g.fillRect(xPos, yPos, barWidth, barHeight);
        g.setColor(Color.RED);
        int healthWidth = (int)((player.health / (double)player.maxHealth) * barWidth);
        g.fillRect(xPos, yPos, healthWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(xPos, yPos, barWidth, barHeight);
        
        // Draw explosions
        for (Explosion ex : explosions) {
            float alpha = 1.0f - (ex.age / (float)ex.duration);
            if (SpriteManager.blastSprite != null) {
                // Draw blast png/gif with transparency
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                int size = 60;
                g2.drawImage(SpriteManager.blastSprite, (int)ex.x - size/2, (int)ex.y - size/2, size, size, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // reset
            } else {
                // Fallback: red circle fading out
                g2.setColor(new Color(255, 100, 0, (int)(180 * alpha)));
                g2.fillOval((int)ex.x - 32, (int)ex.y - 32, 64, 64);
            }
        }
        g2.dispose();


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
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            startGame(); // resets health & score and restarts
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { player.keyReleased(e); }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public boolean isFocusable() { return true; }
}
