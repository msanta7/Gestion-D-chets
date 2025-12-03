package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class TriageDechetsController {

    @FXML private Label nomAgentLabel;
    @FXML private ComboBox<String> typeDechetCombo;
    @FXML private TextField quantiteField;


    @FXML private TableView<TriageItem> triageTable;
    @FXML private TableColumn<TriageItem, String> colType;
    @FXML private TableColumn<TriageItem, Double> colQuantite;
    @FXML private TableColumn<TriageItem, String> colStatut;
    @FXML private TableColumn<TriageItem, Void> colActions;

    private ObservableList<TriageItem> triageList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurer le nom de l'agent
        nomAgentLabel.setText("Agent Tri");

        // Remplir les ComboBox
        ObservableList<String> typesDechets = FXCollections.observableArrayList(
                "Plastique",
                "Verre",
                "Métal",
                "Carton",
                "Organique",
                "Électronique",
                "Dangereux",
                "Textile",
                "Bois"
        );
        typeDechetCombo.setItems(typesDechets);


        // Configurer les colonnes du tableau
        colType.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        colQuantite.setCellValueFactory(cellData -> cellData.getValue().quantiteProperty().asObject());
        colStatut.setCellValueFactory(cellData -> cellData.getValue().statutProperty());

        // Personnaliser la colonne statut
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    setStyle(getStatutStyle(statut));
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }

            private String getStatutStyle(String statut) {
                switch(statut.toLowerCase()) {
                    case "à trier":
                        return "-fx-background-color: #f39c12; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    case "en triage":
                        return "-fx-background-color: #3498db; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    case "trié":
                        return "-fx-background-color: #2ecc71; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    case "stocké":
                        return "-fx-background-color: #9b59b6; -fx-background-radius: 10; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-weight: bold;";
                    default:
                        return "";
                }
            }
        });

        // Configurer la colonne actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnSupprimer = new Button("🗑️");
            private final Button btnModifier = new Button("✏️");

            {
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 8; -fx-border-radius: 5; -fx-background-radius: 5;");
                btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 8; -fx-border-radius: 5; -fx-background-radius: 5;");

                btnSupprimer.setOnAction(event -> {
                    TriageItem item = getTableView().getItems().get(getIndex());
                    supprimerItem(item);
                });

                btnModifier.setOnAction(event -> {
                    TriageItem item = getTableView().getItems().get(getIndex());
                    modifierItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox boutons = new javafx.scene.layout.HBox(5);
                    boutons.setAlignment(javafx.geometry.Pos.CENTER);
                    boutons.getChildren().addAll(btnSupprimer, btnModifier);
                    setGraphic(boutons);
                }
            }
        });

        // Charger des données de test
        chargerDonneesTest();
    }

    private void chargerDonneesTest() {
        triageList.add(new TriageItem("Plastique", 125.5, "à trier"));
        triageList.add(new TriageItem("Verre", 89.2, "en triage"));
        triageList.add(new TriageItem("Métal", 156.8, "trié"));

        triageTable.setItems(triageList);
    }

    @FXML
    private void ajouterTriage() {
        if (validerFormulaire()) {
            String type = typeDechetCombo.getValue();
            double quantite = Double.parseDouble(quantiteField.getText());

            triageList.add(new TriageItem(type, quantite,"à trier"));

            // Réinitialiser le formulaire
            typeDechetCombo.getSelectionModel().clearSelection();
            quantiteField.clear();

            showAlert("Succès", "Déchet ajouté à la liste de triage", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void terminerTriage() {
        if (triageList.isEmpty()) {
            showError("Erreur", "Aucun déchet à traiter");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Terminer le triage");
        alert.setHeaderText("Confirmer la fin du triage");
        alert.setContentText(triageList.size() + " déchets seront marqués comme 'triés'.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (TriageItem item : triageList) {
                    if ("à trier".equals(item.getStatut()) || "en triage".equals(item.getStatut())) {
                        item.setStatut("trié");
                    }
                }
                triageTable.refresh();
                showAlert("Succès", "Triage terminé avec succès", Alert.AlertType.INFORMATION);
            }
        });
    }


    @FXML
    private void annulerTriage(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Annuler le triage");
        alert.setHeaderText("Êtes-vous sûr de vouloir annuler ?");
        alert.setContentText("Tous les déchets en cours de triage seront perdus.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("dash-tri.fxml"));
                    Parent root = loader.load();

                    Scene currentScene = ((Node) event.getSource()).getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de retourner au tableau de bord");
                }
            }
        });
    }

    private void supprimerItem(TriageItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer l'élément");
        alert.setHeaderText("Supprimer " + item.getType() + " (" + item.getQuantite() + " kg)?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                triageList.remove(item);
            }
        });
    }

    private void modifierItem(TriageItem item) {
        // Ouvrir un dialogue de modification
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQuantite()));
        dialog.setTitle("Modifier la quantité");
        dialog.setHeaderText("Modifier la quantité pour " + item.getType());
        dialog.setContentText("Nouvelle quantité (kg):");

        dialog.showAndWait().ifPresent(nouvelleQuantite -> {
            try {
                double quantite = Double.parseDouble(nouvelleQuantite);
                if (quantite > 0) {
                    item.setQuantite(quantite);
                    triageTable.refresh();
                    showAlert("Succès", "Quantité modifiée avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showError("Erreur", "La quantité doit être positive");
                }
            } catch (NumberFormatException e) {
                showError("Erreur", "Veuillez saisir un nombre valide");
            }
        });
    }

    private boolean validerFormulaire() {
        if (typeDechetCombo.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner un type de déchet");
            typeDechetCombo.requestFocus();
            return false;
        }

        if (quantiteField.getText().isEmpty()) {
            showError("Erreur", "Veuillez saisir une quantité");
            quantiteField.requestFocus();
            return false;
        }

        try {
            double quantite = Double.parseDouble(quantiteField.getText());
            if (quantite <= 0) {
                showError("Erreur", "La quantité doit être positive");
                quantiteField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Erreur", "Veuillez saisir un nombre valide pour la quantité");
            quantiteField.requestFocus();
            return false;
        }

        return true;
    }

    // === MÉTHODES DE NAVIGATION ===
    @FXML
    private void showDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DashTri.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger le tableau de bord");
        }
    }

    @FXML
    private void showTriage(ActionEvent event) {
        // Déjà sur cette page
    }

    @FXML
    private void showRapports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("generer-rapport-tri.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page des rapports");
        }
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

                    Scene currentScene = ((Node) event.getSource()).getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur de déconnexion", "Impossible de charger la page de connexion");
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe modèle pour les items de triage
    public static class TriageItem {
        private final StringProperty type = new SimpleStringProperty();
        private final DoubleProperty quantite = new SimpleDoubleProperty();
        private final StringProperty statut = new SimpleStringProperty();

        public TriageItem(String type, double quantite,String statut) {
            setType(type);
            setQuantite(quantite);
            setStatut(statut);
        }

        // Getters et Setters
        public String getType() { return type.get(); }
        public void setType(String value) { type.set(value); }
        public StringProperty typeProperty() { return type; }

        public double getQuantite() { return quantite.get(); }
        public void setQuantite(double value) { quantite.set(value); }
        public DoubleProperty quantiteProperty() { return quantite; }


        public String getStatut() { return statut.get(); }
        public void setStatut(String value) { statut.set(value); }
        public StringProperty statutProperty() { return statut; }
    }
}