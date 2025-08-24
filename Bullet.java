import java.awt.*;

public class Bullet {
    double x, y;
    double dx, dy;
    double speed = 10;
    int size = 8; // smaller for better visuals

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

        double angle = Math.atan2(dy, dx);

        // Rotate around bullet center
        g2.rotate(angle, x + size / 2.0, y + size / 2.0);

        if (SpriteManager.bulletSprite != null) {
            g2.drawImage(SpriteManager.bulletSprite, (int)x, (int)y, size, size, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int)x, (int)y, size, size);
        }

        g2.dispose();
    }

    public boolean isOffScreen(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }
}
