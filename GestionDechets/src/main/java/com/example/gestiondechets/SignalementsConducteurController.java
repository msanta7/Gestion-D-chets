package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import model.Signalements.Signalement;

public class SignalementsConducteurController {

    @FXML private TableView<Signalement> signalementsTable;
    @FXML private TableColumn<Signalement, Integer> idCol;
    @FXML private TableColumn<Signalement, String> dateCol;
    @FXML private TableColumn<Signalement, String> adresseCol;
    @FXML private TableColumn<Signalement, String> descriptionCol;
    @FXML private TableColumn<Signalement, String> statutCol;

    @FXML private Label nomConducteurLabel;

    private ObservableList<Signalement> signalementsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer le nom du conducteur
        nomConducteurLabel.setText("Conducteur");

        // Configurer les colonnes du tableau
        configurerColonnes();

        // Charger les données depuis la base de données
        chargerSignalementsDepuisBD();
    }

    private void configurerColonnes() {
        // Colonne ID
        idCol.setCellValueFactory(cellData -> {
            Signalement signalement = cellData.getValue();
            return new SimpleIntegerProperty(signalement.getId()).asObject();
        });

        // Colonne Date
        dateCol.setCellValueFactory(cellData -> {
            Signalement signalement = cellData.getValue();
            return new SimpleStringProperty(signalement.getDate());
        });

        // Colonne Adresse (utilise localisation)
        adresseCol.setCellValueFactory(cellData -> {
            Signalement signalement = cellData.getValue();
            return new SimpleStringProperty(signalement.getLocalisation());
        });

        // Colonne Description
        descriptionCol.setCellValueFactory(cellData -> {
            Signalement signalement = cellData.getValue();
            return new SimpleStringProperty(signalement.getDescription());
        });

        // Colonne Statut (utilise etat)
        statutCol.setCellValueFactory(cellData -> {
            Signalement signalement = cellData.getValue();
            return new SimpleStringProperty(signalement.getEtat());
        });

        // Optionnel: Ajouter un écouteur pour double-clic
        signalementsTable.setRowFactory(tv -> {
            TableRow<Signalement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Signalement selected = row.getItem();
                    afficherDetailsSignalement(selected);
                }
            });
            return row;
        });
    }

    private void chargerSignalementsDepuisBD() {
        signalementsList.clear();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = Database.connectDB();
            if (conn == null) {
                showError("Erreur de connexion", "Impossible de se connecter à la base de données");
                chargerDonneesTest();
                return;
            }

            stmt = conn.createStatement();

            // Récupérer les signalements depuis la table SIGNALEMENT
            String query = "SELECT id_signalement, date_signalement, adresse, description, etat " +
                    "FROM SIGNALEMENT " +
                    "ORDER BY date_signalement DESC";

            rs = stmt.executeQuery(query);

            int count = 0;
            while (rs.next()) {
                // Créer un Signalement avec les données de la BD
                // Note: La table a moins de champs que la classe Signalement
                Signalement signalement = new Signalement(
                        rs.getInt("id_signalement"),      // id
                        formaterDate(rs.getString("date_signalement")), // date
                        "Non disponible",                 // citoyen (valeur par défaut)
                        "Non spécifié",                   // typeDechet (valeur par défaut)
                        rs.getString("adresse"),          // localisation
                        "Normal",                         // urgence (valeur par défaut)
                        traduireStatut(rs.getString("etat")), // etat
                        "N/A",                            // telephone (valeur par défaut)
                        rs.getString("description")       // description
                );
                signalementsList.add(signalement);
                count++;
            }

            System.out.println("Signalements chargés: " + count);
            signalementsTable.setItems(signalementsList);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de base de données",
                    "Erreur lors du chargement des signalements: " + e.getMessage());

            // Charger des données de test en cas d'erreur
            chargerDonneesTest();
        } finally {
            // Fermer les ressources
            fermerRessources(rs, stmt, conn);
        }
    }

    private String formaterDate(String dateSQL) {
        if (dateSQL == null || dateSQL.isEmpty()) {
            return "Date inconnue";
        }
        try {
            // Si la date est au format SQL (2024-01-15 08:30:00), on peut la formater
            return dateSQL.substring(0, 16); // Garde "2024-01-15 08:30"
        } catch (Exception e) {
            return dateSQL;
        }
    }

    private String traduireStatut(String statutSQL) {
        if (statutSQL == null) return "Inconnu";

        switch (statutSQL.toLowerCase()) {
            case "nouveau": return "Nouveau";
            case "en_cours": return "En cours";
            case "termine": return "Terminé";
            case "annule": return "Annulé";
            default: return statutSQL;
        }
    }

    private void fermerRessources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void afficherDetailsSignalement(Signalement signalement) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du Signalement");
        dialog.setHeaderText("Signalement #" + signalement.getId());

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        // Créer les labels avec les informations
        Label idLabel = new Label("ID: " + signalement.getId());
        Label dateLabel = new Label("Date: " + signalement.getDate());
        Label adresseLabel = new Label("Adresse: " + signalement.getLocalisation());
        Label descriptionLabel = new Label("Description: " + signalement.getDescription());
        Label statutLabel = new Label("Statut: " + signalement.getEtat());
        Label typeLabel = new Label("Type de déchet: " + signalement.getTypeDechet());
        Label urgenceLabel = new Label("Urgence: " + signalement.getUrgence());
        Label citoyenLabel = new Label("Citoyen: " + signalement.getCitoyen());

        // Style des labels
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);
        statutLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #006666;");

        content.getChildren().addAll(
                idLabel, dateLabel, adresseLabel, descriptionLabel,
                statutLabel, typeLabel, urgenceLabel, citoyenLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    // Méthode de secours pour les données de test
    private void chargerDonneesTest() {
        signalementsList.clear();

        System.out.println("Chargement des données de test...");

        signalementsTable.setItems(signalementsList);
        System.out.println("Données de test chargées: " + signalementsList.size() + " signalements");
    }

    // === MÉTHODES DE NAVIGATION ===
    @FXML
    private void showDashboard(ActionEvent event) {
        naviguerVers("DashConducteur.fxml", event);
    }

    @FXML
    private void showCollecte(ActionEvent event) {
        naviguerVers("historique-collecte.fxml", event);
    }

    @FXML
    private void showSignalements(ActionEvent event) {
        // Rafraîchir les données quand on clique sur ce menu
        chargerSignalementsDepuisBD();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                naviguerVers("login.fxml", event);
            }
        });
    }

    private void naviguerVers(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger: " + fxmlFile);
        }
    }

    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}