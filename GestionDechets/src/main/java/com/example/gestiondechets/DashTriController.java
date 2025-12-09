package com.example.gestiondechets;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashTriController {

    @FXML
    private Label nomAgentLabel;

    @FXML
    private TableView<HistoriqueDechet> historiqueTable;

    @FXML
    private TableColumn<HistoriqueDechet, Integer> idAgentCol; // Changed from String to Integer

    @FXML
    private TableColumn<HistoriqueDechet, Integer> idTriageCol; // Changed to Integer

    @FXML
    private TableColumn<HistoriqueDechet, String> typeDechetCol;

    @FXML
    private TableColumn<HistoriqueDechet, Double> quantiteCol; // Changed to Double

    @FXML
    private TableColumn<HistoriqueDechet, String> statutTriageCol;

    @FXML
    private TableColumn<HistoriqueDechet, String> dateTriageCol;

    private Connection conn;
    private ObservableList<HistoriqueDechet> historiqueData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("DashTriController initialize() called"); // DEBUG

        conn = Database.connectDB();
        if (conn == null) {
            System.out.println("ERROR: Database connection is null!"); // DEBUG
            return;
        }

        // DEBUG: Check if table columns are injected
        System.out.println("historiqueTable: " + historiqueTable);
        System.out.println("idTriageCol: " + idTriageCol);
        System.out.println("typeDechetCol: " + typeDechetCol);
        System.out.println("quantiteCol: " + quantiteCol);
        System.out.println("statutTriageCol: " + statutTriageCol);
        System.out.println("dateTriageCol: " + dateTriageCol);

        // Set up table columns using PropertyValueFactory for simplicity
        // Make sure these match the property names in HistoriqueDechet class
        idAgentCol.setCellValueFactory(new PropertyValueFactory<>("idAgent"));
        idTriageCol.setCellValueFactory(new PropertyValueFactory<>("idDechet"));
        typeDechetCol.setCellValueFactory(new PropertyValueFactory<>("typeDechet"));
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        statutTriageCol.setCellValueFactory(new PropertyValueFactory<>("toxicite"));
        dateTriageCol.setCellValueFactory(new PropertyValueFactory<>("dateTriage"));

        // Set agent name
        if (Database.getActiveUser() != null) {
            nomAgentLabel.setText(Database.getActiveUser().getNom());
            System.out.println("Agent name set to: " + Database.getActiveUser().getNom()); // DEBUG
        } else {
            System.out.println("WARNING: No active user found!"); // DEBUG
        }

        // Load historical data
        chargerHistorique30Jours();
    }

    private void chargerHistorique30Jours() {
        System.out.println("chargerHistorique30Jours() called"); // DEBUG
        historiqueData.clear();

        // Calculate date 30 days ago
        LocalDate dateLimite = LocalDate.now().minusDays(30);
        String dateLimiteStr = dateLimite.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("Loading data from date: " + dateLimiteStr); // DEBUG

        String sql = """
            SELECT 
                d.id_dechet,
                d.type_dechet,
                d.quantite,
                d.toxicite,
                DATE_FORMAT(d.date_tri, '%d/%m/%Y %H:%i') as date_tri_formatted,
                d.id_agent_tri
            FROM DECHET d
            WHERE d.date_tri >= ? AND d.date_tri IS NOT NULL
            ORDER BY d.date_tri DESC
            LIMIT 100
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateLimiteStr);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                int idDechet = rs.getInt("id_dechet");
                String typeDechet = rs.getString("type_dechet");
                double quantite = rs.getDouble("quantite");
                String toxicite = rs.getString("toxicite");
                String dateTriage = rs.getString("date_tri_formatted");
                int idAgent = rs.getInt("id_agent_tri");

                System.out.println("Loaded row " + count + ": " +
                        idDechet + ", " + typeDechet + ", " + quantite + "kg, " +
                        toxicite + ", " + dateTriage); // DEBUG

                historiqueData.add(new HistoriqueDechet(
                        idAgent,
                        idDechet,
                        typeDechet,
                        quantite,
                        toxicite,
                        dateTriage
                ));
            }

            System.out.println("Total rows loaded: " + count); // DEBUG

            historiqueTable.setItems(historiqueData);
            System.out.println("Table items set, size: " + historiqueData.size()); // DEBUG

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL Error: " + e.getMessage()); // DEBUG
            showError("Erreur", "Impossible de charger l'historique: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("General Error: " + e.getMessage()); // DEBUG
        }
    }

    // Model class for historique table
    public static class HistoriqueDechet {
        private final Integer idAgent;
        private final Integer idDechet;
        private final String typeDechet;
        private final Double quantite;
        private final String toxicite;
        private final String dateTriage;

        public HistoriqueDechet(int idAgent, int idDechet, String typeDechet,
                                double quantite, String toxicite, String dateTriage) {
            this.idAgent = idAgent;
            this.idDechet = idDechet;
            this.typeDechet = typeDechet;
            this.quantite = quantite;
            this.toxicite = toxicite;
            this.dateTriage = dateTriage;
        }

        // Getters for PropertyValueFactory (must be exactly these names!)
        public Integer getIdAgent() { return idAgent; }
        public Integer getIdDechet() { return idDechet; }
        public String getTypeDechet() { return typeDechet; }
        public Double getQuantite() { return quantite; }
        public String getToxicite() { return toxicite; }
        public String getDateTriage() { return dateTriage; }
    }

    // Navigation methods (keep as before)
    @FXML
    private void showDashboard(ActionEvent event) {
        // Already on dashboard
    }

    @FXML
    private void showTriage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("triage-dechets.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page de triage");
        }
    }

    @FXML
    private void showRapports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("generer-rapport-tri.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page des rapports");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                    Parent root = loader.load();

                    Scene currentScene = ((Node) event.getSource()).getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur de déconnexion", "Impossible de charger la page de connexion");
                }
                Database.logoutUserByPhone(Database.getActiveUser().getTelephone());

            }
        });
    }

    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}