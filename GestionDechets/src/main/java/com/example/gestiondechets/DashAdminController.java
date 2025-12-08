package com.example.gestiondechets;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class DashAdminController {

    // CORRECTION : Utiliser userNameLabel1 au lieu de userNameLabel
    @FXML private Label userNameLabel;  // Changé de userNameLabel à userNameLabel1

    @FXML private Label signalementsCount, collectesCount, usersCount, recyclingCount;

    @FXML private AnchorPane dashboardView, usersView, settingsView;
    @FXML private TableView<Signalement> recentSignalementsTable;

    // Boutons de navigation
    @FXML private Button dashboard, usersBtn, reportsBtn, settingsBtn;

    // Paramètres
    @FXML private ComboBox<String> languageCombo;
    @FXML private TableColumn<Signalement, Number> colId;
    @FXML private TableColumn<Signalement, String> colAdresse;
    @FXML private TableColumn<Signalement, String> colDescription;
    @FXML private TableColumn<Signalement, String> colEtat;
    @FXML private TableColumn<Signalement, String> colDate;
    @FXML
    public void initialize() {
        // CORRECTION : Utiliser userNameLabel1
        if (userNameLabel != null) {
            userNameLabel.setText(Database.getActiveUser().getNom());
            System.out.println(Database.getUserName(Database.getActiveUser().getNom()));
        }

        // Initialiser les statistiques
        updateStats();

        // Initialiser les vues
        showDashboardView();

        // Initialiser les combobox
        if (languageCombo != null) {
            languageCombo.getSelectionModel().select("Français");
        }
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        colEtat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate()));

        loadThisWeekSignalements();
    }

    private void loadThisWeekSignalements() {
        List<Signalement> list = Database.getSignalementsThisWeek();
        ObservableList<Signalement> obs = FXCollections.observableArrayList(list);
        recentSignalementsTable.setItems(obs);
        System.out.println(obs);
    }

    private void updateStats() {
        if (signalementsCount != null) signalementsCount.setText(String.valueOf(Database.countPendingSignalements()));
        if (collectesCount != null) collectesCount.setText(String.valueOf(Database.getnumberof("collecte")));
        if (usersCount != null) usersCount.setText(String.valueOf(Database.getnumberof("utilisateur")));
        if (recyclingCount != null) recyclingCount.setText("67%");

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
        Button[] buttons = {dashboard, usersBtn, reportsBtn, settingsBtn};
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
                Database.logoutUserByPhone(Database.getActiveUser().getTelephone());
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
                stage = (Stage) userNameLabel.getScene().getWindow();
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