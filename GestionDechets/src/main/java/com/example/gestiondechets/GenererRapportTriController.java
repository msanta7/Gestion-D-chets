package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;

public class GenererRapportTriController {

    @FXML private Label nomAgentLabel;
    @FXML private TextField titreRapportField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> typeDechetCombo;
    @FXML private ComboBox<String> statutTriageCombo;
    @FXML private CheckBox inclureDetailsCheck;
    @FXML private CheckBox exporterExcelCheck;

    @FXML
    public void initialize() {
        // Configurer le nom de l'agent
        nomAgentLabel.setText("Agent Tri");

        // Définir les dates par défaut (mois en cours)
        dateDebutPicker.setValue(LocalDate.now().withDayOfMonth(1));
        dateFinPicker.setValue(LocalDate.now());

        // Remplir les ComboBox
        ObservableList<String> typesDechets = FXCollections.observableArrayList(
                "Tous les types",
                "Plastique",
                "Verre",
                "Métal",
                "Carton",
                "Organique",
                "Électronique",
                "Dangereux"
        );
        typeDechetCombo.setItems(typesDechets);
        typeDechetCombo.getSelectionModel().selectFirst();

        ObservableList<String> statuts = FXCollections.observableArrayList(
                "Tous les statuts",
                "En attente",
                "En cours",
                "Terminé",
                "Stocké"
        );
        statutTriageCombo.setItems(statuts);
        statutTriageCombo.getSelectionModel().selectFirst();

    }

    @FXML
    private void genererRapport() {
        if (validerFormulaire()) {
            String titre = titreRapportField.getText();
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();
            String typeDechet = typeDechetCombo.getValue();
            String statut = statutTriageCombo.getValue();

            // Simulation de génération de rapport
            StringBuilder rapport = new StringBuilder();
            rapport.append("=== RAPPORT DE TRI ===\n");
            rapport.append("Titre: ").append(titre).append("\n");
            rapport.append("Période: ").append(debut).append(" à ").append(fin).append("\n");
            rapport.append("Type de déchet: ").append(typeDechet).append("\n");
            rapport.append("Statut: ").append(statut).append("\n");
            rapport.append("\n--- OPTIONS ---\n");

            rapport.append("Excel: ").append(exporterExcelCheck.isSelected() ? "OUI" : "NON").append("\n");
            rapport.append("\n--- STATISTIQUES SIMULÉES ---\n");
            rapport.append("Total déchets triés: 1,245.8 kg\n");
            rapport.append("Plastique: 456.3 kg (36.6%)\n");
            rapport.append("Verre: 289.7 kg (23.2%)\n");
            rapport.append("Métal: 321.5 kg (25.8%)\n");
            rapport.append("Autres: 178.3 kg (14.4%)\n");

            // Afficher le rapport généré
            TextArea textArea = new TextArea(rapport.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(500, 400);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setPrefSize(520, 420);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rapport Généré");
            alert.setHeaderText("Rapport généré avec succès !");
            alert.getDialogPane().setContent(scrollPane);

            // Ajouter des boutons d'exportation
            ButtonType exporterPDFBtn = new ButtonType("Exporter PDF", ButtonBar.ButtonData.YES);
            ButtonType exporterExcelBtn = new ButtonType("Exporter Excel", ButtonBar.ButtonData.YES);
            ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

            alert.getButtonTypes().setAll(exporterPDFBtn, exporterExcelBtn, okBtn);

            alert.showAndWait().ifPresent(response -> {
                if (response == exporterPDFBtn) {
                    showAlert("Export PDF", "Rapport exporté en PDF avec succès", Alert.AlertType.INFORMATION);
                } else if (response == exporterExcelBtn) {
                    showAlert("Export Excel", "Rapport exporté en Excel avec succès", Alert.AlertType.INFORMATION);
                }
            });
        }
    }

    @FXML
    private void previsualiserRapport() {
        if (validerFormulaire()) {
            showAlert("Prévisualisation",
                    "Prévisualisation du rapport '" + titreRapportField.getText() + "'\n" +
                            "Période: " + dateDebutPicker.getValue() + " à " + dateFinPicker.getValue() + "\n" +
                            "Filtres appliqués: " + typeDechetCombo.getValue() + ", " + statutTriageCombo.getValue(),
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void annuler() {
        // Retour au tableau de bord
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dash-tri.fxml"));
            Parent root = loader.load();

            // Récupérer la scène depuis n'importe quel composant du contrôleur
            // Par exemple, utiliser le titreRapportField qui existe toujours
            Scene currentScene = titreRapportField.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner au tableau de bord");
        }
    }



    private boolean validerFormulaire() {
        if (titreRapportField.getText().isEmpty()) {
            showError("Erreur", "Veuillez saisir un titre pour le rapport");
            titreRapportField.requestFocus();
            return false;
        }

        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une période");
            return false;
        }

        if (dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            showError("Erreur", "La date de début doit être avant la date de fin");
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("triage-dechets.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page de triage");
        }
    }

    @FXML
    private void showRapports(ActionEvent event) {
        // Déjà sur cette page
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
}