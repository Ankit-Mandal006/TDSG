import java.awt.*;
import java.awt.AlphaComposite;

public class Explosion {
    public double x, y;
    public int duration = 24;  // total lifespan in frames (~0.4 seconds at 60 FPS)
    public int age = 0;

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isAlive() {
        return age < duration;
    }

    public void draw(Graphics g) {
        float alpha = 1.0f - ((float) age / duration); // fade out effect
        Graphics2D g2 = (Graphics2D) g.create();

        if (SpriteManager.blastSprite != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int size = 60;  // explosion size
            g2.drawImage(SpriteManager.blastSprite, (int) x - size / 2, (int) y - size / 2, size, size, null);
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(255, 100, 0, (int) (180 * alpha)));  // reddish-orange fading circle
            g2.fillOval((int) x - 32, (int) y - 32, 64, 64);
        }

        g2.dispose();
    }
}
