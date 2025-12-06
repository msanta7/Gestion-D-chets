package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashConducteurController implements Initializable {

    @FXML
    private Label nomConducteurLabel;
    @FXML
    private Label interventionsPlanifieesLabel;
    @FXML
    private Label collectesEffectueesLabel;
    @FXML
    private Label signalementsAssignesLabel;
    @FXML
    private TableView<Intervention> interventionsTable;
    @FXML
    private TableColumn<Intervention, Integer> idIntervention;
    @FXML
    private TableColumn<Intervention, String> datePlanification;
    @FXML
    private TableColumn<Intervention, String> adresseSignalement;
    @FXML
    private TableColumn<Intervention, String> dateRealisation;
    @FXML
    private TableColumn<Intervention, String> statu;
    @FXML
    private TableColumn<Intervention, Void> actionsColumn;
    @FXML
    private Button dashboardBtn;
    @FXML
    private Button collecteBtn;
    @FXML
    private Button signalementsBtn;

    private ObservableList<Intervention> interventionsList;
    private int idConducteurConnecte = 0; // Initialiser à 0
    private String nomConducteur = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer l'ID du conducteur connecté
        loadConducteurInfo();

        // Initialiser la table
        setupTableView();

        // Charger les données depuis la base de données
        loadDataFromDatabase();

        // Mettre à jour les statistiques
        updateStatistics();

        // Configurer les actions des boutons
        setupButtonActions();
    }

    private void loadConducteurInfo() {
        try {
            Connection conn = Database.connectDB();
            if (conn != null) {
                // Récupérer le premier conducteur disponible
                String query = "SELECT id_utilisateur, nom FROM UTILISATEUR WHERE role = 'conducteur' LIMIT 1";
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    idConducteurConnecte = rs.getInt("id_utilisateur");
                    nomConducteur = rs.getString("nom");
                    nomConducteurLabel.setText(nomConducteur + " (Conducteur)");
                } else {
                    // Si aucun conducteur n'existe, insérer-en un
                    String insertQuery = """
                        INSERT INTO UTILISATEUR (nom, telephone, adresse, role, mot_de_passe) 
                        VALUES ('Conducteur Test', '0612345678', 'Adresse Test', 'conducteur', 'password123')
                    """;
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                    insertStmt.executeUpdate();

                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        idConducteurConnecte = generatedKeys.getInt(1);
                        nomConducteur = "Conducteur Test";
                        nomConducteurLabel.setText(nomConducteur + " (Conducteur)");
                    }
                }

                conn.close();

                if (idConducteurConnecte == 0) {
                    showAlert("Erreur", "Aucun conducteur trouvé dans la base de données", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les informations du conducteur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTableView() {
        // Configurer les colonnes
        idIntervention.setCellValueFactory(new PropertyValueFactory<>("idIntervention"));
        datePlanification.setCellValueFactory(new PropertyValueFactory<>("datePlanificationFormatted"));
        adresseSignalement.setCellValueFactory(new PropertyValueFactory<>("adresseSignalement"));
        dateRealisation.setCellValueFactory(new PropertyValueFactory<>("dateRealisationFormatted"));
        statu.setCellValueFactory(new PropertyValueFactory<>("statutFormatted"));

        // Configurer la colonne Actions
        setupActionsColumn();

        // Initialiser la liste observable
        interventionsList = FXCollections.observableArrayList();
        interventionsTable.setItems(interventionsList);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Intervention, Void>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button terminerBtn = new Button("Terminer");
            private final Button annulerBtn = new Button("Annuler");
            private final Button supprimerBtn = new Button("Supprimer");

            {
                // Style des boutons
                modifierBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10;");
                terminerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10;");
                annulerBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10;");
                supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10;");

                // Actions des boutons
                modifierBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    modifierIntervention(intervention);
                });

                terminerBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    terminerIntervention(intervention);
                });

                annulerBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    annulerIntervention(intervention);
                });

                supprimerBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    supprimerIntervention(intervention);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    HBox buttonsContainer = new HBox(5);

                    // Afficher les boutons selon le statut
                    String statut = intervention.getStatut();

                    if (statut.equals("planifiee") || statut.equals("en_cours")) {
                        buttonsContainer.getChildren().addAll(modifierBtn, terminerBtn, annulerBtn);
                    } else if (statut.equals("terminee")) {
                        buttonsContainer.getChildren().addAll(modifierBtn, supprimerBtn);
                    } else if (statut.equals("annulee")) {
                        buttonsContainer.getChildren().addAll(modifierBtn, supprimerBtn);
                    }

                    setGraphic(buttonsContainer);
                }
            }
        });
    }

    private void loadDataFromDatabase() {
        try {
            Connection conn = Database.connectDB();
            if (conn != null) {
                // Vérifier que l'ID du conducteur est valide
                if (idConducteurConnecte == 0) {
                    showAlert("Erreur", "ID conducteur invalide", Alert.AlertType.ERROR);
                    return;
                }

                String query = """
                    SELECT 
                        i.id_intervention,
                        i.date_planification,
                        i.date_realisation,
                        i.statut,
                        i.notes,
                        i.id_conducteur,
                        i.id_signalement,
                        s.adresse as adresse_signalement
                    FROM INTERVENTION i
                    INNER JOIN SIGNALEMENT s ON i.id_signalement = s.id_signalement
                    WHERE i.id_conducteur = ?
                    ORDER BY i.date_planification DESC
                """;

                PreparedStatement pst = conn.prepareStatement(query);
                pst.setInt(1, idConducteurConnecte);
                ResultSet rs = pst.executeQuery();

                interventionsList.clear();

                while (rs.next()) {
                    Intervention intervention = new Intervention();
                    intervention.setIdIntervention(rs.getInt("id_intervention"));

                    // Date de planification
                    java.sql.Timestamp planifTimestamp = rs.getTimestamp("date_planification");
                    if (planifTimestamp != null) {
                        intervention.setDatePlanification(planifTimestamp.toLocalDateTime());
                    }

                    // Date de réalisation
                    java.sql.Timestamp realTimestamp = rs.getTimestamp("date_realisation");
                    if (realTimestamp != null) {
                        intervention.setDateRealisation(realTimestamp.toLocalDateTime());
                    }

                    intervention.setStatut(rs.getString("statut"));
                    intervention.setNotes(rs.getString("notes"));
                    intervention.setIdConducteur(rs.getInt("id_conducteur"));
                    intervention.setIdSignalement(rs.getInt("id_signalement"));
                    intervention.setAdresseSignalement(rs.getString("adresse_signalement"));

                    interventionsList.add(intervention);
                }

                conn.close();

                if (interventionsList.isEmpty()) {
                    // Pas d'alerte, juste un message dans la console
                    System.out.println("Aucune intervention trouvée pour ce conducteur.");
                }

            } else {
                showAlert("Erreur", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de base de données", "Erreur lors du chargement des interventions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        try {
            Connection conn = Database.connectDB();
            if (conn != null) {
                // Vérifier que l'ID du conducteur est valide
                if (idConducteurConnecte == 0) {
                    calculateLocalStatistics();
                    return;
                }

                LocalDate aujourdhui = LocalDate.now();
                String aujourdhuiStr = aujourdhui.toString();

                // 1. Interventions planifiées pour aujourd'hui
                String queryPlanifiees = """
                    SELECT COUNT(*) as count 
                    FROM INTERVENTION 
                    WHERE id_conducteur = ? 
                    AND statut IN ('planifiee', 'en_cours')
                    AND DATE(date_planification) = ?
                """;

                PreparedStatement pst1 = conn.prepareStatement(queryPlanifiees);
                pst1.setInt(1, idConducteurConnecte);
                pst1.setString(2, aujourdhuiStr);
                ResultSet rs1 = pst1.executeQuery();
                if (rs1.next()) {
                    interventionsPlanifieesLabel.setText(String.valueOf(rs1.getInt("count")));
                }

                // 2. Collectes effectuées ce mois (interventions terminées)
                String queryCollectes = """
                    SELECT COUNT(*) as count 
                    FROM INTERVENTION 
                    WHERE id_conducteur = ? 
                    AND statut = 'terminee'
                    AND MONTH(date_realisation) = MONTH(CURRENT_DATE())
                    AND YEAR(date_realisation) = YEAR(CURRENT_DATE())
                """;

                PreparedStatement pst2 = conn.prepareStatement(queryCollectes);
                pst2.setInt(1, idConducteurConnecte);
                ResultSet rs2 = pst2.executeQuery();
                if (rs2.next()) {
                    collectesEffectueesLabel.setText(String.valueOf(rs2.getInt("count")));
                }

                // 3. Signalements à traiter
                String queryATraiter = """
                    SELECT COUNT(*) as count 
                    FROM INTERVENTION 
                    WHERE id_conducteur = ? 
                    AND statut IN ('planifiee', 'en_cours')
                """;

                PreparedStatement pst3 = conn.prepareStatement(queryATraiter);
                pst3.setInt(1, idConducteurConnecte);
                ResultSet rs3 = pst3.executeQuery();
                if (rs3.next()) {
                    signalementsAssignesLabel.setText(String.valueOf(rs3.getInt("count")));
                }

                conn.close();

            } else {
                calculateLocalStatistics();
            }

        } catch (Exception e) {
            e.printStackTrace();
            calculateLocalStatistics();
        }
    }

    private void calculateLocalStatistics() {
        LocalDate aujourdhui = LocalDate.now();

        // Interventions planifiées pour aujourd'hui
        int interventionsAujourdhui = (int) interventionsList.stream()
                .filter(i -> i.getDatePlanification() != null
                        && i.getDatePlanification().toLocalDate().isEqual(aujourdhui)
                        && (i.getStatut().equals("planifiee") || i.getStatut().equals("en_cours")))
                .count();

        // Collectes effectuées (statut 'terminee') ce mois
        int collectesCeMois = (int) interventionsList.stream()
                .filter(i -> i.getStatut().equals("terminee")
                        && i.getDateRealisation() != null
                        && i.getDateRealisation().getMonth() == aujourdhui.getMonth()
                        && i.getDateRealisation().getYear() == aujourdhui.getYear())
                .count();

        // Signalements à traiter (statut 'planifiee' ou 'en_cours')
        int signalementsATraiter = (int) interventionsList.stream()
                .filter(i -> i.getStatut().equals("planifiee") || i.getStatut().equals("en_cours"))
                .count();

        // Mettre à jour les labels
        interventionsPlanifieesLabel.setText(String.valueOf(interventionsAujourdhui));
        collectesEffectueesLabel.setText(String.valueOf(collectesCeMois));
        signalementsAssignesLabel.setText(String.valueOf(signalementsATraiter));
    }

    private void setupButtonActions() {
        dashboardBtn.setOnAction(event -> showDashboard());
        collecteBtn.setOnAction(event -> showCollecte());
        signalementsBtn.setOnAction(event -> showSignalements());
    }

    @FXML
    private void showDashboard() {
        // Recharger les données depuis la base
        loadDataFromDatabase();
        updateStatistics();
    }

    @FXML
    private void showCollecte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("historique-collecte.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) collecteBtn.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de collecte", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showSignalements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signalements-conducteur.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signalementsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page des signalements", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de déconnexion");
            confirmation.setHeaderText("Déconnexion");
            confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                        Parent root = loader.load();

                        Stage stage = (Stage) nomConducteurLabel.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Connexion - Gestion des Déchets");
                        stage.centerOnScreen();

                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible de se déconnecter", Alert.AlertType.ERROR);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la déconnexion", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void ouvrirPopupAjoutIntervention() {
        try {
            // Vérifier que l'ID du conducteur est valide
            if (idConducteurConnecte == 0) {
                showAlert("Erreur", "Conducteur non identifié", Alert.AlertType.ERROR);
                return;
            }

            // Charger le formulaire d'ajout d'intervention
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addIntervention.fxml"));
            Parent root = loader.load();

            addInterventionController controller = loader.getController();
            controller.setIdConducteurConnecte(this.idConducteurConnecte);
            controller.setMainController(this);

            Scene scene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.setTitle("Ajouter une Intervention");
            popupStage.initModality(Modality.APPLICATION_MODAL);

            if (nomConducteurLabel != null && nomConducteurLabel.getScene() != null) {
                Stage primaryStage = (Stage) nomConducteurLabel.getScene().getWindow();
                popupStage.initOwner(primaryStage);
            }

            popupStage.setScene(scene);
            controller.setPopupStage(popupStage);
            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Fichier addIntervention.fxml introuvable", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", Alert.AlertType.ERROR);
        }
    }

    public void ajouterNouvelleIntervention(Intervention intervention) {
        try {
            // Vérifier que l'ID du conducteur est valide
            if (idConducteurConnecte == 0) {
                showAlert("Erreur", "Conducteur non identifié", Alert.AlertType.ERROR);
                return;
            }

            // Vérifier que l'ID du signalement existe
            if (!checkSignalementExists(intervention.getIdSignalement())) {
                showAlert("Erreur", "Le signalement #" + intervention.getIdSignalement() + " n'existe pas", Alert.AlertType.ERROR);
                return;
            }

            // VÉRIFIER QUE LE SIGNALEMENT N'A PAS DÉJÀ UNE INTERVENTION
            if (checkSignalementHasIntervention(intervention.getIdSignalement())) {
                showAlert("Erreur", "Ce signalement a déjà une intervention associée", Alert.AlertType.ERROR);
                return;
            }

            // Insérer dans la base de données
            Connection conn = Database.connectDB();
            if (conn != null) {
                String query = """
                INSERT INTO INTERVENTION (date_planification, statut, notes, id_conducteur, id_signalement)
                VALUES (?, ?, ?, ?, ?)
            """;

                PreparedStatement pst = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setTimestamp(1, java.sql.Timestamp.valueOf(intervention.getDatePlanification()));
                pst.setString(2, intervention.getStatut());
                pst.setString(3, intervention.getNotes());
                pst.setInt(4, intervention.getIdConducteur());
                pst.setInt(5, intervention.getIdSignalement());

                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    // Récupérer l'ID généré
                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        intervention.setIdIntervention(newId);
                    }

                    // Ajouter à la liste locale
                    interventionsList.add(intervention);
                    interventionsTable.refresh();
                    updateStatistics();

                    showAlert("Succès", "Intervention ajoutée avec succès!", Alert.AlertType.INFORMATION);
                }

                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout dans la base de données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Ajouter cette méthode dans DashConducteurController
    private boolean checkSignalementHasIntervention(int idSignalement) {
        try {
            Connection conn = Database.connectDB();
            String query = "SELECT COUNT(*) FROM INTERVENTION WHERE id_signalement = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, idSignalement);
            ResultSet rs = pst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                conn.close();
                return true;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkSignalementExists(int idSignalement) {
        try {
            Connection conn = Database.connectDB();
            String query = "SELECT COUNT(*) FROM SIGNALEMENT WHERE id_signalement = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, idSignalement);
            ResultSet rs = pst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                conn.close();
                return true;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void modifierIntervention(Intervention intervention) {
        try {
            // Ouvrir la popup de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addIntervention.fxml"));
            Parent root = loader.load();

            addInterventionController controller = loader.getController();
            controller.setIdConducteurConnecte(this.idConducteurConnecte);
            controller.setMainController(this);
            controller.preRemplirFormulaire(intervention);

            Scene scene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.setTitle("Modifier Intervention #" + intervention.getIdIntervention());
            popupStage.initModality(Modality.APPLICATION_MODAL);

            if (nomConducteurLabel != null && nomConducteurLabel.getScene() != null) {
                Stage primaryStage = (Stage) nomConducteurLabel.getScene().getWindow();
                popupStage.initOwner(primaryStage);
            }

            popupStage.setScene(scene);
            controller.setPopupStage(popupStage);
            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    public void modifierInterventionExistante(Intervention interventionModifiee) {
        try {
            // Mettre à jour dans la base de données
            Connection conn = Database.connectDB();
            if (conn != null) {
                String query = """
                    UPDATE INTERVENTION 
                    SET date_planification = ?, statut = ?, notes = ?, 
                        date_realisation = ?, id_signalement = ?
                    WHERE id_intervention = ?
                """;

                PreparedStatement pst = conn.prepareStatement(query);
                pst.setTimestamp(1, java.sql.Timestamp.valueOf(interventionModifiee.getDatePlanification()));
                pst.setString(2, interventionModifiee.getStatut());
                pst.setString(3, interventionModifiee.getNotes());

                if (interventionModifiee.getDateRealisation() != null) {
                    pst.setTimestamp(4, java.sql.Timestamp.valueOf(interventionModifiee.getDateRealisation()));
                } else {
                    pst.setNull(4, java.sql.Types.TIMESTAMP);
                }

                pst.setInt(5, interventionModifiee.getIdSignalement());
                pst.setInt(6, interventionModifiee.getIdIntervention());

                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    // Mettre à jour dans la liste locale
                    for (int i = 0; i < interventionsList.size(); i++) {
                        if (interventionsList.get(i).getIdIntervention() == interventionModifiee.getIdIntervention()) {
                            interventionsList.set(i, interventionModifiee);
                            break;
                        }
                    }

                    interventionsTable.refresh();
                    updateStatistics();
                    showAlert("Succès", "Intervention modifiée avec succès!", Alert.AlertType.INFORMATION);
                }

                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification dans la base de données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void terminerIntervention(Intervention intervention) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Terminer l'intervention");
        confirmation.setContentText("Êtes-vous sûr de vouloir marquer cette intervention comme terminée?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Mettre à jour dans la base de données
                    Connection conn = Database.connectDB();
                    if (conn != null) {
                        String query = "UPDATE INTERVENTION SET statut = 'terminee', date_realisation = NOW() WHERE id_intervention = ?";
                        PreparedStatement pst = conn.prepareStatement(query);
                        pst.setInt(1, intervention.getIdIntervention());
                        pst.executeUpdate();
                        conn.close();
                    }

                    // Mettre à jour localement
                    intervention.setStatut("terminee");
                    intervention.setDateRealisation(LocalDateTime.now());
                    interventionsTable.refresh();
                    updateStatistics();
                    showAlert("Succès", "Intervention marquée comme terminée", Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la mise à jour dans la base de données", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void annulerIntervention(Intervention intervention) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Annuler l'intervention");
        confirmation.setContentText("Êtes-vous sûr de vouloir annuler cette intervention?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Mettre à jour dans la base de données
                    Connection conn = Database.connectDB();
                    if (conn != null) {
                        String query = "UPDATE INTERVENTION SET statut = 'annulee' WHERE id_intervention = ?";
                        PreparedStatement pst = conn.prepareStatement(query);
                        pst.setInt(1, intervention.getIdIntervention());
                        pst.executeUpdate();
                        conn.close();
                    }

                    // Mettre à jour localement
                    intervention.setStatut("annulee");
                    interventionsTable.refresh();
                    updateStatistics();
                    showAlert("Succès", "Intervention annulée", Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la mise à jour dans la base de données", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void supprimerIntervention(Intervention intervention) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'intervention");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'intervention #" + intervention.getIdIntervention() + " ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Vérifier s'il y a des collectes associées
                    Connection conn = Database.connectDB();
                    if (conn != null) {
                        // Vérifier les collectes associées
                        String checkQuery = "SELECT COUNT(*) FROM COLLECTE WHERE id_intervention = ?";
                        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                        checkStmt.setInt(1, intervention.getIdIntervention());
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next() && rs.getInt(1) > 0) {
                            showAlert("Impossible de supprimer",
                                    "Cette intervention a des collectes associées. Supprimez d'abord les collectes.",
                                    Alert.AlertType.ERROR);
                            return;
                        }

                        // Supprimer l'intervention
                        String deleteQuery = "DELETE FROM INTERVENTION WHERE id_intervention = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                        deleteStmt.setInt(1, intervention.getIdIntervention());

                        int rowsAffected = deleteStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            interventionsList.remove(intervention);
                            updateStatistics();
                            showAlert("Succès", "Intervention supprimée avec succès", Alert.AlertType.INFORMATION);
                        }

                        conn.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression dans la base de données", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setInterventionsList(ObservableList<Intervention> interventionsList) {
        this.interventionsList = interventionsList;
        interventionsTable.setItems(interventionsList);
        updateStatistics();
    }

    // Méthode pour rafraîchir les données
    public void refreshData() {
        loadDataFromDatabase();
        updateStatistics();
    }
}