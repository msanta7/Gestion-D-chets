package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class DashTriController {

    @FXML private TableView<Triage> historiqueTable;
    @FXML private TableColumn<Triage, Integer> idTriageCol;
    @FXML private TableColumn<Triage, String> dateTriageCol;
    @FXML private TableColumn<Triage, String> typeDechetCol;
    @FXML private TableColumn<Triage, Double> quantiteCol;
    @FXML private TableColumn<Triage, String> statutTriageCol;

    @FXML private Label nomAgentLabel;

    private ObservableList<Triage> triagesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer le nom de l'agent
        nomAgentLabel.setText("Agent Tri");

        // Configurer les colonnes
        idTriageCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        dateTriageCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        typeDechetCol.setCellValueFactory(cellData -> cellData.getValue().typeDechetProperty());
        quantiteCol.setCellValueFactory(cellData -> cellData.getValue().quantiteProperty().asObject());
        statutTriageCol.setCellValueFactory(cellData -> cellData.getValue().statutProperty());

        // Personnaliser l'affichage du statut
        statutTriageCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    setStyle(getStatutStyle(statut));
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }

            private String getStatutStyle(String statut) {
                switch(statut.toLowerCase()) {
                    case "en attente":
                        return "-fx-background-color: #f39c12; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    case "en cours":
                        return "-fx-background-color: #3498db; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    case "terminé":
                        return "-fx-background-color: #2ecc71; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    case "stocké":
                        return "-fx-background-color: #9b59b6; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    default:
                        return "";
                }
            }
        });

        // Charger les données
        chargerDonneesTest();
    }

    private void chargerDonneesTest() {
        triagesList.clear();

        // Données de test
        triagesList.add(new Triage(3001, "03/12/2024 08:30", "Plastique", 125.5, "terminé"));
        triagesList.add(new Triage(3002, "02/12/2024 14:15", "Verre", 89.2, "stocké"));
        triagesList.add(new Triage(3003, "01/12/2024 10:45", "Métal", 156.8, "en cours"));
        triagesList.add(new Triage(3004, "30/11/2024 16:20", "Carton", 75.3, "terminé"));
        triagesList.add(new Triage(3005, "29/11/2024 09:10", "Organique", 210.7, "stocké"));
        triagesList.add(new Triage(3006, "28/11/2024 11:30", "Plastique", 95.0, "en attente"));
        triagesList.add(new Triage(3007, "27/11/2024 13:45", "Verre", 68.4, "terminé"));

        historiqueTable.setItems(triagesList);
    }

    // === MÉTHODES DE NAVIGATION ===
    @FXML
    private void showDashboard(ActionEvent event) {
        // Déjà sur cette page
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