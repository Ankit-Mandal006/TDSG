import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LeaderboardWindow extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public LeaderboardWindow() {
        setTitle("🏆 Leaderboard");
        setSize(400, 400);
        setLocation(1080, 100); // Opens beside your main game
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"Username", "Highscore", "Currency"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("🔄 Refresh");
        refresh.addActionListener(e -> loadData());
        add(refresh, BorderLayout.SOUTH);

        loadData();
        setVisible(true);
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:game.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, highscore, currency FROM users ORDER BY highscore DESC LIMIT 10")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("username"),
                        rs.getInt("highscore"),
                        rs.getInt("currency")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading leaderboard: " + e.getMessage());
        }
    }
}
