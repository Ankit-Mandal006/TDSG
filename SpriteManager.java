
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteManager {

    public static BufferedImage playerSprite;
    public static BufferedImage enemySprite;
    public static BufferedImage bulletSprite;
    public static BufferedImage bgSprite;
    public static BufferedImage blastSprite;
    public static BufferedImage bossSprite;
    public static BufferedImage orbSprite;

    // Call once at game start
    public static void loadSprites() {
        try {
            playerSprite = ImageIO.read(SpriteManager.class.getResource("/assets/Player1.png"));
            System.out.println("✅ Player sprite loaded");

            enemySprite = ImageIO.read(SpriteManager.class.getResource("/assets/Enemy1.png"));
            System.out.println("✅ Enemy sprite loaded");

            bulletSprite = ImageIO.read(SpriteManager.class.getResource("/assets/bullet.png"));
            System.out.println("✅ Bullet sprite loaded");

            bgSprite = ImageIO.read(SpriteManager.class.getResource("/assets/bg.png"));
            System.out.println("✅ Background sprite loaded");
            
            blastSprite = ImageIO.read(SpriteManager.class.getResource("/assets/blast.png"));
            System.out.println("✅ Explosion sprite loaded");
            
            bossSprite = ImageIO.read(SpriteManager.class.getResource("/assets/Boss1.png"));
            orbSprite = ImageIO.read(SpriteManager.class.getResource("/assets/orb.png"));

        } catch (IOException e) {
            System.out.println("⚠ Error reading image file");
            
            bossSprite = null;
            orbSprite = null;
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("❌ Could not find one or more sprite files in /assets/ folder");
            npe.printStackTrace();
        }
    }

}
