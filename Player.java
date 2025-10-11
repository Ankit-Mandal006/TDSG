import java.awt.*;
import java.awt.event.KeyEvent;

public class Player {
    double x, y;
    double vx = 0, vy = 0;          // velocity
    final double accel = 0.3;       // acceleration per tick
    final double maxSpeed = 6;      // max velocity
    final double friction = 0.05;   // natural slowdown
    double angle;                   // rotation toward mouse
    final int width = 40, height = 40;

    final int maxHealth = 100;
    int health = maxHealth;

    boolean up, down, left, right;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(int mouseX, int mouseY) {
        angle = Math.atan2(mouseY - (y + height / 2.0), mouseX - (x + width / 2.0));

        // Acceleration
        if (up) vy -= accel;
        if (down) vy += accel;
        if (left) vx -= accel;
        if (right) vx += accel;

        // Apply friction only if no input along that axis
        if (!up && !down) vy *= (1 - friction);
        if (!left && !right) vx *= (1 - friction);

        // Limit speed
        double velocity = Math.sqrt(vx * vx + vy * vy);
        if (velocity > maxSpeed) {
            vx = (vx / velocity) * maxSpeed;
            vy = (vy / velocity) * maxSpeed;
        }

        // Update position
        x += vx;
        y += vy;

        // Clamp position within game boundaries
        x = Math.max(0, Math.min(x, GamePanel.WIDTH - width));
        y = Math.max(0, Math.min(y, GamePanel.HEIGHT - height));

        // Stop velocity if at edges
        if (x == 0 || x == GamePanel.WIDTH - width) vx = 0;
        if (y == 0 || y == GamePanel.HEIGHT - height) vy = 0;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + width / 2.0, y + height / 2.0);
        g2.rotate(angle);

        if (SpriteManager.playerSprite != null) {
            g2.drawImage(SpriteManager.playerSprite, -width / 2, -height / 2, width, height, null);
        } else {
            g2.setColor(Color.CYAN);
            g2.fillRect(-width / 2, -height / 2, width, height);
        }

        g2.dispose();
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = false;
            case KeyEvent.VK_S -> down = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
        }
    }

    public void takeDamage(int dmg) {
        health = Math.max(0, health - dmg);
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
}
