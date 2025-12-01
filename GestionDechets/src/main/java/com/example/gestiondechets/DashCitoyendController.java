package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class DashCitoyendController {

    @FXML private Label nomCitoyenLabel, welcomeLabel;
    @FXML private Label totalSignalementsLabel, enCoursLabel, resolusLabel;
    @FXML private TableView<?> recentSignalementsTable;

    @FXML private Button dashboardBtn, mesSignalementsBtn, nouveauSignalementBtn;

    @FXML
    public void initialize() {
        // Initialiser les données du citoyen
        nomCitoyenLabel.setText("Mohamed Ali");
        welcomeLabel.setText("Bienvenue, Mohamed !");

        // Initialiser les statistiques
        updateStats();

        // Initialiser le tableau
        initializeTable();

        // Mettre en surbrillance le bouton actif
        dashboardBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px;");
    }

    private void updateStats() {
        totalSignalementsLabel.setText("12");
        enCoursLabel.setText("3");
        resolusLabel.setText("9");
    }

    private void initializeTable() {
        // Initialiser le tableau des signalements récents
        // À implémenter avec vos données réelles
    }

    // Navigation
    @FXML
    private void showDashboard() {
        // Déjà sur le dashboard
        updateActiveButton(dashboardBtn);
    }

    @FXML
    private void showMesSignalements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/citoyen-signalements.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nomCitoyenLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // AJOUTER @FXML ICI ↓↓↓
    @FXML
    private void ouvrirPopupSignalement() {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/popup-signalement.fxml"));
            Parent root = loader.load();

            // Créer la fenêtre popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(nomCitoyenLabel.getScene().getWindow());
            popupStage.initStyle(StageStyle.UTILITY);

            // Configurer le contrôleur
            PopupSignalementController controller = loader.getController();
            controller.setStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setTitle("Nouveau Signalement");
            popupStage.setResizable(false);
            popupStage.showAndWait();

            // Rafraîchir les données après fermeture
            updateStats();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le formulaire de signalement.");
            alert.showAndWait();
        }
    }

    // Ajoutez aussi cette méthode si elle est référencée dans le FXML
    @FXML
    private void showNouveauSignalement() {
        ouvrirPopupSignalement(); // Utilise la même méthode que le popup
    }

    @FXML
    private void showTousSignalements() {
        showMesSignalements(); // Redirige vers la page complète des signalements
    }

    @FXML
    private void showGuideTri() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guide du Tri");
        alert.setHeaderText("Comment bien trier vos déchets");
        alert.setContentText("Guide du tri à implémenter...");
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/login.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) nomCitoyenLabel.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateActiveButton(Button activeButton) {
        // Réinitialiser les styles
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");
        mesSignalementsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");
        nouveauSignalementBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");

        // Appliquer le style au bouton actif
        activeButton.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 5; -fx-text-fill: white; -fx-font-size: 14px;");
    }
}