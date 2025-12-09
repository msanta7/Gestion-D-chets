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

public class SettingsController {

    @FXML private Label userNameLabel;

    // SEULEMENT CE QUI EXISTE DANS VOTRE FXML :

    // Général
    @FXML private TextField appNameField;
    @FXML private ComboBox<String> languageCombo, timezoneCombo, dateFormatCombo;

    // Notifications
    @FXML private ToggleButton notificationsToggle;
    @FXML private CheckBox emailNotifications, smsNotifications, pushNotifications;

    // Boutons
    @FXML private Button btnSauvegarder, btnReinitialiser;

    // Boutons de navigation
    @FXML private Button dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn;

    @FXML
    public void initialize() {
        userNameLabel.setText("Admin : Administrateur");

        // Initialiser les valeurs par défaut UNIQUEMENT pour ce qui existe
        initialiserValeursParDefaut();

        // Configurer le ToggleButton des notifications
        notificationsToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            notificationsToggle.setText(newVal ? "Notifications activées" : "Notifications désactivées");
            // Activer/désactiver les autres contrôles de notification
            emailNotifications.setDisable(!newVal);
            smsNotifications.setDisable(!newVal);
            pushNotifications.setDisable(!newVal);
        });
    }

    private void initialiserValeursParDefaut() {
        // Paramètres généraux - UNIQUEMENT CE QUI EXISTE
        appNameField.setText("Gestion des Déchets");

        ObservableList<String> languages = FXCollections.observableArrayList(
                "Français", "Anglais", "Arabe", "Espagnol"
        );
        languageCombo.setItems(languages);
        languageCombo.getSelectionModel().select("Français");

        ObservableList<String> timezones = FXCollections.observableArrayList(
                "UTC+1 (Europe/Paris)", "UTC (Londres)", "UTC-5 (New York)", "UTC+3 (Moscou)"
        );
        timezoneCombo.setItems(timezones);
        timezoneCombo.getSelectionModel().selectFirst();

        ObservableList<String> dateFormats = FXCollections.observableArrayList(
                "JJ/MM/AAAA", "MM/JJ/AAAA", "AAAA-MM-JJ"
        );
        dateFormatCombo.setItems(dateFormats);
        dateFormatCombo.getSelectionModel().selectFirst();

        // Paramètres de notification - UNIQUEMENT CE QUI EXISTE
        notificationsToggle.setSelected(true);
        notificationsToggle.setText("Notifications activées");

        emailNotifications.setSelected(true);
        smsNotifications.setSelected(false);
        pushNotifications.setSelected(true);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        alert.setContentText("Toutes les modifications non sauvegardées seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Charger la page de connexion
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/login.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) userNameLabel.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur lors du chargement de la page de connexion : " + e.getMessage());
                }
                Database.logoutUserByPhone(Database.getActiveUser().getTelephone());

            }
        });
    }

    @FXML
    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/DashAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }

    @FXML
    private void showSignalements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/signalements.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des signalements : " + e.getMessage());
        }
    }

    @FXML
    private void showUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/gestUsers.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la gestion des utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    private void showReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/rapports.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des rapports : " + e.getMessage());
        }
    }

    @FXML
    private void showSettings() {
        // Déjà sur cette page, juste mettre à jour le style du bouton actif
        mettreAJourBoutonActif(settingsBtn);
    }

    private void mettreAJourBoutonActif(Button boutonActif) {
        // Réinitialiser tous les styles
        Button[] boutons = {dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn};
        for (Button btn : boutons) {
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand;");
        }

        // Appliquer le style au bouton actif
        boutonActif.setStyle("-fx-background-color: #34495e; -fx-border-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand;");
    }

    @FXML
    private void sauvegarderParametres() {
        // Valider les champs
        if (!validerParametres()) {
            return;
        }

        // Désactiver le bouton pendant la sauvegarde
        btnSauvegarder.setText("Sauvegarde en cours...");
        btnSauvegarder.setDisable(true);

        // Simuler la sauvegarde dans un thread séparé
        new Thread(() -> {
            try {
                // Simulation de la sauvegarde
                Thread.sleep(1500);

                javafx.application.Platform.runLater(() -> {
                    btnSauvegarder.setText("Sauvegarder");
                    btnSauvegarder.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sauvegarde réussie");
                    alert.setHeaderText(null);
                    alert.setContentText("Tous les paramètres ont été sauvegardés avec succès !");

                    alert.showAndWait();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    btnSauvegarder.setText("Sauvegarder");
                    btnSauvegarder.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de sauvegarde");
                    alert.setHeaderText(null);
                    alert.setContentText("Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void reinitialiserParametres() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Réinitialiser les paramètres");
        alert.setContentText("Êtes-vous sûr de vouloir réinitialiser tous les paramètres aux valeurs par défaut ?");

        ButtonType confirmer = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmer, annuler);

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmer) {
                initialiserValeursParDefaut();

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Paramètres réinitialisés");
                info.setHeaderText(null);
                info.setContentText("Tous les paramètres ont été réinitialisés aux valeurs par défaut.");
                info.showAndWait();
            }
        });
    }

    private boolean validerParametres() {
        StringBuilder erreurs = new StringBuilder();

        // Valider le nom de l'application
        if (appNameField.getText().trim().isEmpty()) {
            erreurs.append("• Le nom de l'application est requis\n");
        }

        // Si des erreurs existent, les afficher
        if (erreurs.length() > 0) {
            showError("Veuillez corriger les erreurs suivantes :\n\n" + erreurs.toString());
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}