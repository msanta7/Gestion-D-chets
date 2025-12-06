package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class EffectuerCollecteController implements Initializable {

    @FXML private TextField idInterventionField;
    @FXML private TextField quantiteField;
    @FXML private DatePicker dateCollectePicker;
    @FXML private Label messageLabel;

    private int agentCollecteurId = getExistingAgentId(); // Changez ceci

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateCollectePicker.setValue(LocalDate.now());
        setupValidationListeners();
    }

    private int getExistingAgentId() {
        // Chercher un utilisateur avec le rôle 'agent_tri'
        try (Connection conn = Database.connectDB()) {
            if (conn != null) {
                String query = "SELECT id_utilisateur FROM UTILISATEUR WHERE role = 'agent_tri' LIMIT 1";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    if (rs.next()) {
                        return rs.getInt("id_utilisateur");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Par défaut, retourner 1 (premier utilisateur)
        return 1;
    }

    // Le reste du code reste identique...
    private void setupValidationListeners() {
        idInterventionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                idInterventionField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d\\.]", ""));
            }
        });
    }

    @FXML
    private void valider() {
        if (!validateFields()) {
            return;
        }

        try {
            int idIntervention = Integer.parseInt(idInterventionField.getText().trim());
            double quantite = Double.parseDouble(quantiteField.getText().trim());
            LocalDate date = dateCollectePicker.getValue();
            LocalTime time = LocalTime.now();
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // Vérifier si l'intervention existe
            if (!interventionExists(idIntervention)) {
                messageLabel.setText("Erreur: L'intervention #" + idIntervention + " n'existe pas.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            // Vérifier si l'agent existe
            if (!agentExists(agentCollecteurId)) {
                messageLabel.setText("Erreur: L'agent collecteur #" + agentCollecteurId + " n'existe pas.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            boolean success = insertCollecte(idIntervention, quantite, dateTime, agentCollecteurId);

            if (success) {
                messageLabel.setText("Collecte ajoutée avec succès !");
                messageLabel.setStyle("-fx-text-fill: #27ae60;");

                resetForm();

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> {
                                    ((Stage) messageLabel.getScene().getWindow()).close();
                                });
                            }
                        },
                        1500
                );
            } else {
                messageLabel.setText("Erreur lors de l'ajout de la collecte.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("Erreur de format des nombres.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private boolean agentExists(int agentId) {
        try (Connection conn = Database.connectDB()) {
            if (conn != null) {
                String query = "SELECT COUNT(*) FROM UTILISATEUR WHERE id_utilisateur = ? AND role = 'agent_tri'";
                try (PreparedStatement pst = conn.prepareStatement(query)) {
                    pst.setInt(1, agentId);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1) > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean interventionExists(int idIntervention) {
        try (Connection conn = Database.connectDB()) {
            if (conn != null) {
                String query = "SELECT COUNT(*) FROM INTERVENTION WHERE id_intervention = ?";
                try (PreparedStatement pst = conn.prepareStatement(query)) {
                    pst.setInt(1, idIntervention);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1) > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean insertCollecte(int idIntervention, double quantite, LocalDateTime dateTime, int agentCollecteurId) {
        try (Connection conn = Database.connectDB()) {
            if (conn != null) {
                String insertQuery = """
                    INSERT INTO COLLECTE 
                    (id_intervention, quantite_collectee, date_collecte, id_agent_collecteur) 
                    VALUES (?, ?, ?, ?)
                """;

                try (PreparedStatement pst = conn.prepareStatement(insertQuery)) {
                    pst.setInt(1, idIntervention);
                    pst.setDouble(2, quantite);
                    pst.setTimestamp(3, Timestamp.valueOf(dateTime));
                    pst.setInt(4, agentCollecteurId);

                    int rowsAffected = pst.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Erreur SQL: " + e.getMessage() +
                    "\nAssurez-vous que l'agent #" + agentCollecteurId + " existe dans la table UTILISATEUR.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
        return false;
    }

    private boolean validateFields() {
        if (idInterventionField.getText().trim().isEmpty()) {
            messageLabel.setText("Veuillez entrer un ID d'intervention.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }

        if (quantiteField.getText().trim().isEmpty()) {
            messageLabel.setText("Veuillez entrer une quantité.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }

        try {
            double quantite = Double.parseDouble(quantiteField.getText().trim());
            if (quantite <= 0) {
                messageLabel.setText("La quantité doit être supérieure à 0.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                return false;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Quantité invalide.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }

        if (dateCollectePicker.getValue() == null) {
            messageLabel.setText("Veuillez sélectionner une date.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }

        return true;
    }

    private void resetForm() {
        idInterventionField.clear();
        quantiteField.clear();
        dateCollectePicker.setValue(LocalDate.now());
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void setAgentCollecteurId(int agentId) {
        this.agentCollecteurId = agentId;
    }
}