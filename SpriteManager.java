
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteManager {

    public static BufferedImage playerSprite;
    public static BufferedImage enemySprite;
    public static BufferedImage bulletSprite;
    public static BufferedImage bgSprite;

    // Call once at game start
    public static void loadSprites() {
        try {
        	playerSprite = ImageIO.read(new File("./player.png"));
        	enemySprite  = ImageIO.read(new File("./enemy.png"));
        	bulletSprite = ImageIO.read(new File("./bullet.png"));
        	bgSprite     = ImageIO.read(new File("./bg.png"));
            

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading sprites!");
        }
    }
}
