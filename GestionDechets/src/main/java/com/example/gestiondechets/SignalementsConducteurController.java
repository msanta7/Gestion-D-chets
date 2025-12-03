package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class SignalementsConducteurController {

    @FXML private TableView<Signalement> signalementsTable;
    @FXML private TableColumn<Signalement, Integer> idCol;
    @FXML private TableColumn<Signalement, String> dateCol;
    @FXML private TableColumn<Signalement, String> adresseCol;
    @FXML private TableColumn<Signalement, String> descriptionCol;
    @FXML private TableColumn<Signalement, String> statutCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatus;

    @FXML private Label nomConducteurLabel;
    @FXML private Label signalementsAssignesLabel;
    @FXML private Label signalementsNouveauxLabel;
    @FXML private Label signalementsEnCoursLabel;

    private ObservableList<Signalement> signalementsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer le nom du conducteur
        nomConducteurLabel.setText("Conducteur");

        // Configurer les colonnes
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        adresseCol.setCellValueFactory(cellData -> cellData.getValue().adresseProperty());
        descriptionCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        statutCol.setCellValueFactory(cellData -> cellData.getValue().statutProperty());


        // Charger les données
        chargerDonneesTest();

    }

    private void chargerDonneesTest() {
        signalementsList.clear();

        // Données de test pour un conducteur
        signalementsList.add(new Signalement(1001, "15/01/2024 08:30", "123 Rue Principale, Centre-ville", "Déchets encombrants abandonnés sur le trottoir", "Nouveau"));
        signalementsList.add(new Signalement(1002, "14/01/2024 14:15", "456 Avenue du Parc, Quartier Nord", "Poubelle publique renversée par le vent", "Nouveau"));
        signalementsList.add(new Signalement(1003, "13/01/2024 10:45", "789 Boulevard Central, Zone Est", "Conteneur à verre plein à craquer", "En cours"));
        signalementsList.add(new Signalement(1004, "12/01/2024 16:20", "321 Rue Secondaire, Secteur Ouest", "Déchets verts non collectés depuis 3 jours", "Nouveau"));
        signalementsList.add(new Signalement(1005, "11/01/2024 09:10", "654 Place du Marché", "Ordures ménagères répandues par les animaux", "Nouveau"));

        signalementsTable.setItems(signalementsList);
    }


    @FXML
    private void handleFilter() {
        String statutSelectionne = filterStatus.getValue();

        if ("Tous les statuts".equals(statutSelectionne)) {
            signalementsTable.setItems(signalementsList);
        } else {
            ObservableList<Signalement> filteredList = FXCollections.observableArrayList();
            for (Signalement s : signalementsList) {
                if (s.getStatut().equals(statutSelectionne)) {
                    filteredList.add(s);
                }
            }
            signalementsTable.setItems(filteredList);
        }
    }

    @FXML
    private void handleRefresh() {
        // Recharger les données
        chargerDonneesTest();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rafraîchissement");
        alert.setHeaderText(null);
        alert.setContentText("Liste des signalements rafraîchie avec succès !");
        alert.showAndWait();
    }

    private void ouvrirDetailsSignalement(Signalement signalement) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du Signalement #" + signalement.getId());
        dialog.setHeaderText("Signalement du " + signalement.getDate());

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        Label adresseLabel = new Label("Adresse: " + signalement.getAdresse());
        Label descriptionLabel = new Label("Description: " + signalement.getDescription());
        Label statutLabel = new Label("Statut: " + signalement.getStatut());

        adresseLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        descriptionLabel.setStyle("-fx-font-size: 14px;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);
        statutLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        content.getChildren().addAll(adresseLabel, descriptionLabel, statutLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private void modifierStatutSignalement(Signalement signalement) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(signalement.getStatut(),
                "Nouveau", "Assigné", "En cours", "Résolu");
        dialog.setTitle("Modifier le statut");
        dialog.setHeaderText("Signalement #" + signalement.getId());
        dialog.setContentText("Sélectionnez le nouveau statut:");

        dialog.showAndWait().ifPresent(nouveauStatut -> {
            signalement.setStatut(nouveauStatut);
            signalementsTable.refresh();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Statut modifié");
            alert.setHeaderText(null);
            alert.setContentText("Le statut du signalement #" + signalement.getId() + " a été modifié en: " + nouveauStatut);
            alert.showAndWait();
        });
    }

    // === MÉTHODES DE NAVIGATION ===
    @FXML
    private void showDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-conducteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger le tableau de bord");
        }
    }

    @FXML
    private void showCollecte(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("collecte.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page de collecte");
        }
    }

    @FXML
    private void showSignalements(ActionEvent event) {
        // Déjà sur cette page, rien à faire
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
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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