import java.awt.*;

public class FastChargerEnemy extends Enemy {
    public FastChargerEnemy(int startX, int startY) {
        super(startX, startY);
        this.speed = 4;        // Much faster than normal
        this.width = 32;
        this.height = 32;
    }

    @Override
    public void draw(Graphics g, Player player) {
        Graphics2D g2 = (Graphics2D) g.create();

        double angle = Math.atan2((player.y + player.height / 2.0) - (y + height / 2.0),
                                  (player.x + player.width / 2.0) - (x + width / 2.0));
        g2.translate(x + width / 2.0, y + height / 2.0);
        g2.rotate(angle);

        if (SpriteManager.chargerEnemySprite != null) {
            g2.drawImage(SpriteManager.chargerEnemySprite, -width / 2, -height / 2, width, height, null);
        } else {
            g2.setColor(Color.GREEN); // fallback color
            g2.fillRect(-width / 2, -height / 2, width, height);
            g2.setColor(Color.BLACK);
            g2.drawRect(-width / 2, -height / 2, width, height);
        }

        g2.dispose();
    }
}
