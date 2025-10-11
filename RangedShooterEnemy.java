import java.awt.*;
import java.util.ArrayList;

public class RangedShooterEnemy extends Enemy {
    private int shootCooldown = 0;
    public ArrayList<Bullet> enemyBullets = new ArrayList<>();

    public RangedShooterEnemy(int startX, int startY) {
        super(startX, startY);
        this.speed = 1.2;
        this.width = 36;
        this.height = 36;
    }

    @Override
    public void chase(double targetX, double targetY) {
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Keep distance between 120 and 180 units from player
        if (distance > 180) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        } else if (distance < 120) {
            x -= (dx / distance) * speed;
            y -= (dy / distance) * speed;
        }
    }

    /** Update enemy: shoot if cooldown allows, update bullets */
    public void update(Player player, int gameWidth, int gameHeight) {
        handleShooting(player);
        updateBullets(gameWidth, gameHeight);
    }

    /** Shoot bullet toward player if cooldown is zero */
    private void handleShooting(Player player) {
        if (shootCooldown > 0) {
            shootCooldown--;
            return;
        }

        double px = player.x + player.width / 2.0;
        double py = player.y + player.height / 2.0;
        double ex = x + width / 2.0;
        double ey = y + height / 2.0;
        double angle = Math.atan2(py - ey, px - ex);

        Bullet bullet = new Bullet(ex, ey, angle, SpriteManager.enemybulletSprite);
        bullet.setSpeed(6); // slower bullet for enemy
        enemyBullets.add(bullet);

        shootCooldown = 80; // cooldown ticks between shots
    }

    /** Update enemy bullets and remove if offscreen */
    private void updateBullets(int gameWidth, int gameHeight) {
        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            Bullet b = enemyBullets.get(i);
            b.update();
            if (b.isOffScreen(gameWidth, gameHeight)) {
                enemyBullets.remove(i);
            }
        }
    }

    /** Draw all enemy bullets */
    public void drawBullets(Graphics g) {
        for (Bullet b : enemyBullets) {
            b.draw(g);
        }
    }

    /** Check collision between enemy bullets and player, apply damage */
    public void checkBulletCollision(Player player) {
        Rectangle playerRect = new Rectangle((int) player.x, (int) player.y, player.width, player.height);

        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            Bullet b = enemyBullets.get(i);
            Rectangle bulletRect = b.getBounds();

            if (bulletRect.intersects(playerRect)) {
                player.takeDamage(10); // customize damage as needed
                enemyBullets.remove(i);
                // TODO: Add hit effects here if desired
            }
        }
    }

    /** Draw rotated enemy facing the player */
    @Override
    public void draw(Graphics g, Player player) {
        Graphics2D g2 = (Graphics2D) g.create();

        double angle = Math.atan2(
            (player.y + player.height / 2.0) - (y + height / 2.0),
            (player.x + player.width / 2.0) - (x + width / 2.0)
        );

        g2.translate(x + width / 2.0, y + height / 2.0);
        g2.rotate(angle);

        if (SpriteManager.rangedEnemySprite != null) {
            g2.drawImage(SpriteManager.rangedEnemySprite, -width / 2, -height / 2, width, height, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(-width / 2, -height / 2, width, height);
            g2.setColor(Color.WHITE);
            g2.drawRect(-width / 2, -height / 2, width, height);
        }

        g2.dispose();
    }
}
