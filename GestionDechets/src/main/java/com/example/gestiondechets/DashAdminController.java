package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class DashAdminController {

    // CORRECTION : Utiliser userNameLabel1 au lieu de userNameLabel
    @FXML private Label userNameLabel1;  // Changé de userNameLabel à userNameLabel1

    @FXML private Label signalementsCount, collectesCount, usersCount, recyclingCount;
    @FXML private Label signalementsLabel, collectesLabel, usersLabel, recyclingLabel;

    @FXML private AnchorPane dashboardView, usersView, settingsView;
    @FXML private TableView<?> recentSignalementsTable;

    // Boutons de navigation
    @FXML private Button dashboard, signalementsBtn, usersBtn, reportsBtn, settingsBtn;

    // Paramètres
    @FXML private ComboBox<String> languageCombo;

    @FXML
    public void initialize() {
        // CORRECTION : Utiliser userNameLabel1
        if (userNameLabel1 != null) {
            userNameLabel1.setText("Admin : Administrateur");
        }

        // Initialiser les statistiques
        updateStats();

        // Initialiser les vues
        showDashboardView();

        // Initialiser les combobox
        if (languageCombo != null) {
            languageCombo.getSelectionModel().select("Français");
        }
    }

    private void updateStats() {
        if (signalementsCount != null) signalementsCount.setText("157");
        if (collectesCount != null) collectesCount.setText("89");
        if (usersCount != null) usersCount.setText("42");
        if (recyclingCount != null) recyclingCount.setText("67%");

        if (signalementsLabel != null) signalementsLabel.setText("Aujourd'hui: 12");
        if (collectesLabel != null) collectesLabel.setText("Aujourd'hui: 8");
        if (usersLabel != null) usersLabel.setText("Actifs: 38");
        if (recyclingLabel != null) recyclingLabel.setText("Mois en cours");
    }

    // Méthodes pour afficher les différentes vues
    @FXML
    private void showDashboardView() {
        if (dashboardView != null) dashboardView.setVisible(true);
        if (usersView != null) usersView.setVisible(false);
        if (settingsView != null) settingsView.setVisible(false);

        // Mettre à jour le style des boutons
        updateActiveButton(dashboard);
    }

    private void updateActiveButton(Button activeButton) {
        // Réinitialiser tous les styles
        Button[] buttons = {dashboard, signalementsBtn, usersBtn, reportsBtn, settingsBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand;");
            }
        }

        // Appliquer le style au bouton actif
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #34495e; -fx-border-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        alert.setContentText("Vous serez redirigé vers la page de connexion.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                chargerPage("/com/example/gestiondechets/login.fxml");
            }
        });
    }

    @FXML
    private void addNewUser() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nouvel utilisateur");
        alert.setHeaderText(null);
        alert.setContentText("Fonctionnalité d'ajout d'utilisateur à implémenter.");
        alert.showAndWait();
    }

    @FXML
    private void filterUsers() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Filtre");
        alert.setHeaderText(null);
        alert.setContentText("Fonctionnalité de filtrage à implémenter.");
        alert.showAndWait();
    }

    @FXML
    private void saveSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres");
        alert.setHeaderText(null);
        alert.setContentText("Paramètres enregistrés avec succès !");
        alert.showAndWait();
    }

    private void afficherErreurDialog(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void chargerPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nouvellePage = loader.load();

            // Récupérer la scène actuelle
            Stage stage;
            if (dashboardView != null) {
                stage = (Stage) dashboardView.getScene().getWindow();
            } else if (recentSignalementsTable != null) {
                stage = (Stage) recentSignalementsTable.getScene().getWindow();
            } else {
                // Utiliser n'importe quel élément non null
                stage = (Stage) userNameLabel1.getScene().getWindow();
            }

            Scene scene = new Scene(nouvellePage);
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreurDialog("Erreur de navigation",
                    "Impossible de charger la page : " + fxmlPath + "\n\nErreur : " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            afficherErreurDialog("Fichier non trouvé",
                    "Le fichier FXML n'a pas été trouvé : " + fxmlPath);
        }
    }

    @FXML
    private void showSignalements() {
        chargerPage("/com/example/gestiondechets/Signalements.fxml");
    }

    @FXML
    private void showUsers() {
        chargerPage("/com/example/gestiondechets/gestUsers.fxml");
    }

    @FXML
    private void showReports() {
        chargerPage("/com/example/gestiondechets/Rapports.fxml");
    }

    @FXML
    private void showSettings() {
        chargerPage("/com/example/gestiondechets/settings.fxml");
    }
}