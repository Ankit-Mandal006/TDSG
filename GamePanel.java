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
    private boolean savedStatsOnGameOver = false;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private ArrayList<Explosion> explosions = new ArrayList<>();
    private int screenshakeFrames = 0;

    private Boss boss;
    private boolean bossActive = false;

    private int mouseX, mouseY;

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

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

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

        timer = new Timer(16, this);
    }

    public void startGame() {
        bullets.clear();
        enemies.clear();
        explosions.clear();
        score = 0;
        gameOver = false;
        savedStatsOnGameOver = false;
        bossActive = false;
        boss = null;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Spawn boss if score >= 50 and boss not already active
            if (!bossActive && score >= 50) {
                boss = new Boss(WIDTH / 2 - 50, HEIGHT / 4);
                bossActive = true;
            }

            if (bossActive && boss != null) {
                boss.update();
                boss.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);

                for (int i = bullets.size() - 1; i >= 0; i--) {
                    Bullet b = bullets.get(i);
                    if (boss.getBounds().intersects(new Rectangle((int) b.x, (int) b.y, b.size, b.size))) {
                        boss.takeDamage(10);
                        bullets.remove(i);
                        explosions.add(new Explosion(boss.x + boss.width / 2.0, boss.y + boss.height / 2.0));
                        screenshakeFrames = 15;
                    }
                }

                for (int i = boss.orbs.size() - 1; i >= 0; i--) {
                    Boss.Orb orb = boss.orbs.get(i);
                    if (new Rectangle((int)player.x, (int)player.y, player.width, player.height).intersects(orb.getBounds())) {
                        player.takeDamage(10);
                        boss.orbs.remove(i);
                    }
                }

                if (boss.isDead()) {
                    bossActive = false;
                    boss = null;
                    score += 100;
                }
            }

            player.update(mouseX, mouseY);

            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet b = bullets.get(i);
                b.update();
                if (b.isOffScreen(WIDTH, HEIGHT)) bullets.remove(i);
            }

            double spawnRate = bossActive ?0.01 : 0.05;; // reduced spawn rate if boss active
            if (Math.random() < spawnRate) {
                spawnEnemyAtEdge();
            }


            for (Enemy en : enemies) {
                en.chase(player.x + player.width / 2.0, player.y + player.height / 2.0);
            }

            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy en = enemies.get(i);
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);
                for (int j = bullets.size() - 1; j >= 0; j--) {
                    Bullet b = bullets.get(j);
                    Rectangle bulletRect = new Rectangle((int) b.x, (int) b.y, b.size, b.size);
                    if (enemyRect.intersects(bulletRect)) {
                        explosions.add(new Explosion(en.x + en.width / 2.0, en.y + en.height / 2.0));
                        screenshakeFrames = 12;
                        enemies.remove(i);
                        bullets.remove(j);
                        score += 10;
                        break;
                    }
                }
            }

            Rectangle playerRect = new Rectangle((int) player.x, (int) player.y, player.width, player.height);
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy en = enemies.get(i);
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);
                if (enemyRect.intersects(playerRect)) {
                    player.takeDamage(20);
                    explosions.add(new Explosion(en.x + en.width / 2.0, en.y + en.height / 2.0));
                    screenshakeFrames = 12;
                    enemies.remove(i);
                }
            }

            for (int i = explosions.size() - 1; i >= 0; i--) {
                explosions.get(i).age++;
                if (!explosions.get(i).isAlive())
                    explosions.remove(i);
            }

            if (screenshakeFrames > 0) screenshakeFrames--;

            if (player.health <= 0) {
                gameOver = true;
                // Despawn the boss on game over
                bossActive = false;
                boss = null;
            }
        }

        if (gameOver && !savedStatsOnGameOver) {
            savedStatsOnGameOver = true;
            handleGameOverAndSave();
            timer.stop();
        }

        repaint();
    }

    private void spawnEnemyAtEdge() {
        int edge = (int)(Math.random() * 4);
        int ex = 0, ey = 0;
        switch (edge) {
            case 0: ex = (int)(Math.random() * (WIDTH - 40)); ey = -40; break;
            case 1: ex = WIDTH; ey = (int)(Math.random() * (HEIGHT - 40)); break;
            case 2: ex = (int)(Math.random() * (WIDTH - 40)); ey = HEIGHT; break;
            case 3: ex = -40; ey = (int)(Math.random() * (HEIGHT - 40)); break;
        }
        enemies.add(new Enemy(ex, ey));
    }

    private void handleGameOverAndSave() {
        if (db != null && username != null && !username.equalsIgnoreCase("Guest")) {
            db.updateHighscore(username, score);
            int coinsEarned = score / 10;
            if (coinsEarned > 0) {
                db.updateCurrency(username, coinsEarned);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int shakeX = 0, shakeY = 0;
        if (screenshakeFrames > 0) {
            shakeX = (int)(Math.random() * 8 - 4);
            shakeY = (int)(Math.random() * 8 - 4);
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(shakeX, shakeY);

        if (SpriteManager.bgSprite != null) {
            g2.drawImage(SpriteManager.bgSprite, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }

        player.draw(g2);

        for (Bullet b : bullets) b.draw(g2);

        for (Enemy en : enemies) en.draw(g2, player);

        if (bossActive && boss != null) {
            boss.draw(g2);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Player: " + username, 10, 20);
        g2.drawString("Score: " + score, 10, 45);

        int barWidth = 150, barHeight = 20;
        int xPos = WIDTH - barWidth - 20, yPos = 20;
        g2.setColor(Color.GRAY);
        g2.fillRect(xPos, yPos, barWidth, barHeight);
        g2.setColor(Color.RED);
        int healthWidth = (int)((player.health / (double)player.maxHealth) * barWidth);
        g2.fillRect(xPos, yPos, healthWidth, barHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(xPos, yPos, barWidth, barHeight);

        for (Explosion ex : explosions) {
            float alpha = 1.0f - (ex.age / (float)ex.duration);
            if (SpriteManager.blastSprite != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                int size = 60;
                g2.drawImage(SpriteManager.blastSprite, (int)ex.x - size/2, (int)ex.y - size/2, size, size, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                g2.setColor(new Color(255, 100, 0, (int)(180 * alpha)));
                g2.fillOval((int)ex.x - 32, (int)ex.y - 32, 64, 64);
            }
        }

        if (gameOver) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 50));
            g2.drawString("GAME OVER", WIDTH / 2 - 150, HEIGHT / 2);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            g2.drawString("Press ENTER to restart", WIDTH / 2 - 110, HEIGHT / 2 + 40);
        }

        g2.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            startGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { player.keyReleased(e); }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public boolean isFocusable() { return true; }
}
