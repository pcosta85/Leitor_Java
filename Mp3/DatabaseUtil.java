import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseUtil {
    
    private static final String URL = "jdbc:mysql://localhost:3306/mp3_player";
    private static final String USER = "root";
    private static final String PASSWORD = "Pcosta9850053"; // Change this to your MySQL password
    
    // Test database connection
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Authenticate user
    public static boolean authenticateUser(String username, String password) {
        try (Connection conn = getConnection()) {
            String query = "SELECT password_hash FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return hashPassword(password).equals(storedHash);
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return false;
    }
    
    // Register new user
    public static boolean registerUser(String username, String email, String password) {
        try (Connection conn = getConnection()) {
            // Check if user already exists
            String checkQuery = "SELECT id FROM users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            
            if (checkStmt.executeQuery().next()) {
                return false; // User already exists
            }
            
            // Insert new user
            String insertQuery = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
        }
        return false;
    }
    
    // Get user ID by username
    public static int getUserId(String username) {
        try (Connection conn = getConnection()) {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
        return -1;
    }
    
    // Save playlist to database
    public static boolean savePlaylist(int userId, String playlistName, List<String> songs) {
        try (Connection conn = getConnection()) {
            // Check if playlist already exists
            String checkQuery = "SELECT id FROM playlists WHERE user_id = ? AND name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, playlistName);
            
            ResultSet rs = checkStmt.executeQuery();
            
            String songsString = String.join("|", songs);
            
            if (rs.next()) {
                // Update existing playlist
                String updateQuery = "UPDATE playlists SET songs = ? WHERE user_id = ? AND name = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, songsString);
                updateStmt.setInt(2, userId);
                updateStmt.setString(3, playlistName);
                return updateStmt.executeUpdate() > 0;
            } else {
                // Insert new playlist
                String insertQuery = "INSERT INTO playlists (user_id, name, songs) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, playlistName);
                insertStmt.setString(3, songsString);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error saving playlist: " + e.getMessage());
        }
        return false;
    }
    
    // Load playlist from database
    public static List<String> loadPlaylist(int userId, String playlistName) {
        try (Connection conn = getConnection()) {
            String query = "SELECT songs FROM playlists WHERE user_id = ? AND name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, playlistName);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String songs = rs.getString("songs");
                if (songs != null && !songs.isEmpty()) {
                    return new ArrayList<>(Arrays.asList(songs.split("\\|")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading playlist: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    // Get all playlists for a user
    public static List<String> getUserPlaylists(int userId) {
        List<String> playlists = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String query = "SELECT name FROM playlists WHERE user_id = ? ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting user playlists: " + e.getMessage());
        }
        return playlists;
    }
    
    // Delete playlist
    public static boolean deletePlaylist(int userId, String playlistName) {
        try (Connection conn = getConnection()) {
            String query = "DELETE FROM playlists WHERE user_id = ? AND name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, playlistName);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting playlist: " + e.getMessage());
        }
        return false;
    }
    
    // Simple password hashing (use BCrypt for production)
    private static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}