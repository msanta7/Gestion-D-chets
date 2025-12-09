package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashCitoyendController {

    @FXML private Label nomCitoyenLabel, welcomeLabel;
    @FXML private Label totalSignalementsLabel, enCoursLabel, resolusLabel;
    @FXML private TableView<Signalement> recentSignalementsTable;
    @FXML private TableColumn<Signalement, Integer> idSignalement;
    @FXML private TableColumn<Signalement, String> dateSignalement;
    @FXML private TableColumn<Signalement, String> adresse;
    @FXML private TableColumn<Signalement, String> description;
    @FXML private TableColumn<Signalement, String> statut;

    @FXML private Button dashboardBtn, nouveauSignalementBtn;

    private ObservableList<Signalement> signalementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialiser les données du citoyen
        nomCitoyenLabel.setText("Mohamed Ali");
        welcomeLabel.setText("Bienvenue, Mohamed !");

        // Initialiser le tableau
        initializeTable();

        // Charger les données depuis la base
        loadData();

        // Mettre en surbrillance le bouton actif
        dashboardBtn.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 5; -fx-text-fill: white; -fx-font-size: 14px;");
    }

    private void loadData() {
        updateStats();
        loadSignalementsFromDB();
    }

    private void updateStats() {
        try {
            Connection conn = Database.connectDB();
            if (conn != null) {
                // Get total signalements
                String queryTotal = "SELECT COUNT(*) as total FROM SIGNALEMENT";
                PreparedStatement pstTotal = conn.prepareStatement(queryTotal);
                ResultSet rsTotal = pstTotal.executeQuery();
                if (rsTotal.next()) {
                    totalSignalementsLabel.setText(String.valueOf(rsTotal.getInt("total")));
                }

                // Get en_cours signalements
                String queryEnCours = "SELECT COUNT(*) as en_cours FROM SIGNALEMENT WHERE etat = 'en_cours'";
                PreparedStatement pstEnCours = conn.prepareStatement(queryEnCours);
                ResultSet rsEnCours = pstEnCours.executeQuery();
                if (rsEnCours.next()) {
                    enCoursLabel.setText(String.valueOf(rsEnCours.getInt("en_cours")));
                }

                // Get termine signalements
                String queryResolus = "SELECT COUNT(*) as termines FROM SIGNALEMENT WHERE etat = 'termine'";
                PreparedStatement pstResolus = conn.prepareStatement(queryResolus);
                ResultSet rsResolus = pstResolus.executeQuery();
                if (rsResolus.next()) {
                    resolusLabel.setText(String.valueOf(rsResolus.getInt("termines")));
                }

                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback values
            totalSignalementsLabel.setText("3");
            enCoursLabel.setText("1");
            resolusLabel.setText("1");
        }
    }

    private void initializeTable() {
        // Initialize table columns with property names from your Signalement class
        idSignalement.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateSignalement.setCellValueFactory(new PropertyValueFactory<>("date"));
        adresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        statut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Set the items to the table
        recentSignalementsTable.setItems(signalementList);

        // Optional: Add custom styling for status column
        statut.setCellFactory(column -> new TableCell<Signalement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Apply different styles based on status
                    switch (item.toLowerCase()) {
                        case "nouveau":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "en cours":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "terminé":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "annulé":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void loadSignalementsFromDB() {
        signalementList.clear(); // Clear existing data

        try {
            Connection conn = Database.connectDB();
            if (conn != null) {
                // Query to get recent signalements (all signalements since no citoyen filter)
                String query = "SELECT * FROM SIGNALEMENT ORDER BY date_signalement DESC LIMIT 10";
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    // Format the date
                    LocalDateTime dateTime = rs.getTimestamp("date_signalement").toLocalDateTime();
                    String formattedDate = dateTime.format(formatter);

                    // Translate status to French
                    String etatDB = rs.getString("etat");
                    String statutFrench = translateStatusToFrench(etatDB);

                    // Create Signalement object using your existing class
                    Signalement signalement = new Signalement(
                            rs.getInt("id_signalement"),
                            formattedDate,
                            rs.getString("adresse"),
                            rs.getString("description"),
                            statutFrench
                    );

                    signalementList.add(signalement);
                }


                if (signalementList.isEmpty()) {
                    System.out.println("Aucun signalement trouvé.");

                }

                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les signalements depuis la base de données: " + e.getMessage());

        }
    }

    private String translateStatusToFrench(String etat) {
        if (etat == null) return "Inconnu";

        switch(etat.toLowerCase()) {
            case "nouveau": return "Nouveau";
            case "en_cours": return "En Cours";
            case "termine": return "Terminé";
            case "annule": return "Annulé";
            default: return etat;
        }
    }

    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation
    @FXML
    private void showDashboard() {
        // Refresh data when returning to dashboard
        loadData();
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

    @FXML
    private void ouvrirPopupSignalement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gestiondechets/popup-signalement.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(nomCitoyenLabel.getScene().getWindow());
            popupStage.initStyle(StageStyle.UTILITY);

            PopupSignalementController controller = loader.getController();
            controller.setStage(popupStage);
            // Remove citoyenId since we don't need it
            // controller.setCitoyenId(currentCitoyenId);

            // Set callback to refresh data after adding new signalement
            controller.setOnSignalementAdded(() -> {
                loadData(); // Refresh both stats and table
            });

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setTitle("Nouveau Signalement");
            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le formulaire de signalement.");
            alert.showAndWait();
        }
    }

    @FXML
    private void showNouveauSignalement() {
        ouvrirPopupSignalement();
    }

    @FXML
    private void showTousSignalements() {
        showMesSignalements();
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
        // Reset all buttons to transparent
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");
        nouveauSignalementBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");

        // Highlight active button
        activeButton.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 5; -fx-text-fill: white; -fx-font-size: 14px;");
    }

    // Method to refresh data (can be called from other controllers)
    public void refreshData() {
        loadData();
    }
}