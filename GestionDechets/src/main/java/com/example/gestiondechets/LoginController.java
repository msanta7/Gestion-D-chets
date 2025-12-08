package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    /**
     * Méthode pour afficher une boîte de dialogue d'erreur
     */
    private void afficherErreurDialog(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Méthode générique pour changer de page FXML
     */
    private void chargerPage(String fxmlPath) {
        try {
            // Charger le nouveau FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nouvellePage = loader.load();

            // Récupérer la scène actuelle et la fenêtre
            Stage stage = (Stage) telephoneField.getScene().getWindow();
            Scene scene = new Scene(nouvellePage);

            // Appliquer la nouvelle scène
            stage.setScene(scene);
            stage.show();

            // Centrer la fenêtre après le changement
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreurDialog("Erreur de navigation",
                    "Impossible de charger la page : " + fxmlPath + "\n\nErreur : " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            afficherErreurDialog("Fichier non trouvé",
                    "Le fichier FXML n'a pas été trouvé : " + fxmlPath + "\n\nAssurez-vous que le fichier existe dans le dossier resources.");
        }
    }

    /**
     * Méthode appelée quand on clique sur le bouton de connexion
     */
    @FXML
    private void handleLogin() throws SQLException {
        String telephone = telephoneField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation des champs
        if (telephone.isEmpty()) {
            afficherErreurDialog("Erreur de connexion", "Veuillez saisir votre numéro de téléphone");
            return;
        }

        if (password.isEmpty()) {
            afficherErreurDialog("Erreur de connexion", "Veuillez saisir votre mot de passe");
            return;
        }

        // Vérifier le format du numéro de téléphone (Maroc)
        if (!telephone.matches("^0[5-7][0-9]{8}$")) {
            afficherErreurDialog("Format incorrect",
                    "Le numéro de téléphone doit être au format marocain:\n" +
                            "• Commencer par 05, 06 ou 07\n" +
                            "• Contenir 10 chiffres\n" +
                            "Exemple: 0612345678");
            return;
        }

        // Valider les identifiants et déterminer le rôle
        String role = validerIdentifiants(telephone,password);

        System.out.println(role);
        if (role != null) {
            // Rediriger vers la page appropriée selon le rôle
            if (role.equals("administrateur")) {
                chargerPage("/com/example/gestiondechets/DashAdmin.fxml");
            } else if (role.equals("citoyen")) {
                chargerPage("/com/example/gestiondechets/DashCitoyen.fxml");
            } else if (role.equals("agent_tri")) {
                chargerPage("/com/example/gestiondechets/DashTri.fxml");
            } else if (role.equals("conducteur")) {
                chargerPage("/com/example/gestiondechets/DashConducteur.fxml");
            }
        } else {
            afficherErreurDialog("Échec de connexion",
                    "Numéro de téléphone ou mot de passe incorrect");
        }
        Database.loginUserByPhone(telephoneField.getText());
    }

    /**
     * Méthode de validation des identifiants avec détection de rôle
     */
    private String validerIdentifiants(String telephone, String password) throws SQLException {
        if (telephone.isEmpty()) {
            return "error";
        }
        if (password.isEmpty()){
            return "error";
        }

        try (Connection connection = Database.connectDB()) {
            String query = "SELECT role " +
                    "FROM utilisateur u " +
                    "WHERE u.telephone = ? AND u.mot_de_passe = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, telephone);       // phone number
            statement.setString(2, password);     // email value

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("role");   // correct phone + email
            } else {
                return null;   // incorrect phone OR incorrect email
            }
        }
    }

}

