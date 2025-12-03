package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HistoriqueCollectesController implements Initializable {

    @FXML private Label nomAgentCollecteurLabel;
    @FXML private Label messageLabel;


    @FXML private TableView<Collecte> collectesTableView;
    @FXML private TableColumn<Collecte, Integer> idColonne;
    @FXML private TableColumn<Collecte, Integer> idInterventionColonne;
    @FXML private TableColumn<Collecte, String> adresseColonne;
    @FXML private TableColumn<Collecte, String> dateColonne;
    @FXML private TableColumn<Collecte, Double> quantiteColonne;
    @FXML private TableColumn<Collecte, String> statutColonne;
    @FXML private TableColumn<Collecte, String> agentColonne;

    @FXML private Button voirDetailsBtn;
    @FXML private Button supprimerBtn;

    private ObservableList<Collecte> collectesList = FXCollections.observableArrayList();
    private FilteredList<Collecte> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les colonnes
        initializeTableColumns();

        // Charger les données
        loadCollectesData();


        // Gérer la sélection dans le tableau
        setupSelectionListener();

        // Initialiser le label de l'agent
        nomAgentCollecteurLabel.setText("Agent Collecteur #2");
    }

    private void initializeTableColumns() {
        idColonne.setCellValueFactory(new PropertyValueFactory<>("idCollecte"));
        idInterventionColonne.setCellValueFactory(new PropertyValueFactory<>("idIntervention"));

        // Pour l'adresse, utilisez simplement l'ID intervention pour l'instant
        adresseColonne.setCellValueFactory(cellData -> {
            int idIntervention = cellData.getValue().getIdIntervention();
            return new javafx.beans.property.SimpleStringProperty("Intervention #" + idIntervention);
        });

        // Colonne date formatée
        dateColonne.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateCollecte();
            if (date != null) {
                String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        quantiteColonne.setCellValueFactory(new PropertyValueFactory<>("quantiteCollectee"));

        // Pour le statut, utilisez "Terminée" par défaut
        statutColonne.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Terminée"));

        // Pour l'agent collecteur
        agentColonne.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Agent #" + cellData.getValue().getIdAgentCollecteur()));
    }

    private void loadCollectesData() {
        // Charger les données depuis la base de données
        // Exemple avec des données fictives
        collectesList.clear();

        // Créer des objets Collecte selon votre classe existante
        Collecte collecte1 = new Collecte();
        collecte1.setIdCollecte(1);
        collecte1.setIdIntervention(101);
        collecte1.setDateCollecte(LocalDateTime.now().minusDays(2));
        collecte1.setQuantiteCollectee(150.5);
        collecte1.setIdAgentCollecteur(2);

        Collecte collecte2 = new Collecte();
        collecte2.setIdCollecte(2);
        collecte2.setIdIntervention(102);
        collecte2.setDateCollecte(LocalDateTime.now().minusDays(1));
        collecte2.setQuantiteCollectee(200.0);
        collecte2.setIdAgentCollecteur(2);

        Collecte collecte3 = new Collecte();
        collecte3.setIdCollecte(3);
        collecte3.setIdIntervention(103);
        collecte3.setDateCollecte(LocalDateTime.now());
        collecte3.setQuantiteCollectee(175.3);
        collecte3.setIdAgentCollecteur(2);

        collectesList.addAll(collecte1, collecte2, collecte3);

        filteredData = new FilteredList<>(collectesList, p -> true);
        SortedList<Collecte> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(collectesTableView.comparatorProperty());
        collectesTableView.setItems(sortedData);
    }


    private void setupSelectionListener() {
        collectesTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean itemSelected = newValue != null;
                    voirDetailsBtn.setDisable(!itemSelected);
                    supprimerBtn.setDisable(!itemSelected);
                }
        );
    }

    @FXML
    private void ouvrirPopupNouvelleCollecte() {
        try {
            // Charger le formulaire de nouvelle collecte
            FXMLLoader loader = new FXMLLoader(getClass().getResource("effectuer-collecte.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur de la popup
            EffectuerCollecteController popupController = loader.getController();

            // Configurer un callback pour rafraîchir le tableau après ajout
            popupController.setOnCollecteAjoutee(() -> {
                rafraichirTableau();
            });

            // Créer une nouvelle scène pour la popup
            Scene scene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.setTitle("Nouvelle Collecte");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(nomAgentCollecteurLabel.getScene().getWindow());
            popupStage.setScene(scene);
            popupStage.setResizable(false);

            // Afficher la popup
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de collecte", Alert.AlertType.ERROR);
        }
    }

    private void rafraichirTableau() {
        // Rafraîchir les données du tableau
        collectesTableView.getItems().clear();
        loadCollectesData();
        messageLabel.setText("Nouvelle collecte ajoutée avec succès !");
        messageLabel.setStyle("-fx-text-fill: #2ecc71;");
    }


    @FXML
    private void voirDetailsCollecte() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dateFormatted = selected.getDateCollecte() != null ?
                    selected.getDateCollecte().format(formatter) : "N/A";

            // Afficher les détails
            showAlert("Détails",
                    String.format("ID: %d\nIntervention: %d\nDate: %s\nQuantité: %.1f kg\nAgent: %d",
                            selected.getIdCollecte(),
                            selected.getIdIntervention(),
                            dateFormatted,
                            selected.getQuantiteCollectee(),
                            selected.getIdAgentCollecteur()),
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void supprimerCollecte() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer la collecte #" + selected.getIdCollecte());
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette collecte ? Cette action est irréversible.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Ici, supprimer de la base de données
                // collecteDAO.delete(selected.getIdCollecte());

                collectesList.remove(selected);
                messageLabel.setText("Collecte #" + selected.getIdCollecte() + " supprimée avec succès");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashConducteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void effectuerNouvelleCollecte() {
        // Cette méthode n'est plus utilisée si vous avez la popup
        // Vous pouvez la supprimer ou la garder comme alternative
        ouvrirPopupNouvelleCollecte();
    }

    @FXML
    private void historiqueCollectes() {
        // Déjà sur cette page
        messageLabel.setText("Vous êtes déjà sur la page historique");
        messageLabel.setStyle("-fx-text-fill: #f39c12;");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Se déconnecter");
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Interface pour le callback de rafraîchissement
    public interface CollecteAjouteeCallback {
        void onCollecteAjoutee();
    }
}