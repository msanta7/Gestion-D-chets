package com.example.gestiondechets;


import javafx.scene.control.TableView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Database {

    public static Connection connectDB()
    {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connect= DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_dechets","root","ayoub2005");

            if (connect != null) {
                System.out.println("Connected to the database successfully");
            }
            return connect;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    public static int getnumberof(String table) {

        // Optional: whitelist table names to avoid SQL injection
        if (!table.matches("[a-zA-Z_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        String query = "SELECT COUNT(*) AS total FROM " + table;

        try (Connection connection = Database.connectDB();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
    public static int countPendingSignalements() {
        String query = "SELECT COUNT(*) AS total " +
                "FROM signalement " +
                "WHERE etat IN ('nouveau', 'en_cours')";

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
    public static int countTermineThisWeek() {
        String query = "SELECT COUNT(*) AS total " +
                "FROM signalement " +
                "WHERE etat = 'termine' " +
                "AND YEARWEEK(date_signalement, 1) = YEARWEEK(CURDATE(), 1)";

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
    public static String getUserName(String telephone) {
        String query = "SELECT nom FROM utilisateur WHERE telephone = ?";

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, telephone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;  // user not found
    }
    public static Utilisateur getActiveUser() {
        String query = "SELECT * FROM utilisateur WHERE estactive = TRUE LIMIT 1";

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                Utilisateur user = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        rs.getBoolean("estactive"),
                        rs.getString("role")
                );
                return user;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;  // no active user
    }
    public static void logoutUserByPhone(String telephone) {
        String query = "UPDATE utilisateur SET estactive = FALSE WHERE telephone = ?";

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, telephone);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public static List<Signalement> getSignalementsThisWeek() {
        String query =
                "SELECT id_signalement, date_signalement, adresse, description, etat " +
                        "FROM signalement " +
                        "WHERE YEARWEEK(date_signalement, 1) = YEARWEEK(CURDATE(), 1)";

        List<Signalement> list = new ArrayList<>();

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Signalement(
                        rs.getInt("id_signalement"),
                        rs.getTimestamp("date_signalement").toString(),
                        rs.getString("adresse"),
                        rs.getString("description"),
                        rs.getString("etat")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
    public static void loginUserByPhone(String telephone) {
        String query = "UPDATE utilisateur SET estactive = TRUE WHERE telephone = ?";

        try (Connection connection = Database.connectDB();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, telephone);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // 1️⃣ Return total number of users
    public static int getTotalUsers() {
        String query = "SELECT COUNT(*) FROM utilisateur";

        try (Connection conn = Database.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 2️⃣ Return number of active users
    public static int getActiveUsers() {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE estactive = TRUE";

        try (Connection conn = Database.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 3️⃣ Return users created in last 3 days
    public static int getNewUsersLast3Days() {
        String query = "SELECT COUNT(*) FROM utilisateur WHERE date_creation >= NOW() - INTERVAL 30 DAY";

        try (Connection conn = Database.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 4️⃣ Add a new user
    public static boolean addUser(String nom,String prenom,String telephone, String adresse, String role, String motDePasse) {

        String query = "INSERT INTO utilisateur (nom, telephone, adresse, role, mot_de_passe, date_creation, estactive) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), 0)";

        try (Connection conn = Database.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nom+ " "+ prenom);
            stmt.setString(2, telephone);
            stmt.setString(3, adresse);
            stmt.setString(4, role);
            stmt.setString(5, motDePasse);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // 5️⃣ Return the user clicked in TableView
    public static Utilisateur getSelectedUser(TableView<Utilisateur> table) {
        return table.getSelectionModel().getSelectedItem();
    }

    // 6️⃣ Delete user by ID
    public static boolean deleteUser(int userId) {
        String query = "DELETE FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public static List<Utilisateur> getAllUsers() {

        List<Utilisateur> users = new ArrayList<>();

        String query = "SELECT id_utilisateur, nom, telephone, adresse, role, mot_de_passe, date_creation, estactive " +
                "FROM utilisateur";

        try (Connection conn = Database.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Utilisateur user = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        rs.getString("role"),
                        rs.getBoolean("estactive"),
                        rs.getString("date_creation")
                );

                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }
    public static List<Utilisateur> filterUsers(String searchText, String roleFilter, String statutFilter) {

        List<Utilisateur> users = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT * FROM utilisateur WHERE 1=1 "
        );

        // Dynamic SQL → add conditions only if filters are present

        // Search by name OR address
        if (searchText != null && !searchText.isEmpty()) {
            query.append(" AND (nom LIKE ? OR adresse LIKE ?) ");
        }

        // Role filter
        if (roleFilter != null && !roleFilter.equals("Tous")) {
            query.append(" AND role = ? ");
        }

        // Statut (estactive)
        if (statutFilter != null && !statutFilter.equals("Tous")) {
            query.append(" AND estactive = ? ");
        }

        try (Connection conn = Database.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;

            // Apply search text
            if (searchText != null && !searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }

            // Apply role filter
            if (roleFilter != null && !roleFilter.equals("Tous")) {
                stmt.setString(paramIndex++, roleFilter);
            }

            // Apply statut filter
            if (statutFilter != null && !statutFilter.equals("Tous")) {
                stmt.setBoolean(paramIndex++, statutFilter.equals("Actif"));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Utilisateur u = new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("adresse"),
                        rs.getString("role"),
                        rs.getBoolean("estactive"),
                        rs.getString("date_creation")
                );
                users.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }


}


