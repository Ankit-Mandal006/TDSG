import javax.swing.*;

public class GameMain extends JFrame {
    private StartMenuPanel startMenuPanel;
    private GamePanel gamePanel;

    public GameMain() {
        super("Space Survivor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Load sprites once
        SpriteManager.loadSprites();

        // Start with menu
        showStartMenu();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showStartMenu() {
        if (gamePanel != null) {
            getContentPane().remove(gamePanel);
            gamePanel.stopGame();
            gamePanel = null;
        }

        startMenuPanel = new StartMenuPanel(this);
        getContentPane().add(startMenuPanel);
        pack();
        revalidate();
        repaint();
    }

    public void showGamePanel() {
    if (startMenuPanel != null) {
        getContentPane().remove(startMenuPanel);
        startMenuPanel = null;
    }

    gamePanel = new GamePanel(this);
    getContentPane().add(gamePanel);
    pack();
    revalidate();
    repaint();

    // ✅ Force focus for KeyListener
    SwingUtilities.invokeLater(() -> {
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
    });

    gamePanel.startGame();
}


    public static void main(String[] args) {
        SpriteManager.loadSprites();
        SwingUtilities.invokeLater(() -> {
            new GameMain(); // ✅ only ONE instance
        });
    }
}
