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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.TableCell;

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

    @FXML private Button supprimerBtn;

    private ObservableList<Collecte> collectesList = FXCollections.observableArrayList();
    private int currentAgentId = 2; // L'ID de l'agent actuellement connecté

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les colonnes
        initializeTableColumns();

        // Charger les données depuis la base de données
        loadCollectesFromDB();

        // Gérer la sélection dans le tableau
        setupSelectionListener();

        // Initialiser le label de l'agent
        nomAgentCollecteurLabel.setText("Agent Collecteur #" + currentAgentId);


        supprimerBtn.setDisable(true);
    }

    private void initializeTableColumns() {
        // ID Collecte
        idColonne.setCellValueFactory(new PropertyValueFactory<>("idCollecte"));

        // Date formatée
        dateColonne.setCellValueFactory(cellData -> {
            Timestamp date = cellData.getValue().getDateCollecte();
            if (date != null) {
                LocalDateTime localDateTime = date.toLocalDateTime();
                String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Quantité avec format
        quantiteColonne.setCellValueFactory(new PropertyValueFactory<>("quantiteCollectee"));
        quantiteColonne.setCellFactory(new Callback<TableColumn<Collecte, Double>, TableCell<Collecte, Double>>() {
            @Override
            public TableCell<Collecte, Double> call(TableColumn<Collecte, Double> param) {
                return new TableCell<Collecte, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(String.format("%.2f kg", item));
                        }
                    }
                };
            }
        });

        // Statut - Note: Votre FXML utilise "idIntervention" mais on va l'utiliser pour le statut
        statutColonne.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut()));

        // Colorer le statut
        statutColonne.setCellFactory(new Callback<TableColumn<Collecte, String>, TableCell<Collecte, String>>() {
            @Override
            public TableCell<Collecte, String> call(TableColumn<Collecte, String> param) {
                return new TableCell<Collecte, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            // Appliquer les couleurs selon le statut
                            if (item.equalsIgnoreCase("Terminée")) {
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            } else if (item.equalsIgnoreCase("En Cours")) {
                                setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            } else if (item.equalsIgnoreCase("Planifiée")) {
                                setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            } else if (item.equalsIgnoreCase("Annulée")) {
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #7f8c8d;");
                            }
                        }
                    }
                };
            }
        });
    }

    private void loadCollectesFromDB() {
        collectesList.clear();

        try (Connection conn = Database.connectDB()) {
            if (conn != null) {
                // Requête pour récupérer les collectes avec le statut
                String query = """
                    SELECT 
                        c.id_collecte,
                        c.date_collecte,
                        c.quantite_collectee,
                        c.id_intervention,
                        i.statut
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
                            collecte.setIdIntervention(rs.getInt("id_intervention"));
                            collecte.setDateCollecte(rs.getTimestamp("date_collecte"));
                            collecte.setQuantiteCollectee(rs.getDouble("quantite_collectee"));
                            collecte.setStatut(translateStatut(rs.getString("statut")));

                            collectesList.add(collecte);
                            count++;
                        }

                        // Mettre à jour le message
                        if (count == 0) {
                            messageLabel.setText("Aucune collecte trouvée pour cet agent.");
                            messageLabel.setStyle("-fx-text-fill: #f39c12;");
                        } else {
                            messageLabel.setText(count + " collecte(s) trouvée(s)");
                            messageLabel.setStyle("-fx-text-fill: #2ecc71;");
                        }

                        // Mettre à jour le tableau
                        collectesTableView.setItems(collectesList);
                    }
                }

            } else {
                messageLabel.setText("Erreur de connexion à la base de données");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                addSampleDataForTesting();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Erreur lors du chargement: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            addSampleDataForTesting();
        }
    }

    private String translateStatut(String statut) {
        if (statut == null) return "Inconnu";

        switch(statut.toLowerCase()) {
            case "planifiee": return "Planifiée";
            case "en_cours": return "En Cours";
            case "terminee": return "Terminée";
            case "annulee": return "Annulée";
            default: return statut;
        }
    }

    private void addSampleDataForTesting() {
        Collecte collecte1 = new Collecte();
        collecte1.setIdCollecte(1);
        collecte1.setIdIntervention(101);
        collecte1.setDateCollecte(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        collecte1.setQuantiteCollectee(150.5);
        collecte1.setStatut("Terminée");

        Collecte collecte2 = new Collecte();
        collecte2.setIdCollecte(2);
        collecte2.setIdIntervention(102);
        collecte2.setDateCollecte(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        collecte2.setQuantiteCollectee(89.3);
        collecte2.setStatut("Terminée");

        Collecte collecte3 = new Collecte();
        collecte3.setIdCollecte(3);
        collecte3.setIdIntervention(103);
        collecte3.setDateCollecte(Timestamp.valueOf(LocalDateTime.now().minusHours(5)));
        collecte3.setQuantiteCollectee(45.7);
        collecte3.setStatut("En Cours");

        Collecte collecte4 = new Collecte();
        collecte4.setIdCollecte(4);
        collecte4.setIdIntervention(104);
        collecte4.setDateCollecte(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        collecte4.setQuantiteCollectee(0.0);
        collecte4.setStatut("Planifiée");

        collectesList.addAll(collecte1, collecte2, collecte3, collecte4);
        collectesTableView.setItems(collectesList);

        messageLabel.setText("Données de test chargées (4 collectes)");
        messageLabel.setStyle("-fx-text-fill: #f39c12;");
    }

    private void setupSelectionListener() {
        collectesTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean itemSelected = newValue != null;

                    supprimerBtn.setDisable(!itemSelected);
                }
        );
    }

    @FXML
    private void ouvrirPopupNouvelleCollecte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("effectuer-collecte.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Nouvelle Collecte");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(nomAgentCollecteurLabel.getScene().getWindow());
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);

            // Rafraîchir après fermeture de la popup
            popupStage.setOnHidden(event -> {
                loadCollectesFromDB();
            });

            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de collecte", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void voirDetailsCollecte() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try (Connection conn = Database.connectDB()) {
                if (conn != null) {
                    String query = """
                        SELECT 
                            c.*,
                            i.statut as statut_intervention,
                            s.adresse,
                            s.description as description_signalement,
                            u.nom as nom_conducteur,
                            u.telephone as telephone_conducteur,
                            a.nom as nom_agent
                        FROM COLLECTE c
                        LEFT JOIN INTERVENTION i ON c.id_intervention = i.id_intervention
                        LEFT JOIN SIGNALEMENT s ON i.id_signalement = s.id_signalement
                        LEFT JOIN UTILISATEUR u ON i.id_conducteur = u.id_utilisateur
                        LEFT JOIN UTILISATEUR a ON c.id_agent_collecteur = a.id_utilisateur
                        WHERE c.id_collecte = ?
                    """;

                    try (PreparedStatement pst = conn.prepareStatement(query)) {
                        pst.setInt(1, selected.getIdCollecte());
                        try (ResultSet rs = pst.executeQuery()) {

                            if (rs.next()) {
                                // Formater la date
                                String dateFormatted = "";
                                Timestamp date = rs.getTimestamp("date_collecte");
                                if (date != null) {
                                    LocalDateTime localDateTime = date.toLocalDateTime();
                                    dateFormatted = localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
                                }

                                // Construire la boîte de dialogue de détails
                                Dialog<Void> dialog = new Dialog<>();
                                dialog.setTitle("Détails de la Collecte");
                                dialog.setHeaderText("Collecte #" + selected.getIdCollecte());

                                VBox content = new VBox(10);
                                content.setStyle("-fx-padding: 20;");
                                content.setPrefWidth(500);

                                // Ajouter les informations
                                Label idLabel = new Label("ID Collecte: " + selected.getIdCollecte());
                                Label interventionLabel = new Label("ID Intervention: " + selected.getIdIntervention());
                                Label dateLabel = new Label("Date: " + dateFormatted);
                                Label quantiteLabel = new Label("Quantité: " + String.format("%.2f kg", selected.getQuantiteCollectee()));
                                Label adresseLabel = new Label("Adresse: " +
                                        (rs.getString("adresse") != null ? rs.getString("adresse") : "N/A"));
                                Label statutLabel = new Label("Statut: " + translateStatut(rs.getString("statut_intervention")));
                                Label descriptionLabel = new Label("Description: " +
                                        (rs.getString("description_signalement") != null ?
                                                rs.getString("description_signalement") : "Aucune description"));
                                Label conducteurLabel = new Label("Conducteur: " +
                                        (rs.getString("nom_conducteur") != null ?
                                                rs.getString("nom_conducteur") : "Non assigné"));
                                Label telephoneLabel = new Label("Téléphone: " +
                                        (rs.getString("telephone_conducteur") != null ?
                                                rs.getString("telephone_conducteur") : "N/A"));
                                Label agentLabel = new Label("Agent collecteur: " +
                                        (rs.getString("nom_agent") != null ?
                                                rs.getString("nom_agent") : "Agent #" + currentAgentId));

                                // Mettre en forme
                                idLabel.setStyle("-fx-font-weight: bold;");
                                statutLabel.setStyle("-fx-font-weight: bold;");
                                descriptionLabel.setWrapText(true);

                                content.getChildren().addAll(
                                        idLabel, interventionLabel, dateLabel, quantiteLabel,
                                        new Separator(),
                                        adresseLabel,
                                        new Separator(),
                                        statutLabel, descriptionLabel,
                                        new Separator(),
                                        conducteurLabel, telephoneLabel, agentLabel
                                );

                                ScrollPane scrollPane = new ScrollPane(content);
                                scrollPane.setFitToWidth(true);
                                scrollPane.setPrefHeight(400);

                                dialog.getDialogPane().setContent(scrollPane);
                                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                                dialog.getDialogPane().setPrefSize(550, 450);

                                dialog.showAndWait();

                            } else {
                                showAlert("Erreur", "Aucun détail trouvé pour cette collecte", Alert.AlertType.ERROR);
                            }
                        }
                    }
                } else {
                    showAlert("Erreur", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger les détails: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une collecte pour voir les détails", Alert.AlertType.WARNING);
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

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = Database.connectDB()) {
                        if (conn != null) {
                            // Vérifier s'il y a des déchets liés à cette collecte
                            String checkQuery = "SELECT COUNT(*) FROM DECHET WHERE id_collecte = ?";
                            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                                checkStmt.setInt(1, selected.getIdCollecte());
                                try (ResultSet rs = checkStmt.executeQuery()) {

                                    if (rs.next() && rs.getInt(1) > 0) {
                                        showAlert("Impossible de supprimer",
                                                "Cette collecte a des déchets associés. Supprimez d'abord les déchets.",
                                                Alert.AlertType.ERROR);
                                        return;
                                    }
                                }
                            }

                            // Supprimer la collecte
                            String deleteQuery = "DELETE FROM COLLECTE WHERE id_collecte = ?";
                            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                                deleteStmt.setInt(1, selected.getIdCollecte());

                                int rowsAffected = deleteStmt.executeUpdate();

                                if (rowsAffected > 0) {
                                    // Supprimer de la liste
                                    collectesList.remove(selected);
                                    collectesTableView.refresh();

                                    messageLabel.setText("Collecte #" + selected.getIdCollecte() + " supprimée avec succès");
                                    messageLabel.setStyle("-fx-text-fill: #2ecc71;");

                                    // Réinitialiser la sélection
                                    collectesTableView.getSelectionModel().clearSelection();

                                    supprimerBtn.setDisable(true);

                                } else {
                                    showAlert("Erreur", "Aucune collecte supprimée", Alert.AlertType.ERROR);
                                }
                            }
                        } else {
                            showAlert("Erreur", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une collecte à supprimer", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DashConducteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Se déconnecter");
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Impossible de charger la page de connexion", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Note: J'ai supprimé la référence à adresseColonne car elle n'existe pas dans votre FXML
    // Si vous voulez afficher l'adresse, ajoutez d'abord la colonne dans votre FXML
}