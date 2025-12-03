package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class EffectuerCollecteController implements Initializable {

    @FXML private TextField idInterventionField;
    @FXML private TextField quantiteField;
    @FXML private DatePicker dateCollectePicker;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private Label messageLabel;
    @FXML private Button validerBtn;
    @FXML private Button annulerBtn;

    private HistoriqueCollectesController.CollecteAjouteeCallback callback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser le DatePicker avec la date d'aujourd'hui
        dateCollectePicker.setValue(LocalDate.now());

        // Remplir le ComboBox de statut
        statutComboBox.getItems().addAll("Planifiée", "En cours", "Terminée", "Annulée");
        statutComboBox.setValue("Terminée");

        // Validation numérique pour la quantité
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                quantiteField.setText(oldValue);
            }
        });
    }

    public void setOnCollecteAjoutee(HistoriqueCollectesController.CollecteAjouteeCallback callback) {
        this.callback = callback;
    }

    @FXML
    private void valider() {
        if (!validateForm()) {
            return;
        }

        try {
            // Récupérer les données
            int idIntervention = Integer.parseInt(idInterventionField.getText().trim());
            double quantite = Double.parseDouble(quantiteField.getText().trim());
            LocalDate dateCollecte = dateCollectePicker.getValue();
            String statut = statutComboBox.getValue();

            // Créer l'objet Collecte
            Collecte collecte = new Collecte();
            collecte.setIdIntervention(idIntervention);
            collecte.setQuantiteCollectee(quantite);
            collecte.setDateCollecte(dateCollecte.atStartOfDay());
            collecte.setStatut(statut);
            collecte.setIdAgentCollecteur(2); // ID de l'agent connecté

            // Ici, sauvegarder dans la base de données
            // collecteDAO.save(collecte);
            System.out.println("Nouvelle collecte ajoutée: " + collecte);

            // Afficher message de succès
            messageLabel.setText("✓ Collecte ajoutée avec succès !");
            messageLabel.setStyle("-fx-text-fill: #2ecc71;");

            // Désactiver le bouton valider
            validerBtn.setDisable(true);

            // Fermer la popup après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        // Appeler le callback pour rafraîchir le tableau principal
                        if (callback != null) {
                            callback.onCollecteAjoutee();
                        }

                        // Fermer la fenêtre
                        Stage stage = (Stage) validerBtn.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        stage.close();
    }

    private boolean validateForm() {
        // Vérifier ID intervention
        if (idInterventionField.getText().trim().isEmpty()) {
            showError("L'ID de l'intervention est requis");
            idInterventionField.requestFocus();
            return false;
        }

        try {
            int id = Integer.parseInt(idInterventionField.getText().trim());
            if (id <= 0) {
                showError("ID d'intervention invalide");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("ID d'intervention doit être un nombre");
            return false;
        }

        // Vérifier quantité
        if (quantiteField.getText().trim().isEmpty()) {
            showError("La quantité est requise");
            quantiteField.requestFocus();
            return false;
        }

        try {
            double quantite = Double.parseDouble(quantiteField.getText().trim());
            if (quantite <= 0) {
                showError("La quantité doit être supérieure à 0");
                return false;
            }
            if (quantite > 10000) {
                showError("La quantité ne peut pas dépasser 10000 kg");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Quantité invalide. Utilisez un nombre (ex: 150.5)");
            return false;
        }

        // Vérifier date
        if (dateCollectePicker.getValue() == null) {
            showError("La date de collecte est requise");
            dateCollectePicker.requestFocus();
            return false;
        }

        // Vérifier que la date n'est pas dans le futur
        if (dateCollectePicker.getValue().isAfter(LocalDate.now())) {
            showError("La date de collecte ne peut pas être dans le futur");
            return false;
        }

        // Vérifier statut
        if (statutComboBox.getValue() == null) {
            showError("Le statut est requis");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        messageLabel.setText("✗ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }
}