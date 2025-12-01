package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.io.IOException;

public class SignalementsController {

    @FXML private Label userNameLabel;
    @FXML private Label totalSignalements, nouveauxSignalements, enCoursSignalements, terminesSignalements;

    @FXML private TextField searchField;
    @FXML private DatePicker dateDebutPicker, dateFinPicker;
    @FXML private ComboBox<String> etatFilter, typeFilter;
    @FXML private TableView<?> signalementsTable;

    @FXML private Button dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn;
    @FXML private Button appliquerFiltresBtn;

    @FXML
    public void initialize() {
        // Initialiser le label utilisateur
        if (userNameLabel != null) {
            userNameLabel.setText("Admin : Administrateur");
        }

        // Initialiser les statistiques
        updateStats();

        // Initialiser les combobox
        if (etatFilter != null) {
            etatFilter.setItems(FXCollections.observableArrayList(
                    "Tous", "Nouveau", "En cours", "Terminé", "Annulé"
            ));
            etatFilter.getSelectionModel().select("Tous");
        }

        if (typeFilter != null) {
            typeFilter.setItems(FXCollections.observableArrayList(
                    "Tous", "Organique", "Recyclable", "Dangereux", "Encombrant"
            ));
            typeFilter.getSelectionModel().select("Tous");
        }

        // Mettre à jour le style du bouton actif
        if (signalementsBtn != null) {
            signalementsBtn.setStyle("-fx-background-color: #34495e; -fx-border-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand;");
        }
    }

    private void updateStats() {
        if (totalSignalements != null) totalSignalements.setText("157");
        if (nouveauxSignalements != null) nouveauxSignalements.setText("12");
        if (enCoursSignalements != null) enCoursSignalements.setText("89");
        if (terminesSignalements != null) terminesSignalements.setText("56");
    }

    @FXML
    private void appliquerFiltres() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Filtres");
        alert.setHeaderText(null);
        alert.setContentText("Filtres appliqués avec succès !");
        alert.showAndWait();
    }

    // Méthodes de navigation
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                chargerPage("/com/example/gestiondechets/login.fxml");
            }
        });
    }

    @FXML
    private void showDashboard() {
        chargerPage("/com/example/gestiondechets/DashAdmin.fxml");
    }

    @FXML
    private void showSignalements() {
        // Déjà sur cette page
        updateActiveButton(signalementsBtn);
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

    private void updateActiveButton(Button activeButton) {
        // Réinitialiser tous les styles
        Button[] buttons = {dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn};
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
            if (signalementsTable != null) {
                stage = (Stage) signalementsTable.getScene().getWindow();
            } else if (userNameLabel != null) {
                stage = (Stage) userNameLabel.getScene().getWindow();
            } else {
                // Utiliser n'importe quel élément non null
                stage = (Stage) appliquerFiltresBtn.getScene().getWindow();
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
}