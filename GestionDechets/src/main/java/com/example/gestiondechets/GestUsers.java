package com.example.gestiondechets;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GestUsers {




        @FXML private Label userNameLabel, totalUsersLabel, activeUsersLabel, newUsersLabel;
        @FXML private TableView<?> usersTable;
        @FXML private TextField searchField;
        @FXML private ComboBox<String> roleFilter, statusFilter;
        @FXML private Button dashboardBtn, signalementsBtn, usersBtn, reportsBtn, settingsBtn;


        @FXML
        public void initialize() {
            userNameLabel.setText("Administrateur");
            totalUsersLabel.setText("0");
            activeUsersLabel.setText("0");
            newUsersLabel.setText("0");
        }

        @FXML
        private void showCollectes() {
            // Naviguer vers collectes
        }


        @FXML
        private void openAddUserDialog() {
            // Code pour ajouter utilisateur
        }

        @FXML
        private void applyFilters() {
            // Code pour filtrer
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

    }

