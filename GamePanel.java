import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();

    private int score = 0;
    private boolean gameOver = false;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private int mouseX, mouseY;

    public GamePanel() {
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
        timer.start();
    }

    public void startGame() {
        // reset state if needed
        bullets.clear();
        enemies.clear();
        score = 0;
        gameOver = false;
        player.x = WIDTH / 2.0;
        player.y = HEIGHT / 2.0;
        timer.start();
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
            for (Enemy en : enemies) {
                Rectangle enemyRect = new Rectangle((int) en.x, (int) en.y, en.width, en.height);
                if (enemyRect.intersects(playerRect)) {
                    gameOver = true;
                    break;
                }
                // only trigger game over when enemy fully passes bottom (safer)
                if (en.y > HEIGHT) {
                    gameOver = true;
                    break;
                }
            }

            if (gameOver) timer.stop();
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
            g.drawImage(SpriteManager.bgSprite, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
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
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) startGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
