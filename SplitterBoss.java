import java.awt.*;
import java.util.ArrayList;

public class SplitterBoss extends Boss {
    int splitHealthThreshold = maxHealth / 2;
    boolean hasSplit = false;
    public ArrayList<MiniBoss> miniBosses = new ArrayList<>();

    public SplitterBoss(double x, double y) {
        super(x, y);
        // Optionally change width, health, etc.
        this.maxHealth = 450;
        this.health = maxHealth;
        this.width = 90;
        this.height = 90;
    }

    @Override
    public void update() {
        super.update();
        // On reaching threshold, split
        if (!hasSplit && health < splitHealthThreshold) {
            hasSplit = true;
            miniBosses.add(new MiniBoss(x + 30, y + 30));
            miniBosses.add(new MiniBoss(x - 30, y + 30));
        }

        // Update minibosses if any
        for (MiniBoss mb : miniBosses) {
            mb.update();
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        for (MiniBoss mb : miniBosses) mb.draw(g);
    }

    // Inner class for minibosses
    public static class MiniBoss extends Boss {
        public MiniBoss(double x, double y) {
            super(x, y);
            this.maxHealth = 120;
            this.health = maxHealth;
            this.width = 50;
            this.height = 50;
        }

        @Override
        public void update() {
            super.update();
            // Could add different orb patterns or speed here
        }

        @Override
        public void draw(Graphics g) {
            if (SpriteManager.bossSprite != null) {
                g.drawImage(SpriteManager.bossSprite, (int)x, (int)y, width, height, null);
            } else {
                g.setColor(Color.PINK);
                g.fillRect((int)x, (int)y, width, height);
            }
            for (Orb orb : orbs) {
                orb.draw(g);
            }
        }
    }
}
