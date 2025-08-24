import java.awt.*;
import java.awt.event.KeyEvent;

public class Player {
    double x, y;
    double vx = 0, vy = 0;        // velocity
    double accel = 0.3;           // acceleration per tick
    double maxSpeed = 6;          // max velocity
    double friction = 0.05;       // natural slowdown
    double angle;                 // rotation toward mouse
    int width = 40, height = 40;
    
    int maxHealth = 100;
    int health = 100;

    boolean up, down, left, right;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(int mouseX, int mouseY) {
        // Rotation toward mouse
        angle = Math.atan2(mouseY - (y + height / 2), mouseX - (x + width / 2));

        // Apply acceleration
        if (up) vy -= accel;
        if (down) vy += accel;
        if (left) vx -= accel;
        if (right) vx += accel;

        // Apply friction (only if no input in that direction)
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

        // Keep player inside screen
        if (x < 0) { x = 0; vx = 0; }
        if (x + width > GamePanel.WIDTH) { x = GamePanel.WIDTH - width; vx = 0; }
        if (y < 0) { y = 0; vy = 0; }
        if (y + height > GamePanel.HEIGHT) { y = GamePanel.HEIGHT - height; vy = 0; }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + width/2, y + height/2);
        g2.rotate(angle);

        if (SpriteManager.playerSprite != null) {
            g2.drawImage(SpriteManager.playerSprite, -width/2, -height/2, width, height, null);
        } else {
            g2.setColor(Color.CYAN);
            g2.fillRect(-width/2, -height/2, width, height);
        }

        g2.dispose();
    }

    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        if (e.getKeyCode() == KeyEvent.VK_S) down = true;
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) up = false;
        if (e.getKeyCode() == KeyEvent.VK_S) down = false;
        if (e.getKeyCode() == KeyEvent.VK_A) left = false;
        if (e.getKeyCode() == KeyEvent.VK_D) right = false;
    }
    
    public void takeDamage(int dmg) {
        health -= dmg;
        if (health < 0) health = 0;
    }

    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }
}
