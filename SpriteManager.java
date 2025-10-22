
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteManager {
    // existing sprites
	public static BufferedImage playerDefaultSprite;
    public static BufferedImage playerSkin1;
    public static BufferedImage playerSkin2;
    public static BufferedImage playerSkin3;
    public static BufferedImage selectedPlayerSprite;
    public static BufferedImage enemySprite;
    public static BufferedImage bulletSprite;
    public static BufferedImage enemybulletSprite;
    public static BufferedImage bgSprite;
    public static BufferedImage blastSprite;
    public static BufferedImage bossSprite;
    public static BufferedImage orbSprite;
    
    

    // new sprites for additional enemy types
    public static BufferedImage chargerEnemySprite;
    public static BufferedImage rangedEnemySprite;

    // Load all sprites here
    public static void loadSprites() {
        try {
            // existing sprite loads
        	

        	playerDefaultSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Player1.png"));
            playerSkin1 = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Player2.png"));
            playerSkin2 = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Player3.png"));
            playerSkin3 = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Player4.png"));
            selectedPlayerSprite = playerDefaultSprite;
            enemySprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Enemy1.png"));
            bulletSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/bullet.png"));
            enemybulletSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Enemy bullet.png"));
            bgSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/bg.png"));
            blastSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/blast.png"));
            bossSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/Boss1.png"));
            orbSprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/orb.png"));

            // load new enemy sprites
            chargerEnemySprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/ChargerEnemy.png"));
            rangedEnemySprite = ImageIO.read(SpriteManager.class.getResourceAsStream("/assets/RangedEnemy.png"));

            System.out.println("✅ Enemy sprites loaded");

        } catch (IOException e) {
            System.out.println("⚠ Error reading image file");
            // fallback to null if not found
            chargerEnemySprite = null;
            rangedEnemySprite = null;
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("❌ Could not find one or more sprite files in /assets/ folder");
            npe.printStackTrace();
        }
    }
}
