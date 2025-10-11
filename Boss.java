import java.awt.*;
import java.util.ArrayList;

public class Boss {
    double x, y;
    int width = 100, height = 100;
    int maxHealth = 500;
    int health = maxHealth;
    double facingAngle = 0; 

    ArrayList<Orb> orbs = new ArrayList<>();
    int orbSpawnTimer = 0;

    // Spawn animation fields
    private boolean spawning = true;
    private int spawnTimer = 0;
    private static final int SPAWN_DURATION = 60; // 1 second at 60 FPS

    // Defeat animation fields
    private boolean defeated = false;
    private int defeatTimer = 0;
    private static final int DEFEAT_DURATION = 60; // 1 second
    private ArrayList<Explosion> defeatExplosions = new ArrayList<>();

    public Boss(double x, double y) {
        this.x = x;
        this.y = y;
        spawning = true;
        spawnTimer = 0;
        defeated = false;
        defeatTimer = 0;
        defeatExplosions.clear();
    }

    public static Point getRandomSpawnPoint() {
        int padding = 100; // padding from edges
        int spawnX = padding + (int) (Math.random() * (GamePanel.WIDTH - 2 * padding - 100));
        int spawnY = padding + (int) (Math.random() * (GamePanel.HEIGHT - 2 * padding - 100));
        return new Point(spawnX, spawnY);
    }

    public void update() {
        if (spawning) {
            spawnTimer++;
            if (spawnTimer >= SPAWN_DURATION) {
                spawning = false;
            }
            return; // skip other updates while spawning
        }

        if (defeated) {
            defeatTimer++;

            // Spawn explosions periodically during defeat animation
            if (defeatTimer % 10 == 0) {
                double ex = x + Math.random() * width;
                double ey = y + Math.random() * height;
                defeatExplosions.add(new Explosion(ex, ey));
            }

            // Update explosions and remove dead ones
            for (int i = defeatExplosions.size() - 1; i >= 0; i--) {
                Explosion explosion = defeatExplosions.get(i);
                explosion.age++;
                if (!explosion.isAlive()) {
                    defeatExplosions.remove(i);
                }
            }

            // After defeat animation ends, clear orbs and mark health zero to despawn
            if (defeatTimer >= DEFEAT_DURATION) {
                health = 0;
                orbs.clear();
            }
            return; // skip normal updates while defeated
        }

        // Normal behavior
        orbSpawnTimer++;
        if (orbSpawnTimer % 60 == 0) {
            spawnOrbs();
        }

        for (int i = orbs.size() - 1; i >= 0; i--) {
            Orb orb = orbs.get(i);
            orb.update();
            if (orb.isOffScreen(GamePanel.WIDTH, GamePanel.HEIGHT)) {
                orbs.remove(i);
            }
        }
    }

    public void chase(double targetX, double targetY) {
        if (spawning || defeated) return;
        double speed = 1.0; // slow speed
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }

        // update facing angle to point toward the player
        facingAngle = Math.atan2(dy, dx);
    }

    private void spawnOrbs() {
        int minOrbs = 20;
        int maxOrbs = 30;
        // Compute orb count based on health lost (more lost = more orbs)
        int orbCount = minOrbs + (int) (((maxHealth - health) / (double) maxHealth) * (maxOrbs - minOrbs));

        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double baseAngle = 2 * Math.PI / orbCount;

        for (int i = 0; i < orbCount; i++) {
            double angle = baseAngle * i + (Math.random() * 0.4 - 0.2);
            orbs.add(new Orb(centerX, centerY, angle));
        }
    }


    public void draw(Graphics g) {
        if (spawning) {
            float alpha = 1.0f - spawnTimer / (float) SPAWN_DURATION;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(255, 255, 0, (int) (alpha * 255)));
            int radius = (int) (width * 1.5 * (spawnTimer / (float) SPAWN_DURATION));
            g2.fillOval((int) (x + width / 2 - radius / 2), (int) (y + height / 2 - radius / 2), radius, radius);
            g2.dispose();
            return;
        }

        if (defeated) {
            for (Explosion ex : defeatExplosions) {
                ex.draw(g);
            }
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        // Rotate around boss center by facingAngle
        int centerX = (int) (x + width / 2.0);
        int centerY = (int) (y + height / 2.0);
        g2.rotate(facingAngle, centerX, centerY);

        if (SpriteManager.bossSprite != null) {
            g2.drawImage(SpriteManager.bossSprite, (int) x, (int) y, width, height, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect((int) x, (int) y, width, height);
        }

        g2.dispose();

        // Draw orbs and health bar as usual without rotation
        for (Orb orb : orbs) {
            orb.draw(g);
        }

        int barWidth = 400;
        int barHeight = 20;
        int xPos = (GamePanel.WIDTH - barWidth) / 2;
        int yPos = GamePanel.HEIGHT - barHeight - 20;
        g.setColor(Color.GRAY);
        g.fillRect(xPos, yPos, barWidth, barHeight);
        g.setColor(Color.RED);
        int healthWidth = (int) ((health / (double) maxHealth) * barWidth);
        g.fillRect(xPos, yPos, healthWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(xPos, yPos, barWidth, barHeight);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public void takeDamage(int dmg) {
        if (spawning || defeated) return;
        health -= dmg;
        if (health <= 0) {
            health = 0;
            defeated = true;
            defeatTimer = 0;
            defeatExplosions.clear();
        }
    }

    public boolean isDead() {
        return health <= 0 && !spawning; // consider dead after spawn animation finishes and health zero
    }

    // Inner Orb class remains unchanged
    public static class Orb {
        double x, y;
        double angle;
        double speed = 4;
        int size = 10;

        public Orb(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public void update() {
            x += Math.cos(angle) * speed;
            y += Math.sin(angle) * speed;
        }

        public void draw(Graphics g) {
            if (SpriteManager.orbSprite != null) {
                g.drawImage(SpriteManager.orbSprite, (int) x, (int) y, size, size, null);
            } else {
                g.setColor(Color.ORANGE);
                g.fillOval((int) x, (int) y, size, size);
            }
        }

        public boolean isOffScreen(int width, int height) {
            return x < -size || x > width || y < -size || y > height;
        }

        public Rectangle getBounds() {
            return new Rectangle((int) x, (int) y, size, size);
        }
    }
}
