package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class addInterventionController {

    @FXML private TextField idSignalementField;
    @FXML private DatePicker datePlanificationPicker;
    @FXML private ComboBox<String> heurePlanificationCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea notesArea;
    @FXML private Label adresseLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label etatLabel;
    @FXML private Label messageLabel;
    @FXML private Button validerBtn;
    @FXML private Button annulerBtn;
    @FXML private Button rechercherSignalementBtn;

    private Stage popupStage;
    private DashConducteurController mainController;
    private int idConducteurConnecte;

    @FXML
    private void initialize() {
        // Initialiser les heures (8h-18h)
        for (int i = 8; i <= 18; i++) {
            heurePlanificationCombo.getItems().add(String.format("%02d:00", i));
        }
        heurePlanificationCombo.setValue("09:00");

        // Initialiser les statuts selon l'ENUM de la base
        statutCombo.getItems().addAll("planifiee", "en_cours", "terminee", "annulee");
        statutCombo.setValue("planifiee");

        // Définir la date d'aujourd'hui
        datePlanificationPicker.setValue(LocalDate.now());

        // Initialiser les labels d'information
        clearSignalementInfo();

        // Configurer les actions des boutons
        validerBtn.setOnAction(event -> handleValider());
        annulerBtn.setOnAction(event -> handleAnnuler());
        if (rechercherSignalementBtn != null) {
            rechercherSignalementBtn.setOnAction(event -> rechercherSignalement());
        }
    }

    @FXML
    private void rechercherSignalement() {
        String idText = idSignalementField.getText().trim();
        if (idText.isEmpty()) {
            showError("Veuillez entrer un ID de signalement");
            return;
        }

        try {
            int idSignalement = Integer.parseInt(idText);
            // Ici, normalement vous feriez une requête à la base de données
            // Pour l'exemple, on simule des données
            if (idSignalement >= 1 && idSignalement <= 10) {
                adresseLabel.setText("Adresse du signalement #" + idSignalement);
                descriptionLabel.setText("Description du signalement");
                etatLabel.setText("nouveau");
                messageLabel.setText("✓ Signalement trouvé");
                messageLabel.setStyle("-fx-text-fill: #2ecc71;");
            } else {
                showError("Signalement non trouvé");
                clearSignalementInfo();
            }
        } catch (NumberFormatException e) {
            showError("ID invalide. Doit être un nombre");
        }
    }

    @FXML
    private void handleValider() {
        if (!validateForm()) {
            return;
        }

        try {
            int idSignalement = Integer.parseInt(idSignalementField.getText().trim());
            LocalDate date = datePlanificationPicker.getValue();
            String heure = heurePlanificationCombo.getValue();
            String statut = statutCombo.getValue();
            String notes = notesArea.getText().trim();

            // Combiner date et heure
            LocalDateTime dateTime = LocalDateTime.of(date,
                    LocalTime.parse(heure, DateTimeFormatter.ofPattern("HH:mm")));

            // Créer l'intervention
            Intervention intervention = new Intervention(
                    idSignalement,
                    dateTime,
                    statut,
                    notes
            );

            // Ajouter les infos du signalement si disponibles
            intervention.setAdresseSignalement(adresseLabel.getText());
            intervention.setDescriptionSignalement(descriptionLabel.getText());
            intervention.setEtatSignalement(etatLabel.getText());
            intervention.setIdConducteur(this.idConducteurConnecte);

            // Passer au contrôleur principal
            if (mainController != null) {
                mainController.ajouterNouvelleIntervention(intervention);
            }

            // Message de succès
            messageLabel.setText("✓ Intervention créée avec succès!");
            messageLabel.setStyle("-fx-text-fill: #2ecc71;");

            // Fermer après délai
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        if (popupStage != null) {
                            popupStage.close();
                        }
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
    private void handleAnnuler() {
        if (popupStage != null) {
            popupStage.close();
        }
    }

    private boolean validateForm() {
        // Vérifier ID signalement
        if (idSignalementField.getText().trim().isEmpty()) {
            showError("L'ID du signalement est requis");
            return false;
        }

        // Vérifier date
        if (datePlanificationPicker.getValue() == null) {
            showError("La date de planification est requise");
            return false;
        }

        // Vérifier heure
        if (heurePlanificationCombo.getValue() == null) {
            showError("L'heure de planification est requise");
            return false;
        }

        // Vérifier que la date n'est pas dans le passé
        if (datePlanificationPicker.getValue().isBefore(LocalDate.now())) {
            showError("La date ne peut pas être dans le passé");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        messageLabel.setText("✗ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void clearSignalementInfo() {
        adresseLabel.setText("");
        descriptionLabel.setText("");
        etatLabel.setText("");
    }

    // Méthode pour pré-remplir le formulaire (pour la modification)
    public void preRemplirFormulaire(Intervention intervention) {
        if (intervention != null) {
            idSignalementField.setText(String.valueOf(intervention.getIdSignalement()));
            idSignalementField.setDisable(true); // Ne pas permettre de modifier l'ID

            if (intervention.getDatePlanification() != null) {
                datePlanificationPicker.setValue(intervention.getDatePlanification().toLocalDate());
                heurePlanificationCombo.setValue(
                        intervention.getDatePlanification().format(DateTimeFormatter.ofPattern("HH:mm"))
                );
            }

            statutCombo.setValue(intervention.getStatut());
            notesArea.setText(intervention.getNotes());

            // Afficher les infos du signalement
            adresseLabel.setText(intervention.getAdresseSignalement());
            descriptionLabel.setText(intervention.getDescriptionSignalement());
            etatLabel.setText(intervention.getEtatSignalement());

            // Changer le texte du bouton pour la modification
            validerBtn.setText("Modifier");
        }
    }

    // Setters pour les références
    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    public void setMainController(DashConducteurController controller) {
        this.mainController = controller;
    }

    public void setIdConducteurConnecte(int id) {
        this.idConducteurConnecte = id;
    }
}