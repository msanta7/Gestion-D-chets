package com.example.gestiondechets;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.control.*;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class ListeCollectesController {

    @FXML
    private TableView<CollecteFX> triageTable;

    @FXML
    private TableColumn<CollecteFX, Integer> colNcollecte;

    @FXML
    private TableColumn<CollecteFX, Double> colQuantite;

    @FXML
    private TableColumn<CollecteFX, String> colStatut;

    @FXML
    private Button btnTrier;
    private Connection conn;

    @FXML
    public void initialize() {
        conn = Database.connectDB();

        colNcollecte.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colQuantite.setCellValueFactory(data -> data.getValue().quantiteProperty().asObject());
        colStatut.setCellValueFactory(data -> data.getValue().statutProperty());

        chargerCollectes();
        // Disable button by default
        btnTrier.setDisable(true);

        // Enable when a row is selected
        triageTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    btnTrier.setDisable(newSelection == null);
                });
    }

    private void chargerCollectes() {
        triageTable.getItems().clear();

        String sql = """
            SELECT c.id_collecte, c.quantite_collectee,
                   IFNULL(SUM(d.quantite), 0) AS quantite_triee,
                   c.date_collecte,
                   u.nom as agent_nom
            FROM COLLECTE c
            LEFT JOIN DECHET d ON d.id_collecte = c.id_collecte
            LEFT JOIN UTILISATEUR u ON u.id_utilisateur = c.id_agent_collecteur
            GROUP BY c.id_collecte, c.quantite_collectee, c.date_collecte, u.nom
            ORDER BY c.date_collecte DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_collecte");
                double qCollecte = rs.getDouble("quantite_collectee");
                double qTriee = rs.getDouble("quantite_triee");

                String statut;
                if (qTriee == 0) {
                    statut = "Non triée";
                } else if (qTriee < qCollecte) {
                    statut = "En cours";
                } else {
                    statut = "Triée";
                }

                CollecteFX collecte = new CollecteFX(id, qCollecte, statut);
                // Store additional data if needed
                triageTable.getItems().add(collecte);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les collectes: " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirTri() {
        CollecteFX collecte = triageTable.getSelectionModel().getSelectedItem();
        if (collecte == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une collecte à trier", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Load the TriDechet.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TriDechet.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the collecte
            TriDechetController controller = loader.getController();
            controller.setCollecte(collecte);

            // Create and show the popup window
            Stage stage = new Stage();
            stage.setTitle("Tri des déchets - Collecte #" + collecte.getId());
            stage.setScene(new Scene(root));

            // Make it modal (blocks the parent window)
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(triageTable.getScene().getWindow());

            // Set minimum size
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.showAndWait(); // Wait for the popup to close

            // Refresh the table after the popup closes
            chargerCollectes();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre de tri: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    // Rest of your navigation methods remain the same...
    @FXML
    private void showDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DashTri.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger le tableau de bord");
        }
    }

    @FXML
    private void showTriage(ActionEvent event) {
        // Déjà sur cette page
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
                    Database.loginUserByPhone(Database.getActiveUser().getTelephone());

                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur de déconnexion", "Impossible de charger la page de connexion");
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe modèle pour les items de triage (you might not need this here)
    public static class TriageItem {
        private final StringProperty type = new SimpleStringProperty();
        private final DoubleProperty quantite = new SimpleDoubleProperty();
        private final StringProperty statut = new SimpleStringProperty();

        public TriageItem(String type, double quantite, String statut) {
            setType(type);
            setQuantite(quantite);
            setStatut(statut);
        }

        // Getters et Setters
        public String getType() { return type.get(); }
        public void setType(String value) { type.set(value); }
        public StringProperty typeProperty() { return type; }

        public double getQuantite() { return quantite.get(); }
        public void setQuantite(double value) { quantite.set(value); }
        public DoubleProperty quantiteProperty() { return quantite; }

        public String getStatut() { return statut.get(); }
        public void setStatut(String value) { statut.set(value); }
        public StringProperty statutProperty() { return statut; }
    }
    @FXML
    private void ajouterTriage(ActionEvent event) {
        ouvrirTri(); // Just call the existing method
    }
}