
import java.awt.*;

public class Bullet {
    double x, y;
    double dx, dy;
    double speed = 10;
    int size = 12;

    public Bullet(double startX, double startY, double angle) {
        this.x = startX;
        this.y = startY;
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Calculate angle of movement
        double angle = Math.atan2(dy, dx);

        // Translate to bullet center
        g2.translate(x + size / 2, y + size / 2);
        g2.rotate(angle);

        // Draw sprite or default shape
        if (SpriteManager.bulletSprite != null) {
            g2.drawImage(SpriteManager.bulletSprite, -size / 2, -size / 2, size, size, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval(-size / 2, -size / 2, size, size);
        }

        g2.dispose();
    }


    public boolean isOffScreen(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }
}
