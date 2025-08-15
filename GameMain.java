

import javax.swing.*;

public class GameMain {
    public static void main(String[] args) {
    	SpriteManager.loadSprites();
        JFrame frame = new JFrame("Space Survivor");
        GamePanel panel = new GamePanel();

        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.startGame();
    }
}
