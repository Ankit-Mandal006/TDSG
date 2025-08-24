import java.awt.*;

public class Enemy {
    double x, y;
    double speed = 1.6; // tweakable
    int width = 40, height = 40;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    // Move toward target (player)
    public void chase(double targetX, double targetY) {
        double angle = Math.atan2(
                targetY - (y + height / 2.0),
                targetX - (x + width / 2.0)
        );
        x += Math.cos(angle) * speed;
        y += Math.sin(angle) * speed;
    }

    // Draw enemy rotated toward player
    public void draw(Graphics g, Player player) {
        Graphics2D g2 = (Graphics2D) g.create();

        // rotate toward player
        double angle = Math.atan2(
                (player.y + player.height / 2.0) - (y + height / 2.0),
                (player.x + player.width / 2.0) - (x + width / 2.0)
        );
        g2.translate(x + width / 2.0, y + height / 2.0);
        g2.rotate(angle);

        if (SpriteManager.enemySprite != null) {
            g2.drawImage(SpriteManager.enemySprite, -width / 2, -height / 2, width, height, null);
        } else {
            // 🔴 Always visible fallback
            g2.setColor(Color.RED);
            g2.fillRect(-width / 2, -height / 2, width, height);

            g2.setColor(Color.BLACK);
            g2.drawRect(-width / 2, -height / 2, width, height);
            System.out.println("⚠️ Enemy sprite is NULL → drawing fallback box at (" + x + "," + y + ")");
        }
        System.out.println("Drawing enemy at: " + x + ", " + y);
        g2.dispose();
    }
}
