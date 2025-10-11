import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final GameMain mainFrame;
    private final String username;
    private final DatabaseManager db;

    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;
    public ArrayList<Enemy> enemies;
    public ArrayList<Boss> bosses;
    private ArrayList<Explosion> explosions;

    private int score;
    private boolean gameOver;
    private boolean savedStatsOnGameOver = false;
    private int screenshakeFrames = 0;

    public static final int WIDTH = 1000;
    public static final int HEIGHT = 700;

    private int mouseX, mouseY;

    private WaveManager waveManager;

    // Pause state variables
    private boolean paused = false;
    private int pauseSelection = 0; // 0 = Resume, 1 = Main Menu

    private int damageFlashFrames = 0;
    private static final int DAMAGE_FLASH_DURATION = 10;

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
        bosses = new ArrayList<>();
        explosions = new ArrayList<>();

        waveManager = new WaveManager();

        addKeyListener(this);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!paused && !gameOver) {
                    shootPlayerBullet(e.getX(), e.getY());
                }
            }
        });

        timer = new Timer(16, this); // ~60 FPS
    }

    private void shootPlayerBullet(int targetX, int targetY) {
        double centerX = player.x + player.width / 2.0;
        double centerY = player.y + player.height / 2.0;
        double angle = Math.atan2(targetY - centerY, targetX - centerX);

        double bulletX = centerX + Math.cos(angle) * player.width / 2.0 - 2;
        double bulletY = centerY + Math.sin(angle) * player.height / 2.0 - 2;

        bullets.add(new Bullet(bulletX, bulletY, angle, SpriteManager.bulletSprite));
    }

    public void triggerDamageFlash() {
        damageFlashFrames = DAMAGE_FLASH_DURATION;
    }

    public void startGame() {
        bullets.clear();
        enemies.clear();
        bosses.clear();
        explosions.clear();

        score = 0;
        gameOver = false;
        savedStatsOnGameOver = false;
        paused = false;

        player.x = WIDTH / 2.0;
        player.y = HEIGHT / 2.0;
        player.health = player.maxHealth;

        waveManager.startWave(1, this);

        timer.start();
    }

    public void stopGame() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) {
            updateGameLogic();
        } else if (gameOver && !savedStatsOnGameOver) {
            savedStatsOnGameOver = true;
            saveStatsAndStop();
        }
        repaint();
    }

    private void updateGameLogic() {
        waveManager.updateWave(this);

        player.update(mouseX, mouseY);

        if (damageFlashFrames > 0) {
            damageFlashFrames--;
        }

        updateBullets();
        updateBosses();
        updateEnemies();
        handleBulletEnemyCollisions();
        handleEnemyPlayerCollisions();
        updateExplosions();

        if (screenshakeFrames > 0) {
            screenshakeFrames--;
        }

        if (player.health <= 0) {
            gameOver = true;
            bosses.clear();
        }
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (b.isOffScreen(WIDTH, HEIGHT)) {
                bullets.remove(i);
            }
        }
    }

    private void updateBosses() {
        for (int i = bosses.size() - 1; i >= 0; i--) {
            Boss b = bosses.get(i);
            b.update();
            b.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);

            // Bullets vs Boss collision
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                if (b.getBounds().intersects(bullet.getBounds())) {
                    b.takeDamage(10);
                    bullets.remove(j);
                    explosions.add(new Explosion(b.x + b.width / 2.0, b.y + b.height / 2.0));
                    screenshakeFrames = 15;
                }
            }

            // Boss Orbs vs Player collision
            for (int j = b.orbs.size() - 1; j >= 0; j--) {
                Boss.Orb orb = b.orbs.get(j);
                if (new Rectangle((int) player.x, (int) player.y, player.width, player.height)
                        .intersects(orb.getBounds())) {
                    player.takeDamage(10);
                    triggerDamageFlash();
                    b.orbs.remove(j);
                }
            }

            if (b.isDead()) {
                bosses.remove(i);
                score += 100;
            }
        }
    }

    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            if (en instanceof RangedShooterEnemy) {
                RangedShooterEnemy rse = (RangedShooterEnemy) en;
                rse.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);
                rse.update(player, WIDTH, HEIGHT);
                rse.checkBulletCollision(player);
            } else {
                en.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);
            }
        }
    }

    private void handleBulletEnemyCollisions() {
        outer:
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet b = bullets.get(j);
                if (enemyRect.intersects(b.getBounds())) {
                    explosions.add(new Explosion(en.x + en.width / 2.0, en.y + en.height / 2.0));
                    screenshakeFrames = 12;
                    enemies.remove(i);
                    bullets.remove(j);
                    score += 10;
                    continue outer;
                }
            }
        }
    }

    private void handleEnemyPlayerCollisions() {
        Rectangle playerRect = new Rectangle((int) player.x, (int) player.y, player.width, player.height);
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);
            if (enemyRect.intersects(playerRect)) {
                player.takeDamage(20);
                triggerDamageFlash();
                explosions.add(new Explosion(en.x + en.width / 2.0, en.y + en.height / 2.0));
                screenshakeFrames = 12;
                enemies.remove(i);
            }
        }
    }

    private void updateExplosions() {
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion ex = explosions.get(i);
            ex.age++;
            if (!ex.isAlive()) {
                explosions.remove(i);
            }
        }
    }

    private void saveStatsAndStop() {
        if (db != null && username != null && !username.equalsIgnoreCase("Guest")) {
            db.updateHighscore(username, score);
            int coinsEarned = score / 10;
            if (coinsEarned > 0) {
                db.updateCurrency(username, coinsEarned);
            }
        }
        timer.stop();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int shakeX = 0, shakeY = 0;
        if (screenshakeFrames > 0) {
            shakeX = (int) (Math.random() * 8 - 4);
            shakeY = (int) (Math.random() * 8 - 4);
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(shakeX, shakeY);

        // Draw background
        if (SpriteManager.bgSprite != null) {
            g2.drawImage(SpriteManager.bgSprite, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Draw game entities
        player.draw(g2);

        for (Bullet b : bullets) b.draw(g2);
        for (Enemy en : enemies) {
            en.draw(g2, player);
            if (en instanceof RangedShooterEnemy) {
                ((RangedShooterEnemy) en).drawBullets(g2);
            }
        }
        for (Boss b : bosses) b.draw(g2);

        // Draw explosions with fade
        for (Explosion ex : explosions) {
            float alpha = 1.0f - ((float) ex.age / ex.duration);
            if (SpriteManager.blastSprite != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                int size = 60;
                g2.drawImage(SpriteManager.blastSprite, (int) ex.x - size / 2, (int) ex.y - size / 2, size, size, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                g2.setColor(new Color(255, 100, 0, (int) (180 * alpha)));
                g2.fillOval((int) ex.x - 32, (int) ex.y - 32, 64, 64);
            }
        }

        // Draw HUD
        drawPlayerHUD(g2);

        if (!bosses.isEmpty()) drawCombinedBossHealthBar(g2);

        // Draw game over overlay
        if (gameOver) drawGameOverOverlay(g2);

        // Draw damage flash
        if (damageFlashFrames > 0) {
            float alpha = damageFlashFrames / (float) DAMAGE_FLASH_DURATION * 0.5f;
            Graphics2D flash = (Graphics2D) g.create();
            flash.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            flash.setColor(Color.RED);
            flash.fillRect(0, 0, WIDTH, HEIGHT);
            flash.dispose();
        }

        // Draw pause menu
        if (paused) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            int boxWidth = 300, boxHeight = 150;
            int boxX = (WIDTH - boxWidth) / 2, boxY = (HEIGHT - boxHeight) / 2;

            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);

            String[] options = {"Resume", "Main Menu"};
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            int optionYStart = boxY + 50;

            for (int i = 0; i < options.length; i++) {
                int optionX = boxX + 50;
                int optionY = optionYStart + i * 40;
                if (pauseSelection == i) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString("▶ " + options[i], optionX, optionY);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(options[i], optionX + 24, optionY);
                }
            }

            g2d.dispose();
        }

        g2.dispose();
    }

    private void drawPlayerHUD(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Player: " + username, 10, 20);
        g2.drawString("Score: " + score, 10, 45);

        int barWidth = 150, barHeight = 20;
        int xPos = WIDTH - barWidth - 20, yPos = 20;

        g2.setColor(Color.GRAY);
        g2.fillRect(xPos, yPos, barWidth, barHeight);

        g2.setColor(Color.RED);
        int healthWidth = (int) ((player.health / (double) player.maxHealth) * barWidth);
        g2.fillRect(xPos, yPos, healthWidth, barHeight);

        g2.setColor(Color.WHITE);
        g2.drawRect(xPos, yPos, barWidth, barHeight);
    }

    private void drawCombinedBossHealthBar(Graphics2D g2) {
        int totalMaxHealth = bosses.stream().mapToInt(b -> b.maxHealth).sum();
        int totalHealth = bosses.stream().mapToInt(b -> b.health).sum();

        int barWidth = 400, barHeight = 20;
        int xPos = (WIDTH - barWidth) / 2, yPos = HEIGHT - barHeight - 20;

        g2.setColor(Color.GRAY);
        g2.fillRect(xPos, yPos, barWidth, barHeight);

        g2.setColor(Color.RED);
        int healthWidth = (int) ((totalHealth / (double) totalMaxHealth) * barWidth);
        g2.fillRect(xPos, yPos, healthWidth, barHeight);

        g2.setColor(Color.WHITE);
        g2.drawRect(xPos, yPos, barWidth, barHeight);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Boss Health", xPos + 10, yPos - 5);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        g2.setColor(new Color(255, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g2.getFontMetrics();
        String msg = "GAME OVER";
        int msgX = (WIDTH - fm.stringWidth(msg)) / 2;
        int msgY = HEIGHT / 2;
        g2.drawString(msg, msgX, msgY);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String restartMsg = "Press ENTER to restart";
        int restartX = (WIDTH - g2.getFontMetrics().stringWidth(restartMsg)) / 2;
        g2.drawString(restartMsg, restartX, msgY + 40);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Handle pause toggle
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            paused = !paused;
            if (paused) {
                timer.stop();
                pauseSelection = 0; // default to Resume
            } else {
                timer.start();
            }
            repaint();
            return; // skip further processing this event
        }

        if (paused) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> pauseSelection = (pauseSelection + 1) % 2;
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> pauseSelection = (pauseSelection + 1) % 2;
                case KeyEvent.VK_ENTER -> {
                    if (pauseSelection == 0) {
                        paused = false;
                        timer.start();
                    } else if (pauseSelection == 1) {
                        timer.stop();
                        mainFrame.showStartMenu(); // Implement in your GameMain class
                    }
                }
            }
            repaint();
            return;
        }

        if (!gameOver) {
            player.keyPressed(e);
        }

        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            startGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!paused && !gameOver) {
            player.keyReleased(e);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public boolean isFocusable() {
        return true;
    }
}
