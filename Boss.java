import java.awt.*;
import java.util.ArrayList;

public class Boss {
    double x, y;
    int width = 100, height = 100;
    int maxHealth = 500;
    int health = maxHealth;

    ArrayList<Orb> orbs = new ArrayList<>();
    int orbSpawnTimer = 0;

    public Boss(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        orbSpawnTimer++;
        if (orbSpawnTimer % 60 == 0) { // Spawn orbs every second (adjust as needed)
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
        double speed = 1.0; // slow speed
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > 1) {  // only move if not too close
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    
    private void spawnOrbs() {
        int orbCount = 12;
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double baseAngle = 2 * Math.PI / orbCount;

        for (int i = 0; i < orbCount; i++) {
            // Add small random variation to each angle (±0.2 radians)
            double angle = baseAngle * i + (Math.random() * 0.4 - 0.2);
            orbs.add(new Orb(centerX, centerY, angle));
        }
    }


    public void draw(Graphics g) {
        if (SpriteManager.bossSprite != null) {
            g.drawImage(SpriteManager.bossSprite, (int)x, (int)y, width, height, null);
        } else {
            // fallback
            g.setColor(Color.MAGENTA);
            g.fillRect((int)x, (int)y, width, height);
        }

        for (Orb orb : orbs) {
            orb.draw(g);
        }

        // health bar drawing remains same
        int barWidth = 400;
        int barHeight = 20;
        int xPos = (GamePanel.WIDTH - barWidth) / 2;
        int yPos = GamePanel.HEIGHT - barHeight - 20;
        g.setColor(Color.GRAY);
        g.fillRect(xPos, yPos, barWidth, barHeight);
        g.setColor(Color.RED);
        int healthWidth = (int)((health / (double)maxHealth) * barWidth);
        g.fillRect(xPos, yPos, healthWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(xPos, yPos, barWidth, barHeight);
    }


    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        if (health < 0) health = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    // Orb inner class representing boss projectiles
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
                g.drawImage(SpriteManager.orbSprite, (int)x, (int)y, size, size, null);
            } else {
                g.setColor(Color.ORANGE);
                g.fillOval((int)x, (int)y, size, size);
            }
        }


        public boolean isOffScreen(int width, int height) {
            return x < -size || x > width || y < -size || y > height;
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, size, size);
        }
    }
}
