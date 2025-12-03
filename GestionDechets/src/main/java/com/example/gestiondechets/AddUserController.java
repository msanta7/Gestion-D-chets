package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddUserController {

    @FXML private TextField nomField, prenomField, telephoneField;
    @FXML private TextArea adresseField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button btnAjouter;

    private Stage stage;

    @FXML
    public void initialize() {
        // Initialiser le ComboBox avec le premier rôle sélectionné
        roleCombo.getSelectionModel().selectFirst();

        // Désactiver le bouton ajouter initialement
        btnAjouter.setDisable(true);

        // Écouter les changements pour valider le formulaire
        nomField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        boolean isValid = true;

        // Validation des champs obligatoires selon la table
        if (nomField.getText().trim().isEmpty()) isValid = false;
        if (prenomField.getText().trim().isEmpty()) isValid = false;
        if (telephoneField.getText().trim().isEmpty()) isValid = false;
        if (adresseField.getText().trim().isEmpty()) isValid = false;
        if (passwordField.getText().isEmpty()) isValid = false;
        if (confirmPasswordField.getText().isEmpty()) isValid = false;
        if (roleCombo.getValue() == null) isValid = false;

        // Validation du format téléphone (Maroc)
        String telephone = telephoneField.getText().trim();
        if (!telephone.isEmpty() && !telephone.matches("^0[5-7][0-9]{8}$")) {
            isValid = false;
        }

        // Validation confirmation mot de passe
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            isValid = false;
        }

        btnAjouter.setDisable(!isValid);
    }

    @FXML
    private void ajouterUtilisateur() {
        // Récupérer les données
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseField.getText().trim();
        String role = roleCombo.getValue();
        String password = passwordField.getText();

        // Validation supplémentaire
        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
            showError("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        if (!telephone.matches("^0[5-7][0-9]{8}$")) {
            showError("Format incorrect",
                    "Le numéro de téléphone doit être valide:\n" +
                            "• Commencer par 05, 06 ou 07\n" +
                            "• Contenir 10 chiffres\n" +
                            "Exemple: 0612345678");
            return;
        }

        if (!password.equals(confirmPasswordField.getText())) {
            showError("Mot de passe", "Les mots de passe ne correspondent pas.");
            return;
        }

        // Insérer dans la base de données
        if (insererUtilisateurBD(nom, prenom, telephone, adresse, role, password)) {
            // Afficher confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Utilisateur ajouté");
            alert.setHeaderText(null);
            alert.setContentText("L'utilisateur a été ajouté avec succès !");
            alert.showAndWait();

            // Fermer la fenêtre
            fermerPopup();
        } else {
            showError("Erreur", "Impossible d'ajouter l'utilisateur dans la base de données.");
        }
    }

    private boolean insererUtilisateurBD(String nom, String prenom, String telephone,
                                         String adresse, String role, String motDePasse) {
        // Combiner nom et prénom pour correspondre à la table
        String nomComplet = prenom + " " + nom;

        // Hash du mot de passe (dans une vraie app, utilisez BCrypt)
        // Pour l'instant, on stocke en clair pour les tests

        String sql = "INSERT INTO UTILISATEUR (nom, telephone, adresse, role, mot_de_passe) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomComplet);          // nom (prénom + nom)
            pstmt.setString(2, telephone);           // telephone
            pstmt.setString(3, adresse);             // adresse
            pstmt.setString(4, role);                // role
            pstmt.setString(5, motDePasse);          // mot_de_passe (À HASHER en production)

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Utilisateur inséré avec succès: " + nomComplet);
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur BD", "Erreur lors de l'insertion: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void fermerPopup() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}