import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DatabaseUtil {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        carregarConfiguracaoBD();
    }

    private static void carregarConfiguracaoBD() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/db.properties")) {
            props.load(fis);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            System.out.println("DB URL: " + URL);
            System.out.println("DB USER: " + USER);
        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração da BD: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

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

    public static boolean registerUser(String username, String email, String password) {
        try (Connection conn = getConnection()) {
            System.out.println("Tentando registar user...");
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);
            System.out.println("DB URL: " + URL);

            String checkQuery = "SELECT id FROM users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);

            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next()) {
                System.out.println("Já existe um utilizador com este username ou email.");
                return false;
            }

            String insertQuery = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));

            int linhas = stmt.executeUpdate();
            System.out.println("Linhas inseridas: " + linhas);

            return linhas > 0;
        } catch (SQLException e) {
            System.err.println("ERRO REAL NO REGISTO:");
            System.err.println("Mensagem: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

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

    public static boolean savePlaylist(int userId, String playlistName, List<String> songs) {
        try (Connection conn = getConnection()) {
            String checkQuery = "SELECT id FROM playlists WHERE user_id = ? AND name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, playlistName);

            ResultSet rs = checkStmt.executeQuery();
            String songsString = String.join("|", songs);

            if (rs.next()) {
                String updateQuery = "UPDATE playlists SET songs = ? WHERE user_id = ? AND name = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, songsString);
                updateStmt.setInt(2, userId);
                updateStmt.setString(3, playlistName);
                return updateStmt.executeUpdate() > 0;
            } else {
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

    public static List<String> loadPlaylist(int userId, String playlistName) {
        List<String> lista = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String query = "SELECT songs FROM playlists WHERE user_id = ? AND name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, playlistName);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String songs = rs.getString("songs");
                if (songs != null && !songs.isEmpty()) {
                    lista = new ArrayList<>(Arrays.asList(songs.split("\\|")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading playlist: " + e.getMessage());
        }

        return lista;
    }

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

    private static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}