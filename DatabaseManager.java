import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:game.db";
    private String lastError = null;

    public String getLastError() {
        return lastError;
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Add sqlite-jdbc JAR to classpath.");
        }
    }

    public DatabaseManager() {
        createTables();
        addColumnsIfNeeded();
    }

    private void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            // Create table if not exists
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "highscore INTEGER DEFAULT 0," +
                "currency INTEGER DEFAULT 0" +
                ")"
            );

            // Add columns if not present (SQLite versions may have limited ALTER support)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN purchased_skins TEXT DEFAULT ''");
            } catch (SQLException e) {
                // Column probably exists; ignore
            }

            try {
                stmt.execute("ALTER TABLE users ADD COLUMN selected_skin TEXT DEFAULT 'default'");
            } catch (SQLException e) {
                // Column probably exists; ignore
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addColumnsIfNeeded() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Add purchased_skins column if it does not exist
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN purchased_skins TEXT DEFAULT ''");
            } catch (SQLException e) {
                // Exception likely "duplicate column", ignore it
            }

            // Add selected_skin column if it does not exist
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN selected_skin TEXT DEFAULT 'default'");
            } catch (SQLException e) {
                // Exception likely "duplicate column", ignore it
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean isSkinPurchased(String username, String skinId) {
        String sql = "SELECT purchased_skins FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String skins = rs.getString("purchased_skins");
                if (skins != null && !skins.isEmpty()) {
                    for (String s : skins.split(",")) {
                        if (s.trim().equalsIgnoreCase(skinId.trim())) return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void buySkin(String username, String skinId, int price) {
        int currentCoins = getCurrency(username);
        if (currentCoins < price) return;

        String purchasedSql = "SELECT purchased_skins FROM users WHERE username=?";
        String updateSql = "UPDATE users SET currency=?, purchased_skins=? WHERE username=?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement select = conn.prepareStatement(purchasedSql);
             PreparedStatement update = conn.prepareStatement(updateSql)) {
            select.setString(1, username);
            ResultSet rs = select.executeQuery();

            String skins = "";
            if (rs.next()) {
                skins = rs.getString("purchased_skins");
                if (skins == null) skins = "";
            }
            if (!skins.contains(skinId)) {
                skins = skins.isEmpty() ? skinId : skins + "," + skinId;
            }
            int newBalance = currentCoins - price;

            update.setInt(1, newBalance);
            update.setString(2, skins);
            update.setString(3, username);
            update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getSelectedSkin(String username) {
        String sql = "SELECT selected_skin FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("selected_skin");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "default";
    }

    public void setSelectedSkin(String username, String skinId) {
        String sql = "UPDATE users SET selected_skin=? WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, skinId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getCurrency(String username) {
        String sql = "SELECT currency FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("currency");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateCurrency(String username, int deltaCoins) {
        String sql = "UPDATE users SET currency = currency + ? WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, deltaCoins);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(String username, String password) {
        lastError = null;
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            lastError = "Username/password cannot be empty.";
            return false;
        }
        String sql = "INSERT INTO users(username,password) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        lastError = null;
        if (username == null || username.isBlank() || password == null) {
            lastError = "Username/password cannot be empty.";
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE username=? AND password=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean ok = rs.next();
                if (!ok) lastError = "Invalid username or password.";
                return ok;
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public int getHighscore(String username) {
        lastError = null;
        String sql = "SELECT highscore FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("highscore");
                return 0;
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return 0;
        }
    }

    public void updateHighscore(String username, int score) {
        lastError = null;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int current = getHighscore(username);
            if (score > current) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE users SET highscore=? WHERE username=?")) {
                    pstmt.setInt(1, score);
                    pstmt.setString(2, username);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
    }
}
