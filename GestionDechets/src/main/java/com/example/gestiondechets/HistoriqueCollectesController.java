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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HistoriqueCollectesController implements Initializable {

    @FXML private Label nomAgentCollecteurLabel;
    @FXML private Label messageLabel;
    @FXML private TableView<Collecte> collectesTableView;
    @FXML private TableColumn<Collecte, Integer> idColonne;
    @FXML private TableColumn<Collecte, String> dateColonne;
    @FXML private TableColumn<Collecte, Double> quantiteColonne;
    @FXML private TableColumn<Collecte, String> statutColonne;

    @FXML private Button voirDetailsBtn;
    @FXML private Button supprimerBtn;

    private ObservableList<Collecte> collectesList = FXCollections.observableArrayList();
    private int currentAgentId = 2; // ID par défaut

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("=== INITIALISATION HISTORIQUE COLLECTES ===");

        // Vérifier que tous les éléments FXML sont injectés
        verifyFXMLElements();

        // Configurer les colonnes de la table
        setupTableColumns();

        // Charger les données
        loadCollectesData();

        // Configurer les boutons
        setupButtons();

        System.out.println("=== INITIALISATION TERMINÉE ===");
    }

    private void verifyFXMLElements() {
        System.out.println("Vérification des éléments FXML:");
        System.out.println("TableView: " + (collectesTableView != null ? "✓" : "✗ NULL"));
        System.out.println("idColonne: " + (idColonne != null ? "✓" : "✗ NULL"));
        System.out.println("dateColonne: " + (dateColonne != null ? "✓" : "✗ NULL"));
        System.out.println("quantiteColonne: " + (quantiteColonne != null ? "✓" : "✗ NULL"));
        System.out.println("statutColonne: " + (statutColonne != null ? "✓" : "✗ NULL"));
        System.out.println("messageLabel: " + (messageLabel != null ? "✓" : "✗ NULL"));

        if (collectesTableView == null) {
            System.out.println("ERREUR: La TableView est null ! Vérifiez les fx:id dans le FXML.");
        }
    }

    private void setupTableColumns() {
        System.out.println("Configuration des colonnes de la table...");

        // 1. Colonne ID
        idColonne.setCellValueFactory(new PropertyValueFactory<>("idCollecte"));
        idColonne.setCellFactory(column -> new TableCell<Collecte, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item);
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
                }
            }
        });

        // 2. Colonne Date
        dateColonne.setCellValueFactory(cellData -> {
            Collecte collecte = cellData.getValue();
            if (collecte != null && collecte.getDateCollecte() != null) {
                LocalDateTime date = collecte.getDateCollecte().toLocalDateTime();
                String formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // 3. Colonne Quantité
        quantiteColonne.setCellValueFactory(new PropertyValueFactory<>("quantiteCollectee"));
        quantiteColonne.setCellFactory(column -> new TableCell<Collecte, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f kg", item));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // 4. Colonne Statut
        statutColonne.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutColonne.setCellFactory(column -> new TableCell<Collecte, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Couleurs selon le statut
                    switch(item.toLowerCase()) {
                        case "terminée":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        case "en cours":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        case "planifiée":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        case "annulée":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        System.out.println("Colonnes configurées avec succès");
    }

    private void loadCollectesData() {
        System.out.println("Chargement des données...");

        // Effacer la liste existante
        collectesList.clear();

        // Essayer de charger depuis la base de données
        boolean loadedFromDB = loadFromDatabase();

        // Si échec, charger des données de test
        if (!loadedFromDB) {
            loadTestData();
        }

        // Assigner la liste à la TableView
        collectesTableView.setItems(collectesList);

        // Mettre à jour le message
        updateMessage();

        System.out.println("Données chargées: " + collectesList.size() + " collectes");
    }

    private boolean loadFromDatabase() {
        System.out.println("Tentative de chargement depuis la base de données...");

        try (Connection conn = Database.connectDB()) {
            if (conn == null) {
                System.out.println("❌ Connexion DB échouée");
                return false;
            }

            String query = """
                SELECT 
                    c.id_collecte,
                    c.date_collecte,
                    c.quantite_collectee,
                    c.id_intervention,
                    COALESCE(i.statut, 'inconnu') as statut
                FROM COLLECTE c
                LEFT JOIN INTERVENTION i ON c.id_intervention = i.id_intervention
                WHERE c.id_agent_collecteur = ?
                ORDER BY c.date_collecte DESC
            """;

            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, currentAgentId);

                try (ResultSet rs = pst.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        Collecte collecte = new Collecte();
                        collecte.setIdCollecte(rs.getInt("id_collecte"));
                        collecte.setDateCollecte(rs.getTimestamp("date_collecte"));
                        collecte.setQuantiteCollectee(rs.getDouble("quantite_collectee"));
                        collecte.setIdIntervention(rs.getInt("id_intervention"));
                        collecte.setStatut(formatStatut(rs.getString("statut")));

                        collectesList.add(collecte);
                        count++;
                    }

                    System.out.println("✅ " + count + " collectes chargées depuis la DB");
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadTestData() {
        System.out.println("Chargement des données de test...");

        // Créer des données de test
        for (int i = 1; i <= 5; i++) {
            Collecte collecte = new Collecte();
            collecte.setIdCollecte(i);
            collecte.setDateCollecte(Timestamp.valueOf(LocalDateTime.now().minusDays(i)));
            collecte.setQuantiteCollectee(25.5 * i);
            collecte.setIdIntervention(100 + i);

            switch (i % 4) {
                case 0: collecte.setStatut("Terminée"); break;
                case 1: collecte.setStatut("En Cours"); break;
                case 2: collecte.setStatut("Planifiée"); break;
                case 3: collecte.setStatut("Annulée"); break;
            }

            collectesList.add(collecte);
        }

        System.out.println("✅ Données de test chargées");
    }

    private String formatStatut(String statutDB) {
        if (statutDB == null) return "Inconnu";

        switch (statutDB.toLowerCase()) {
            case "terminee": return "Terminée";
            case "en_cours": return "En Cours";
            case "planifiee": return "Planifiée";
            case "annulee": return "Annulée";
            default: return statutDB;
        }
    }

    private void updateMessage() {
        if (messageLabel == null) return;

        int count = collectesList.size();
        if (count == 0) {
            messageLabel.setText("Aucune collecte trouvée");
            messageLabel.setStyle("-fx-text-fill: #f39c12;");
        } else {
            messageLabel.setText(count + " collecte(s) affichée(s)");
            messageLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }

    private void setupButtons() {
        // Désactiver les boutons par défaut
        if (voirDetailsBtn != null) voirDetailsBtn.setDisable(true);
        if (supprimerBtn != null) supprimerBtn.setDisable(true);

        // Gérer la sélection dans le tableau
        collectesTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    if (voirDetailsBtn != null) voirDetailsBtn.setDisable(!hasSelection);
                    if (supprimerBtn != null) supprimerBtn.setDisable(!hasSelection);
                }
        );
    }

    // ========== ACTIONS DES BOUTONS ==========

    @FXML
    private void handleNouvelleCollecte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("effectuer-collecte.fxml"));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.setTitle("Nouvelle Collecte");
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setScene(new Scene(root));

            popup.setOnHidden(e -> {
                // Recharger les données après fermeture
                loadCollectesData();
            });

            popup.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleVoirDetails() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner une collecte", Alert.AlertType.WARNING);
            return;
        }

        // Créer une boîte de dialogue de détails
        Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
        detailsDialog.setTitle("Détails de la collecte");
        detailsDialog.setHeaderText("Collecte #" + selected.getIdCollecte());

        StringBuilder content = new StringBuilder();
        content.append("ID Collecte: ").append(selected.getIdCollecte()).append("\n");

        if (selected.getDateCollecte() != null) {
            LocalDateTime date = selected.getDateCollecte().toLocalDateTime();
            content.append("Date: ").append(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append("\n");
        }

        content.append("Quantité: ").append(String.format("%.1f kg", selected.getQuantiteCollectee())).append("\n");
        content.append("Statut: ").append(selected.getStatut()).append("\n");
        content.append("ID Intervention: ").append(selected.getIdIntervention());

        detailsDialog.setContentText(content.toString());
        detailsDialog.showAndWait();
    }

    @FXML
    private void handleSupprimer() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la collecte #" + selected.getIdCollecte());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette collecte ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Supprimer de la base de données
                    boolean deleted = deleteFromDatabase(selected.getIdCollecte());

                    if (deleted) {
                        // Supprimer de la liste
                        collectesList.remove(selected);
                        collectesTableView.refresh();
                        updateMessage();

                        showAlert("Succès", "Collecte supprimée avec succès", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Erreur", "Échec de la suppression", Alert.AlertType.ERROR);
                    }

                } catch (Exception e) {
                    showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private boolean deleteFromDatabase(int idCollecte) {
        try (Connection conn = Database.connectDB()) {
            if (conn == null) return false;

            String query = "DELETE FROM COLLECTE WHERE id_collecte = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, idCollecte);
                int rows = pst.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.out.println("Erreur suppression: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void handleRetour() {
        try {
            Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("DashConducteur.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner au dashboard", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Se déconnecter");
        confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
                    Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    showAlert("Erreur", "Impossible de charger la page de connexion", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Rafraîchissement des données...");
        loadCollectesData();
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}