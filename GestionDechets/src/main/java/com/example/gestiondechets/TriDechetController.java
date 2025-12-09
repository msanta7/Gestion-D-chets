package com.example.gestiondechets;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.beans.property.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TriDechetController {

    @FXML
    private ChoiceBox<String> cbTypeDechet;

    @FXML
    private ChoiceBox<String> cbCategorie;

    @FXML
    private TextField txtQuantite;

    @FXML
    private Label lblRestant;

    // Statistics labels - make sure these match your FXML fx:id
    @FXML
    private Label statPlastiqueValue;

    @FXML
    private Label statVerreValue;

    @FXML
    private Label statMetalValue;

    @FXML
    private Label statPapierValue;

    @FXML
    private Label statOrganiqueValue;

    private CollecteFX collecte;
    private double quantiteRestante;
    private double quantiteCollecteTotale;
    private Connection conn;

    // Map to store waste type statistics
    private Map<String, Double> wasteStats = new HashMap<>();

    // Map to link waste types to their labels
    private Map<String, Label> wasteLabels = new HashMap<>();

    @FXML
    public void initialize() {
        conn = Database.connectDB();

        // Initialize ChoiceBoxes
        cbTypeDechet.getItems().addAll(
                "Plastique", "Verre", "Métal", "Papier", "Organique"
        );

        cbCategorie.getItems().addAll(
                "menager", "industriel", "medical", "agricole"
        );

        // Initialize waste stats map
        wasteStats.put("Plastique", 0.0);
        wasteStats.put("Verre", 0.0);
        wasteStats.put("Métal", 0.0);
        wasteStats.put("Papier", 0.0);
        wasteStats.put("Organique", 0.0);

        // Add listener to quantity field for numeric validation
        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantite.setText(oldValue);
            }
        });

        // Initialize waste labels map after UI components are loaded
        initializeWasteLabels();
    }

    private void initializeWasteLabels() {
        // This will be called after FXML injection
        wasteLabels.put("Plastique", statPlastiqueValue);
        wasteLabels.put("Verre", statVerreValue);
        wasteLabels.put("Métal", statMetalValue);
        wasteLabels.put("Papier", statPapierValue);
        wasteLabels.put("Organique", statOrganiqueValue);
    }

    public void setCollecte(CollecteFX collecte) {
        this.collecte = collecte;
        if (collecte != null) {
            this.quantiteCollecteTotale = collecte.getQuantite();

            // Load data for this specific collecte
            chargerQuantiteRestante();
            chargerStatistiquesDechets();

            // Auto-fill quantity field with remaining quantity
            txtQuantite.setText(String.format("%.2f", quantiteRestante));
        }
    }

    private void chargerQuantiteRestante() {
        if (collecte == null) return;

        String sql = """
            SELECT IFNULL(SUM(quantite), 0) as total_quantite
            FROM DECHET 
            WHERE id_collecte = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, collecte.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double quantiteUtilisee = rs.getDouble("total_quantite");
                quantiteRestante = quantiteCollecteTotale - quantiteUtilisee;
                lblRestant.setText(String.format("Quantité restante : %.2f kg", quantiteRestante));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la quantité restante: " + e.getMessage());
        }
    }

    private void chargerStatistiquesDechets() {
        if (collecte == null) return;

        // Reset statistics
        for (String key : wasteStats.keySet()) {
            wasteStats.put(key, 0.0);
        }

        String sql = """
            SELECT type_dechet, SUM(quantite) as total_quantite
            FROM DECHET 
            WHERE id_collecte = ?
            GROUP BY type_dechet
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, collecte.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type_dechet");
                Double quantite = rs.getDouble("total_quantite");

                // Update the waste stats map
                wasteStats.put(type, quantite);
            }

            // Update the UI labels
            updateStatistiqueLabels();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void updateStatistiqueLabels() {
        // Update each waste type label
        for (Map.Entry<String, Double> entry : wasteStats.entrySet()) {
            String type = entry.getKey();
            Double quantite = entry.getValue();

            // Get the corresponding label
            Label label = wasteLabels.get(type);
            if (label != null) {
                label.setText(String.format("%.2f kg", quantite));
            }
        }
    }
    @FXML
    private void ajouterDechet() {
        // Validate inputs
        if (cbTypeDechet.getValue() == null || cbTypeDechet.getValue().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner un type de déchet", Alert.AlertType.ERROR);
            return;
        }

        if (cbCategorie.getValue() == null || cbCategorie.getValue().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner une catégorie", Alert.AlertType.ERROR);
            return;
        }

        double quantiteAjouter;
        try {
            quantiteAjouter = Double.parseDouble(txtQuantite.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer une quantité valide", Alert.AlertType.ERROR);
            return;
        }

        if (quantiteAjouter <= 0) {
            showAlert("Erreur", "La quantité doit être supérieure à 0", Alert.AlertType.ERROR);
            return;
        }

        if (quantiteAjouter > quantiteRestante) {
            showAlert("Erreur",
                    String.format("Quantité trop élevée. Maximum disponible: %.2f kg", quantiteRestante),
                    Alert.AlertType.ERROR);
            return;
        }

        // Determine toxicity based on category
        String categorie = cbCategorie.getValue();
        String toxicite;

        switch (categorie) {
            case "menager":
                toxicite = "non";
                break;
            case "agricole":
                toxicite = "faible";
                break;
            case "industriel":
                toxicite = "moyenne";
                break;
            case "medical":
                toxicite = "elevee";
                break;
            default:
                toxicite = "non";
                break;
        }

        // Insert the waste into the database with toxicity
        String sql = """
        INSERT INTO DECHET 
        (type_dechet, categorie, quantite, id_collecte, statut_tri, toxicite, date_tri, id_agent_tri)
        VALUES (?, ?, ?, ?, 'en_cours', ?, NOW(), ?)
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cbTypeDechet.getValue());
            ps.setString(2, categorie);
            ps.setDouble(3, quantiteAjouter);
            ps.setInt(4, collecte.getId());
            ps.setString(5, toxicite);
            ps.setInt(6, Database.getActiveUser().getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Update local statistics
                String type = cbTypeDechet.getValue();
                double nouvelleQuantite = wasteStats.get(type) + quantiteAjouter;
                wasteStats.put(type, nouvelleQuantite);

                // Refresh data from database
                chargerQuantiteRestante();
                updateStatistiqueLabels();

                // Auto-fill quantity field with new remaining quantity
                txtQuantite.setText(String.format("%.2f", quantiteRestante));

                // Clear selection
                cbTypeDechet.setValue(null);
                cbCategorie.setValue(null);

                showAlert("Succès",
                        String.format("Déchet ajouté avec succès\nType: %s\nCatégorie: %s\nToxicité: %s",
                                type, categorie, toxicite),
                        Alert.AlertType.INFORMATION);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ajouter le déchet: " + e.getMessage());
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) txtQuantite.getScene().getWindow();
        stage.close();
    }

    // Navigation methods (if needed in popup)
    private void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String titre, String message) {
        showAlert(titre, message, Alert.AlertType.ERROR);
    }

    // Classe modèle pour les items de triage
    public static class TriageItem {
        private final StringProperty type = new SimpleStringProperty();
        private final DoubleProperty quantite = new SimpleDoubleProperty();
        private final StringProperty statut = new SimpleStringProperty();

        public TriageItem(String type, double quantite, String statut) {
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