import javax.swing.*;

public class GameMain extends JFrame {
    private StartMenuPanel startMenuPanel;
    private GamePanel gamePanel;

    private String username;
    private DatabaseManager db;

    public GameMain(String username, DatabaseManager db) {
        super("Trigger Tracker - Player: " + username);
        this.username = username;
        this.db = db;

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

    // === Show Start Menu ===
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

    // === Show Game Panel ===
    public void showGamePanel() {
        if (startMenuPanel != null) {
            getContentPane().remove(startMenuPanel);
            startMenuPanel = null;
        }

        // ✅ Pass (this, username, db) to GamePanel
        gamePanel = new GamePanel(this, username, db);
        getContentPane().add(gamePanel);
        pack();
        revalidate();
        repaint();

        // ✅ Ensure KeyListener works
        SwingUtilities.invokeLater(() -> {
            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();
        });

        gamePanel.startGame();
    }

    // === Getter methods so panels can access username & db ===
    public String getUsername() {
        return username;
    }

    public DatabaseManager getDatabaseManager() {
        return db;
    }

    public static void main(String[] args) {
        // Example: Hardcoded until login system starts it
        String username = "Player1"; 
        DatabaseManager db = new DatabaseManager();

        SwingUtilities.invokeLater(() -> {
            new GameMain(username, db); // ✅ only ONE JFrame
        });
    }
}
