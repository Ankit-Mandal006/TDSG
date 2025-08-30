import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:game.db";
    private String lastError = null;

    static {
        // Try to ensure driver is available early — helpful error if jar missing.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Add sqlite-jdbc JAR to classpath.");
        }
    }

    public DatabaseManager() {
        createTables();
    }

    public String getLastError() {
        return lastError;
    }

    private void createTables() {
        String userTable =
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT UNIQUE NOT NULL," +
            "password TEXT NOT NULL," +
            "highscore INTEGER DEFAULT 0," +
            "currency INTEGER DEFAULT 0" +
            ")";
        lastError = null;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(userTable);
        } catch (SQLException e) {
            lastError = e.getMessage();
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

    public int getCurrency(String username) {
        lastError = null;
        String sql = "SELECT currency FROM users WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("currency");
                return 0;
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return 0;
        }
    }

    public void updateCurrency(String username, int coins) {
        lastError = null;
        String sql = "UPDATE users SET currency = currency + ? WHERE username=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, coins);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
    }
}
