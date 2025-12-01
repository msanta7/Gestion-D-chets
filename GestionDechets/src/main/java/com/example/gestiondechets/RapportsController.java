package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RapportsController {

    // Éléments qui existent dans votre FXML
    @FXML private Label userNameLabel;
    @FXML private Label monthSignalements, monthCollectes, monthDechets, monthRecyclage;

    // RadioButtons pour la période
    @FXML private RadioButton dailyRadio, weeklyRadio, monthlyRadio, yearlyRadio, customRadio;

    // RadioButtons pour le type de rapport
    @FXML private RadioButton signalementsReport, collectesReport, dechetsReport, recyclageReport;

    // DatePickers
    @FXML private DatePicker startDatePicker, endDatePicker;

    // Labels pour les dates
    @FXML private Label startDateLabel, endDateLabel;

    // Bouton générer
    @FXML private Button generateBtn;

    // Boutons de navigation
    @FXML private Button dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn;

    @FXML
    public void initialize() {
        // Initialiser le label utilisateur
        if (userNameLabel != null) {
            userNameLabel.setText("Admin : Administrateur");
        }

        // Initialiser les statistiques
        updateMonthlyStats();

        // Désactiver les datepickers par défaut
        if (startDatePicker != null) startDatePicker.setDisable(true);
        if (endDatePicker != null) endDatePicker.setDisable(true);
        if (startDateLabel != null) startDateLabel.setDisable(true);
        if (endDateLabel != null) endDateLabel.setDisable(true);

        // Activer les datepickers seulement quand "Personnalisé" est sélectionné
        if (customRadio != null) {
            customRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (startDatePicker != null) startDatePicker.setDisable(!newVal);
                if (endDatePicker != null) endDatePicker.setDisable(!newVal);
                if (startDateLabel != null) startDateLabel.setDisable(!newVal);
                if (endDateLabel != null) endDateLabel.setDisable(!newVal);
            });
        }

        // Sélectionner les valeurs par défaut
        if (monthlyRadio != null) monthlyRadio.setSelected(true);
        if (signalementsReport != null) signalementsReport.setSelected(true);

        // Mettre à jour le style du bouton actif
        if (reportsBtn != null) {
            reportsBtn.setStyle("-fx-background-color: #34495e; -fx-border-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand;");
        }
    }

    private void updateMonthlyStats() {
        // Mettre à jour les statistiques mensuelles
        if (monthSignalements != null) monthSignalements.setText("157");
        if (monthCollectes != null) monthCollectes.setText("89");
        if (monthDechets != null) monthDechets.setText("342");
        if (monthRecyclage != null) monthRecyclage.setText("67%");
    }

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
        chargerPage("/com/example/gestiondechets/Signalements.fxml");
    }

    @FXML
    private void showUsers() {
        chargerPage("/com/example/gestiondechets/gestUsers.fxml");
    }

    @FXML
    private void showReports() {
        // Déjà sur cette page
        updateActiveButton(reportsBtn);
    }

    @FXML
    private void showSettings() {
        chargerPage("/com/example/gestiondechets/settings.fxml");
    }

    @FXML
    private void generateReport() {
        // Récupérer les paramètres
        String periodType = getSelectedPeriodType();
        String reportType = getSelectedReportType();

        // Générer le rapport
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rapport généré");
        alert.setHeaderText(null);
        alert.setContentText("Rapport " + reportType + " pour la période " + periodType + " généré avec succès!");
        alert.showAndWait();
    }

    private String getSelectedPeriodType() {
        if (dailyRadio != null && dailyRadio.isSelected()) return "Journalier";
        if (weeklyRadio != null && weeklyRadio.isSelected()) return "Hebdomadaire";
        if (monthlyRadio != null && monthlyRadio.isSelected()) return "Mensuel";
        if (yearlyRadio != null && yearlyRadio.isSelected()) return "Annuel";
        if (customRadio != null && customRadio.isSelected()) return "Personnalisé";
        return "Mensuel";
    }

    private String getSelectedReportType() {
        if (signalementsReport != null && signalementsReport.isSelected()) return "Signalements";
        if (collectesReport != null && collectesReport.isSelected()) return "Collectes";
        if (dechetsReport != null && dechetsReport.isSelected()) return "Déchets";
        if (recyclageReport != null && recyclageReport.isSelected()) return "Recyclage";
        return "Signalements";
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
            if (generateBtn != null) {
                stage = (Stage) generateBtn.getScene().getWindow();
            } else if (userNameLabel != null) {
                stage = (Stage) userNameLabel.getScene().getWindow();
            } else {
                // Créer une nouvelle fenêtre
                stage = new Stage();
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