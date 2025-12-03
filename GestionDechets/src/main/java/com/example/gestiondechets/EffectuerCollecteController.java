package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EffectuerCollecteController implements Initializable {

    @FXML private Label nomAgentCollecteurLabel;


    @FXML private TextField idInterventionField;
    @FXML private TextField quantiteCollecteeField;
    @FXML private Spinner<Double> quantiteSpinner;
    @FXML private DatePicker dateCollectePicker;

    @FXML private Label adresseInterventionLabel;
    @FXML private Label dateInterventionLabel;
    @FXML private Label statutInterventionLabel;
    @FXML private Label messageLabel;

    @FXML private Button rechercherInterventionBtn;
    @FXML private Button validerCollecteBtn;
    @FXML private Button annulerCollecteBtn;

    @FXML private Button historiqueBtn;



    private int idAgentCollecteurConnecte = 2; // ID de l'agent collecteur connecté

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les labels
        nomAgentCollecteurLabel.setText("Agent Collecteur #" + idAgentCollecteurConnecte);

        // Initialiser le spinner pour la quantité
        SpinnerValueFactory<Double> valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10000.0, 0.0, 0.5);
        quantiteSpinner.setValueFactory(valueFactory);
        quantiteSpinner.setEditable(true);

        // Définir la date d'aujourd'hui
        dateCollectePicker.setValue(LocalDate.now());


        // Configurer les actions des boutons
        setupButtonActions();

        // Nettoyer les labels d'information
        clearInterventionInfo();
    }

    private void setupButtonActions() {
        rechercherInterventionBtn.setOnAction(event -> rechercherIntervention());
        validerCollecteBtn.setOnAction(event -> validerCollecte());
        annulerCollecteBtn.setOnAction(event -> annulerCollecte());

        // Validation numérique pour la quantité
        quantiteCollecteeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                quantiteCollecteeField.setText(oldValue);
            }
        });
    }

    @FXML
    private void rechercherIntervention() {
        String idText = idInterventionField.getText().trim();
        if (idText.isEmpty()) {
            showError("Veuillez entrer un ID d'intervention");
            return;
        }

        try {
            int idIntervention = Integer.parseInt(idText);

            // Ici, vous devriez faire une requête à la base de données
            // Pour l'exemple, on simule des données
            if (idIntervention >= 1 && idIntervention <= 10) {
                adresseInterventionLabel.setText("Adresse de l'intervention #" + idIntervention);
                dateInterventionLabel.setText(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                statutInterventionLabel.setText("planifiée");
                messageLabel.setText("✓ Intervention trouvée");
                messageLabel.setStyle("-fx-text-fill: #2ecc71;");
            } else {
                showError("Intervention non trouvée");
                clearInterventionInfo();
            }
        } catch (NumberFormatException e) {
            showError("ID invalide. Doit être un nombre");
        }
    }

    @FXML
    private void validerCollecte() {
        if (!validateForm()) {
            return;
        }

        try {
            // Récupérer les données
            int idIntervention = Integer.parseInt(idInterventionField.getText().trim());
            double quantite;

            // Utiliser le spinner ou le champ texte
            if (!quantiteCollecteeField.getText().trim().isEmpty()) {
                quantite = Double.parseDouble(quantiteCollecteeField.getText().trim());
            } else {
                quantite = quantiteSpinner.getValue();
            }

            LocalDate dateCollecte = dateCollectePicker.getValue();

            // Créer l'objet Collecte
            Collecte collecte = new Collecte();
            collecte.setIdIntervention(idIntervention);
            collecte.setQuantiteCollectee(quantite);
            collecte.setDateCollecte(dateCollecte.atStartOfDay()); // Convertir LocalDate en LocalDateTime
            collecte.setIdAgentCollecteur(this.idAgentCollecteurConnecte);

            // Ici, vous devriez sauvegarder dans la base de données
            System.out.println("Collecte à enregistrer: " + collecte);

            // Afficher un message de succès
            messageLabel.setText("✓ Collecte enregistrée avec succès!\n" +
                    "ID Intervention: " + idIntervention + "\n" +
                    "Quantité: " + quantite + " kg\n" +
                    "Date: " + dateCollecte.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            messageLabel.setStyle("-fx-text-fill: #2ecc71;");

            // Réinitialiser le formulaire après un délai
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> {
                        reinitialiserFormulaire();

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
    private void annulerCollecte() {
        reinitialiserFormulaire();
        messageLabel.setText("");
    }

    @FXML
    public void retourDashboard() {
        try {
            // Charger le dashboard conducteur (ou autre dashboard selon le rôle)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashConducteur.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de Bord");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner au dashboard", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showHistorique() {
        try {
            // Charger le nouveau fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("historique-collecte.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) historiqueBtn.getScene().getWindow(); // Remplacez "collecteButton" par votre bouton
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
    public void effectuerNouvelleCollecte() {
        reinitialiserFormulaire();
        messageLabel.setText("Prêt pour une nouvelle collecte");
        messageLabel.setStyle("-fx-text-fill: #3498db;");
    }

    @FXML
    public void historiqueCollectes() {
        // Ici, vous pouvez ouvrir une fenêtre d'historique
        showAlert("Information", "Fonctionnalité Historique à implémenter", Alert.AlertType.INFORMATION);
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

                        Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Connexion - Gestion des Déchets");

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

    private boolean validateForm() {
        // Vérifier ID intervention
        if (idInterventionField.getText().trim().isEmpty()) {
            showError("L'ID de l'intervention est requis");
            return false;
        }

        // Vérifier que l'intervention a été trouvée
        if (adresseInterventionLabel.getText().isEmpty()) {
            showError("Veuillez d'abord rechercher et sélectionner une intervention valide");
            return false;
        }

        // Vérifier quantité
        String quantiteText = quantiteCollecteeField.getText().trim();
        if (quantiteText.isEmpty() && quantiteSpinner.getValue() == 0) {
            showError("La quantité collectée est requise");
            return false;
        }

        // Vérifier que la quantité est positive
        try {
            double quantite = quantiteText.isEmpty() ? quantiteSpinner.getValue() : Double.parseDouble(quantiteText);
            if (quantite <= 0) {
                showError("La quantité doit être supérieure à 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Quantité invalide");
            return false;
        }

        // Vérifier date
        if (dateCollectePicker.getValue() == null) {
            showError("La date de collecte est requise");
            return false;
        }

        // Vérifier que la date n'est pas dans le futur
        if (dateCollectePicker.getValue().isAfter(LocalDate.now())) {
            showError("La date de collecte ne peut pas être dans le futur");
            return false;
        }

        return true;
    }

    private void reinitialiserFormulaire() {
        idInterventionField.clear();
        quantiteCollecteeField.clear();
        quantiteSpinner.getValueFactory().setValue(0.0);
        dateCollectePicker.setValue(LocalDate.now());
        clearInterventionInfo();
        messageLabel.setText("");
    }

    private void clearInterventionInfo() {
        adresseInterventionLabel.setText("");
        dateInterventionLabel.setText("");
        statutInterventionLabel.setText("");
    }


    private void showError(String message) {
        messageLabel.setText("✗ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe Collecte correspondant à la table
    public static class Collecte {
        private int idCollecte;
        private LocalDateTime dateCollecte;
        private double quantiteCollectee;
        private int idIntervention;
        private int idAgentCollecteur;

        // Getters et Setters
        public int getIdCollecte() { return idCollecte; }
        public void setIdCollecte(int idCollecte) { this.idCollecte = idCollecte; }

        public LocalDateTime getDateCollecte() { return dateCollecte; }
        public void setDateCollecte(LocalDateTime dateCollecte) { this.dateCollecte = dateCollecte; }

        public double getQuantiteCollectee() { return quantiteCollectee; }
        public void setQuantiteCollectee(double quantiteCollectee) { this.quantiteCollectee = quantiteCollectee; }

        public int getIdIntervention() { return idIntervention; }
        public void setIdIntervention(int idIntervention) { this.idIntervention = idIntervention; }

        public int getIdAgentCollecteur() { return idAgentCollecteur; }
        public void setIdAgentCollecteur(int idAgentCollecteur) { this.idAgentCollecteur = idAgentCollecteur; }

        @Override
        public String toString() {
            return "Collecte{" +
                    "idCollecte=" + idCollecte +
                    ", dateCollecte=" + dateCollecte +
                    ", quantiteCollectee=" + quantiteCollectee +
                    ", idIntervention=" + idIntervention +
                    ", idAgentCollecteur=" + idAgentCollecteur +
                    '}';
        }
    }
}