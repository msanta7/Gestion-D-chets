package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private int idConducteurConnecte = 1; // ID du conducteur connecté

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les labels
        nomConducteurLabel.setText("Mohamed Ali (Conducteur)");

        // Initialiser la table
        setupTableView();

        // Charger les données de test
        loadSampleData();

        // Mettre à jour les statistiques
        updateStatistics();

        // Configurer les actions des boutons
        setupButtonActions();
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

            {
                modifierBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                modifierBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    modifierIntervention(intervention);
                });

                terminerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 12px;");
                terminerBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    terminerIntervention(intervention);
                });

                annulerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                annulerBtn.setOnAction(event -> {
                    Intervention intervention = getTableView().getItems().get(getIndex());
                    annulerIntervention(intervention);
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
                        buttonsContainer.getChildren().add(modifierBtn);
                    } else {
                        buttonsContainer.getChildren().add(modifierBtn);
                    }

                    setGraphic(buttonsContainer);
                }
            }
        });
    }

    private void loadSampleData() {
        // Ajouter des données de test compatibles avec la table INTERVENTION
        Intervention i1 = new Intervention(1,
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
                "planifiee",
                "Collecte des ordures ménagères");
        i1.setIdIntervention(1);
        i1.setIdConducteur(this.idConducteurConnecte);
        i1.setAdresseSignalement("Rue Mohammed V, Casablanca");
        interventionsList.add(i1);

        Intervention i2 = new Intervention(2,
                LocalDateTime.now().withHour(14).withMinute(0),
                "terminee",
                "Collecte des déchets recyclables");
        i2.setIdIntervention(2);
        i2.setIdConducteur(this.idConducteurConnecte);
        i2.setDateRealisation(LocalDateTime.now());
        i2.setAdresseSignalement("Avenue Hassan II, Rabat");
        interventionsList.add(i2);

        Intervention i3 = new Intervention(3,
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(30),
                "planifiee",
                "Collecte d'objets encombrants");
        i3.setIdIntervention(3);
        i3.setIdConducteur(this.idConducteurConnecte);
        i3.setAdresseSignalement("Quartier Industriel, Tanger");
        interventionsList.add(i3);

        // Mettre à jour les statistiques
        updateStatistics();
    }

    private void updateStatistics() {
        LocalDate aujourdhui = LocalDate.now();

        // Interventions planifiées pour aujourd'hui
        int interventionsAujourdhui = (int) interventionsList.stream()
                .filter(i -> i.getDatePlanification() != null
                        && i.getDatePlanification().toLocalDate().isEqual(aujourdhui))
                .count();

        // Collectes effectuées (statut 'terminee') ce mois
        int collectesCeMois = (int) interventionsList.stream()
                .filter(i -> i.getStatut().equals("terminee")
                        && i.getDateRealisation() != null
                        && i.getDateRealisation().getMonth() == aujourdhui.getMonth())
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
        // Logique pour afficher le dashboard
        System.out.println("Dashboard clicked");
        // Vous pouvez mettre à jour le contenu principal ici si nécessaire
    }

    @FXML
    private void showCollecte() {
        try {
            // Charger le nouveau fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("historique-collecte.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) collecteBtn.getScene().getWindow();
            Scene scene = new Scene(root);

            // Changer la scène
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
            // Demander confirmation avant de déconnecter
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de déconnexion");
            confirmation.setHeaderText("Déconnexion");
            confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Charger la vue de connexion
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                        Parent root = loader.load();

                        // Récupérer la scène actuelle
                        Stage stage = (Stage) nomConducteurLabel.getScene().getWindow();

                        // Changer de scène
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
            System.out.println("Tentative d'ouverture du formulaire d'ajout d'intervention...");

            // Vérifier si le fichier FXML existe
            URL fxmlUrl = getClass().getResource("addIntervention.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Fichier addIntervention.fxml introuvable dans le classpath!");

                // Essayer un chemin absolu
                fxmlUrl = getClass().getResource("/com/example/gestiondechets/addIntervention.fxml");
                if (fxmlUrl == null) {
                    throw new IOException("Fichier addIntervention.fxml introuvable dans le classpath.");
                }
            }

            System.out.println("Fichier FXML trouvé: " + fxmlUrl);

            // Charger le fichier FXML de la popup
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("FXML chargé avec succès");

            // Récupérer le contrôleur de la popup
            addInterventionController controller = loader.getController();

            if (controller == null) {
                throw new IllegalStateException("Le contrôleur addInterventionController n'a pas été instancié.");
            }

            System.out.println("Contrôleur récupéré: " + controller.getClass().getName());

            // Passer les références au contrôleur
            controller.setIdConducteurConnecte(this.idConducteurConnecte);
            controller.setMainController(this);

            System.out.println("Paramètres passés au contrôleur: idConducteur=" + this.idConducteurConnecte);

            // Récupérer la fenêtre principale - version sans multiple assignations
            Stage primaryStage = null;
            try {
                if (nomConducteurLabel != null && nomConducteurLabel.getScene() != null) {
                    primaryStage = (Stage) nomConducteurLabel.getScene().getWindow();
                    System.out.println("Fenêtre principale récupérée: " + primaryStage);
                } else {
                    System.err.println("Warning: Impossible de récupérer la fenêtre principale");
                }
            } catch (Exception e) {
                System.err.println("Warning: Erreur lors de la récupération de la fenêtre principale: " + e.getMessage());
                // Ne pas réassigner primaryStage ici
            }

            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.setTitle("Ajouter une Intervention");
            popupStage.initModality(Modality.APPLICATION_MODAL);

            // Lier à la fenêtre principale si disponible
            if (primaryStage != null) {
                popupStage.initOwner(primaryStage);
            }

            popupStage.setScene(scene);

            // Passer la référence du stage au contrôleur
            controller.setPopupStage(popupStage);

            // Configuration de la fenêtre popup
            popupStage.setResizable(false);

            // Centrer la fenêtre popup - utiliser des variables finales locales
            final Stage finalPrimaryStage = primaryStage; // Créer une copie finale
            final Stage finalPopupStage = popupStage; // Créer une copie finale

            popupStage.setOnShown(event -> {
                if (finalPrimaryStage != null) {
                    // Centrer par rapport à la fenêtre principale
                    finalPopupStage.setX(finalPrimaryStage.getX() + (finalPrimaryStage.getWidth() - finalPopupStage.getWidth()) / 2);
                    finalPopupStage.setY(finalPrimaryStage.getY() + (finalPrimaryStage.getHeight() - finalPopupStage.getHeight()) / 2);
                } else {
                    // Centrer sur l'écran
                    finalPopupStage.centerOnScreen();
                }
            });

            // Afficher la popup et attendre sa fermeture
            System.out.println("Affichage de la popup d'ajout d'intervention...");
            popupStage.showAndWait();

            System.out.println("Popup d'ajout d'intervention fermée");

        } catch (IOException e) {
            // Erreur de fichier FXML
            e.printStackTrace();
            System.err.println("ERREUR CRITIQUE: Impossible de charger le fichier FXML!");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Fichier Manquant");
            alert.setHeaderText("Fichier addIntervention.fxml introuvable");
            alert.setContentText("Le fichier de formulaire est introuvable.\n\n" +
                    "Assurez-vous que le fichier 'addIntervention.fxml' existe dans:\n" +
                    "• src/main/resources/com/example/gestiondechets/\n" +
                    "• Ou dans le même package que DashConducteurController\n\n" +
                    "Erreur technique: " + e.getMessage());
            alert.getDialogPane().setPrefSize(500, 250);
            alert.showAndWait();

        } catch (IllegalStateException e) {
            // Erreur de contrôleur
            e.printStackTrace();
            System.err.println("ERREUR: Problème avec le contrôleur FXML!");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Contrôleur");
            alert.setHeaderText("Erreur lors du chargement du formulaire");
            alert.setContentText("Impossible de charger le contrôleur du formulaire.\n\n" +
                    "Vérifiez que:\n" +
                    "1. Le fichier addIntervention.fxml a un fx:controller valide\n" +
                    "2. La classe addInterventionController existe\n" +
                    "3. Toutes les méthodes nécessaires sont implémentées\n\n" +
                    "Erreur: " + e.getMessage());
            alert.showAndWait();

        } catch (Exception e) {
            // Erreur générale
            e.printStackTrace();
            System.err.println("ERREUR INATTENDUE: " + e.getClass().getName() + " - " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Inattendue");
            alert.setHeaderText("Une erreur inattendue s'est produite");
            alert.setContentText("Impossible d'ouvrir le formulaire d'ajout.\n\n" +
                    "Détails techniques: " + e.getClass().getSimpleName() + "\n" +
                    "Message: " + e.getMessage() + "\n\n" +
                    "Veuillez redémarrer l'application.");
            alert.showAndWait();
        }
    }


    public void ajouterNouvelleIntervention(Intervention intervention) {
        try {
            System.out.println("Tentative d'ajout d'une nouvelle intervention...");

            // Validation de base
            if (intervention == null) {
                throw new IllegalArgumentException("L'intervention ne peut pas être null");
            }

            System.out.println("Intervention reçue: ID Signalement=" + intervention.getIdSignalement() +
                    ", Date=" + intervention.getDatePlanification() +
                    ", Statut=" + intervention.getStatut());

            // S'assurer que l'ID du conducteur est défini
            if (intervention.getIdConducteur() == 0) {
                intervention.setIdConducteur(this.idConducteurConnecte);
                System.out.println("ID Conducteur défini: " + this.idConducteurConnecte);
            }

            // Générer un ID temporaire
            int nouvelId;
            if (interventionsList.isEmpty()) {
                nouvelId = 1000;
            } else {
                nouvelId = interventionsList.stream()
                        .mapToInt(Intervention::getIdIntervention)
                        .max()
                        .orElse(999) + 1;
            }

            intervention.setIdIntervention(nouvelId);
            System.out.println("ID attribué à l'intervention: " + nouvelId);

            // Ajouter à la liste
            interventionsList.add(intervention);
            System.out.println("Intervention ajoutée à la liste, taille actuelle: " + interventionsList.size());

            // Rafraîchir la table
            interventionsTable.refresh();
            System.out.println("TableView rafraîchie");

            // Mettre à jour les statistiques
            updateStatistics();
            System.out.println("Statistiques mises à jour");

            // Afficher confirmation
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText(null);
            successAlert.setContentText("✅ Intervention ajoutée avec succès!\n\n" +
                    "ID: " + intervention.getIdIntervention() + "\n" +
                    "Signalement: " + intervention.getIdSignalement() + "\n" +
                    "Date: " + intervention.getDatePlanificationFormatted() + "\n" +
                    "Statut: " + intervention.getStatutFormatted());
            successAlert.showAndWait();

            System.out.println("Nouvelle intervention ajoutée avec succès: " + intervention);

        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation: " + e.getMessage());

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur de Validation");
            errorAlert.setHeaderText("Données invalides");
            errorAlert.setContentText("Impossible d'ajouter l'intervention:\n\n" + e.getMessage());
            errorAlert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur inattendue lors de l'ajout: " + e.getMessage());

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Erreur lors de l'ajout");
            errorAlert.setContentText("Une erreur s'est produite lors de l'ajout de l'intervention:\n\n" + e.getMessage());
            errorAlert.showAndWait();
        }
    }


    // Méthode appelée par addInterventionController pour modifier une intervention existante
    public void modifierInterventionExistante(Intervention interventionModifiee) {
        try {
            // Trouver l'intervention à modifier dans la liste
            for (int i = 0; i < interventionsList.size(); i++) {
                if (interventionsList.get(i).getIdIntervention() == interventionModifiee.getIdIntervention()) {
                    // Mettre à jour l'intervention
                    interventionsList.set(i, interventionModifiee);

                    // Rafraîchir la table
                    interventionsTable.refresh();

                    // Mettre à jour les statistiques
                    updateStatistics();

                    showAlert("Succès", "Intervention modifiée avec succès!", Alert.AlertType.INFORMATION);
                    return;
                }
            }

            showAlert("Erreur", "Intervention non trouvée", Alert.AlertType.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification de l'intervention", Alert.AlertType.ERROR);
        }
    }

    private void modifierIntervention(Intervention intervention) {
        // Créer une variable finale pour le lambda
        final Intervention interventionToModify = intervention;

        try {
            // Ouvrir la popup de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addIntervention.fxml"));
            Parent root = loader.load();

            addInterventionController controller = loader.getController();

            // Passer l'ID du conducteur connecté
            controller.setIdConducteurConnecte(this.idConducteurConnecte);
            controller.setMainController(this);

            // Pré-remplir le formulaire avec l'intervention à modifier
            controller.preRemplirFormulaire(interventionToModify);

            Scene scene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.setTitle("Modifier Intervention #" + interventionToModify.getIdIntervention());
            popupStage.initModality(Modality.APPLICATION_MODAL);

            // Lier à la fenêtre principale si disponible
            if (nomConducteurLabel != null && nomConducteurLabel.getScene() != null) {
                Stage primaryStage = (Stage) nomConducteurLabel.getScene().getWindow();
                popupStage.initOwner(primaryStage);
            }

            popupStage.setScene(scene);
            popupStage.setResizable(false);

            controller.setPopupStage(popupStage);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    private void terminerIntervention(Intervention intervention) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Terminer l'intervention");
        confirmation.setContentText("Êtes-vous sûr de vouloir marquer cette intervention comme terminée?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                intervention.setStatut("terminee");
                intervention.setDateRealisation(LocalDateTime.now());
                interventionsTable.refresh();
                updateStatistics();
                showAlert("Succès", "Intervention marquée comme terminée", Alert.AlertType.INFORMATION);
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
                intervention.setStatut("annulee");
                interventionsTable.refresh();
                updateStatistics();
                showAlert("Succès", "Intervention annulée", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void supprimerIntervention(Intervention intervention) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'intervention");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'intervention #" + intervention.getIdIntervention() + " ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                interventionsList.remove(intervention);
                updateStatistics();
                showAlert("Succès", "Intervention supprimée avec succès", Alert.AlertType.INFORMATION);
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
}