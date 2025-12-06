package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class addInterventionController implements Initializable {

    @FXML private TextField idSignalementField;
    @FXML private DatePicker datePlanificationPicker;
    @FXML private ComboBox<String> heurePlanificationCombo;
    @FXML private ComboBox<String> statutCombo; // CHANGÉ: statutComboBox -> statutCombo
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;
    @FXML private Label adresseLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label etatLabel;
    @FXML private Button validerBtn;
    @FXML private Button annulerBtn;
    @FXML private Button rechercherSignalementBtn;

    private Stage popupStage;
    private int idConducteurConnecte;
    private DashConducteurController mainController;
    private Intervention interventionAModifier;
    private boolean enModeModification = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les ComboBox
        if (statutCombo != null) {
            statutCombo.getItems().addAll("planifiee", "en_cours", "terminee", "annulee");
            statutCombo.setValue("planifiee");
        }

        if (heurePlanificationCombo != null) {
            // Ajouter les heures de 8h à 18h
            for (int i = 8; i <= 18; i++) {
                String heure = String.format("%02d:00", i);
                heurePlanificationCombo.getItems().add(heure);
            }
            heurePlanificationCombo.setValue("08:00");
        }

        // Initialiser les labels
        adresseLabel.setText("Non sélectionné");
        descriptionLabel.setText("Non sélectionné");
        etatLabel.setText("Non sélectionné");

        // Configurer la validation
        setupValidation();

        // Configurer les boutons
        if (annulerBtn != null) {
            annulerBtn.setOnAction(e -> handleAnnuler());
        }

        if (rechercherSignalementBtn != null) {
            rechercherSignalementBtn.setOnAction(e -> rechercherSignalement());
        }
    }

    private void setupValidation() {
        if (validerBtn != null) {
            validerBtn.setDisable(true);
        }

        if (idSignalementField != null) {
            idSignalementField.textProperty().addListener((obs, oldVal, newVal) -> {
                validateForm();
            });
        }

        if (datePlanificationPicker != null) {
            datePlanificationPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateForm();
            });
        }

        if (heurePlanificationCombo != null) {
            heurePlanificationCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateForm();
            });
        }
    }

    private void validateForm() {
        boolean isValid = true;

        // Vérifier ID signalement
        if (idSignalementField != null) {
            isValid = !idSignalementField.getText().trim().isEmpty();
            if (isValid) {
                try {
                    Integer.parseInt(idSignalementField.getText().trim());
                } catch (NumberFormatException e) {
                    isValid = false;
                }
            }
        }

        // Vérifier date
        if (datePlanificationPicker != null) {
            isValid = isValid && datePlanificationPicker.getValue() != null;
        }

        // Vérifier heure
        if (heurePlanificationCombo != null) {
            isValid = isValid && heurePlanificationCombo.getValue() != null;
        }

        if (validerBtn != null) {
            validerBtn.setDisable(!isValid);
        }
    }

    @FXML
    private void rechercherSignalement() {
        try {
            String idText = idSignalementField.getText().trim();
            if (idText.isEmpty()) {
                showError("Veuillez entrer un ID de signalement");
                return;
            }

            int idSignalement = Integer.parseInt(idText);

            // Rechercher le signalement dans la base de données
            java.sql.Connection conn = Database.connectDB();
            if (conn != null) {
                String query = "SELECT adresse, description, etat FROM SIGNALEMENT WHERE id_signalement = ?";
                java.sql.PreparedStatement pst = conn.prepareStatement(query);
                pst.setInt(1, idSignalement);
                java.sql.ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    // Afficher les informations
                    adresseLabel.setText(rs.getString("adresse"));
                    descriptionLabel.setText(rs.getString("description") != null ? rs.getString("description") : "Aucune");
                    etatLabel.setText(translateEtat(rs.getString("etat")));

                    // Vérifier si une intervention existe déjà pour ce signalement
                    if (!enModeModification && checkSignalementHasIntervention(idSignalement)) {
                        showError("ATTENTION: Ce signalement a déjà une intervention associée");
                    } else {
                        showSuccess("Signalement trouvé");
                    }
                } else {
                    showError("Signalement non trouvé");
                    adresseLabel.setText("Non trouvé");
                    descriptionLabel.setText("Non trouvé");
                    etatLabel.setText("Non trouvé");
                }

                conn.close();
            }
        } catch (NumberFormatException e) {
            showError("ID invalide. Veuillez entrer un nombre.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private String translateEtat(String etat) {
        if (etat == null) return "Inconnu";
        switch(etat.toLowerCase()) {
            case "nouveau": return "Nouveau";
            case "en_cours": return "En Cours";
            case "termine": return "Terminé";
            case "annule": return "Annulé";
            default: return etat;
        }
    }

    private boolean checkSignalementHasIntervention(int idSignalement) {
        try {
            java.sql.Connection conn = Database.connectDB();
            String query = "SELECT COUNT(*) FROM INTERVENTION WHERE id_signalement = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, idSignalement);
            java.sql.ResultSet rs = pst.executeQuery();

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

    public void setPopupStage(Stage stage) {
        this.popupStage = stage;
    }

    public void setIdConducteurConnecte(int id) {
        this.idConducteurConnecte = id;
    }

    public void setMainController(DashConducteurController controller) {
        this.mainController = controller;
    }

    public void preRemplirFormulaire(Intervention intervention) {
        this.interventionAModifier = intervention;
        this.enModeModification = true;

        // Pré-remplir les champs
        if (idSignalementField != null) {
            idSignalementField.setText(String.valueOf(intervention.getIdSignalement()));
            idSignalementField.setDisable(true);
            // Charger automatiquement les informations du signalement
            rechercherSignalement();
        }

        if (datePlanificationPicker != null && intervention.getDatePlanification() != null) {
            datePlanificationPicker.setValue(intervention.getDatePlanification().toLocalDate());

            // Extraire l'heure
            String heure = String.format("%02d:00", intervention.getDatePlanification().getHour());
            if (heurePlanificationCombo != null) {
                heurePlanificationCombo.setValue(heure);
            }
        }

        if (statutCombo != null && intervention.getStatut() != null) {
            statutCombo.setValue(intervention.getStatut());
        }

        if (notesArea != null && intervention.getNotes() != null) {
            notesArea.setText(intervention.getNotes());
        }

        // Désactiver le bouton recherche en mode modification
        if (rechercherSignalementBtn != null) {
            rechercherSignalementBtn.setDisable(true);
        }
    }

    @FXML
    private void handleValider() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Récupérer les données
            int idSignalement = Integer.parseInt(idSignalementField.getText().trim());

            // Combiner date et heure
            LocalDate date = datePlanificationPicker.getValue();
            String[] heureParts = heurePlanificationCombo.getValue().split(":");
            LocalTime time = LocalTime.of(Integer.parseInt(heureParts[0]), Integer.parseInt(heureParts[1]));
            LocalDateTime datePlanification = LocalDateTime.of(date, time);

            String statut = statutCombo != null ? statutCombo.getValue() : "planifiee";
            String notes = notesArea != null ? notesArea.getText().trim() : "";

            if (enModeModification && interventionAModifier != null) {
                // MODE MODIFICATION
                interventionAModifier.setDatePlanification(datePlanification);
                interventionAModifier.setStatut(statut);
                interventionAModifier.setNotes(notes);

                mainController.modifierInterventionExistante(interventionAModifier);
                popupStage.close();

            } else {
                // MODE AJOUT - vérifier qu'il n'y a pas déjà une intervention
                if (checkSignalementHasIntervention(idSignalement)) {
                    showError("Ce signalement a déjà une intervention associée");
                    return;
                }

                // Créer nouvelle intervention
                Intervention nouvelleIntervention = new Intervention();
                nouvelleIntervention.setIdSignalement(idSignalement);
                nouvelleIntervention.setDatePlanification(datePlanification);
                nouvelleIntervention.setStatut(statut);
                nouvelleIntervention.setNotes(notes);
                nouvelleIntervention.setIdConducteur(idConducteurConnecte);

                mainController.ajouterNouvelleIntervention(nouvelleIntervention);

                // Fermer la popup après un court délai
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> {
                            popupStage.close();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        if (popupStage != null) {
            popupStage.close();
        }
    }

    private boolean validateInputs() {
        // Vérifier ID signalement
        if (idSignalementField == null || idSignalementField.getText().trim().isEmpty()) {
            showError("L'ID du signalement est requis");
            return false;
        }

        try {
            int idSignalement = Integer.parseInt(idSignalementField.getText().trim());
            if (idSignalement <= 0) {
                showError("ID signalement invalide");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("ID signalement doit être un nombre");
            return false;
        }

        // Vérifier date
        if (datePlanificationPicker == null || datePlanificationPicker.getValue() == null) {
            showError("La date de planification est requise");
            return false;
        }

        // Vérifier heure
        if (heurePlanificationCombo == null || heurePlanificationCombo.getValue() == null) {
            showError("L'heure de planification est requise");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText("✗ " + message);
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText("✓ " + message);
            messageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        }
    }
}