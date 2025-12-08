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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class GestUsers {


    private Utilisateur selectedUser;

    @FXML
    private Label userNameLabel, totalUsersLabel, activeUsersLabel, newUsersLabel;
    @FXML
    private TableView<Utilisateur> usersTable;
    @FXML
    private TableColumn<Utilisateur, Number> userId;
    @FXML
    private TableColumn<Utilisateur, String> accountName;
    @FXML
    private TableColumn<Utilisateur, String> userAdresse;
    @FXML
    private TableColumn<Utilisateur, String> phoneNumber;
    @FXML
    private TableColumn<Utilisateur, String> role;
    @FXML
    private TableColumn<Utilisateur, String> creationDate;
    @FXML
    private TableColumn<Utilisateur, String> statut;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilter, statusFilter;
    @FXML
    private Button dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn;
    @FXML
    private Button deleteUser;

    @FXML
    public void initialize() {
        userNameLabel.setText(Database.getActiveUser().getNom());
        totalUsersLabel.setText(String.valueOf(Database.getTotalUsers()));
        activeUsersLabel.setText(String.valueOf(Database.getActiveUsers()));
        newUsersLabel.setText(String.valueOf(Database.getNewUsersLast3Days()));

        userId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        accountName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        userAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        phoneNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelephone()));
        role.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        creationDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCreationDate()));
        statut.setCellValueFactory(data -> {
            boolean active = data.getValue().isEstactive();
            String text = active ? "Active" : "Not Active";
            return new SimpleStringProperty(text);

        });
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = Database.getSelectedUser(usersTable);
                deleteUser.setDisable(false);
            }
        });
        LoadUsers();

    }

    private void LoadUsers() {
        List<Utilisateur> list = Database.getAllUsers();
        ObservableList<Utilisateur> obs = FXCollections.observableArrayList(list);
        usersTable.setItems(obs);
    }

    @FXML
    private void openAddUserDialog() {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/addUser.fxml"));
            Parent root = loader.load();

            // Créer la fenêtre popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(userNameLabel.getScene().getWindow());
            popupStage.initStyle(StageStyle.UTILITY);

            // Configurer le contrôleur
            AddUserController controller = loader.getController();
            controller.setStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setTitle("Ajouter un utilisateur");
            popupStage.setResizable(false);
            popupStage.showAndWait();

            // Rafraîchir les données après fermeture
            // Vous pouvez appeler une méthode pour actualiser le tableau ici

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le formulaire d'ajout.");
            alert.showAndWait();
        }
    }

    @FXML
    private void applyFilters() {
        String search = searchField.getText();
        String role = roleFilter.getValue();         // "Tous", "citoyen", ...
        String statut = statusFilter.getValue();     // "Tous", "Actif", "Inactif"

        List<Utilisateur> results = Database.filterUsers(search, role, statut);

        usersTable.setItems(FXCollections.observableArrayList(results));
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
            }
        });
        Database.logoutUserByPhone(Database.getActiveUser().getTelephone());
    }

    @FXML
    private void showDashboard() {
        try {
            URL url = getClass().getResource("/com/example/gestiondechets/DashAdmin.fxml");
            if (url == null) {
                showError("Le fichier DashAdmin.fxml n'a pas été trouvé à l'emplacement : /com/example/gestiondechets/DashAdmin.fxml");
                return;
            }

            System.out.println("Chargement du FXML depuis : " + url);

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du tableau de bord : " + e.getMessage() +
                    "\nCause : " + (e.getCause() != null ? e.getCause().getMessage() : "Aucune cause"));
        }
    }

    @FXML
    private void showSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/settings.fxml"));
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
    private void showUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/utilisateurs.fxml"));
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
@FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            // No user selected
            Alert noSelection = new Alert(Alert.AlertType.WARNING);
            noSelection.setTitle("Aucun utilisateur sélectionné");
            noSelection.setHeaderText(null);
            noSelection.setContentText("Veuillez sélectionner un utilisateur avant de continuer.");
            noSelection.showAndWait();
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Vous êtes sur le point de supprimer définitivement cet utilisateur !");
        alert.setContentText("Cette action est irréversible. Voulez-vous continuer ?");

        // Wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed → delete
            Database.deleteUser(selectedUser.getId());
            deleteUser.setDisable(true);

            // Optional: show information after deletion
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Utilisateur supprimé");
            info.setHeaderText(null);
            info.setContentText("L'utilisateur a été supprimé avec succès.");
            info.showAndWait();
        } else {
            // User cancelled → do nothing
            System.out.println("Suppression annulée par l'utilisateur.");
        }
    }
}


