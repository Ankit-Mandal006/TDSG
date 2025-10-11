import java.awt.*;

public class Bullet {
    private double x, y;
    private double dx, dy;
    private double speed = 10;
    private int size = 8; // bullet size (width and height)
    private Image sprite;

    // Constructor takes start position, angle in radians, and optional sprite image
    public Bullet(double startX, double startY, double angle) {
        this(startX, startY, angle, null);
    }

    public Bullet(double startX, double startY, double angle, Image sprite) {
        this.x = startX;
        this.y = startY;
        this.sprite = sprite;
        setDirection(angle);
        setSpeed(speed); // initializes dx and dy based on angle and speed
    }

    // Set bullet speed while maintaining direction
    public void setSpeed(double speed) {
        this.speed = speed;
        double angle = Math.atan2(dy, dx);
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
    }

    // Set bullet direction (angle in radians) while maintaining speed
    public void setDirection(double angle) {
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
    }

    // Update bullet position based on velocity
    public void update() {
        x += dx;
        y += dy;
    }

    // Draw the bullet rotated in the direction of movement
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        double angle = Math.atan2(dy, dx);
        g2.rotate(angle, x + size / 2.0, y + size / 2.0);

        if (sprite != null) {
            g2.drawImage(sprite, (int) x, (int) y, size, size, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int) x, (int) y, size, size);
        }

        g2.dispose();
    }

    // Check if bullet is off-screen
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < -size || x > screenWidth || y < -size || y > screenHeight;
    }

    // Getters for position and size, useful for collision detection
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
    }
}
