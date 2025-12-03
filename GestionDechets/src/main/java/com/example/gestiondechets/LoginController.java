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
    private void handleLogin() {
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
        String role = validerIdentifiants(telephone, password);

        if (role != null) {
            // Rediriger vers la page appropriée selon le rôle
            if (role.equals("ADMIN")) {
                chargerPage("/com/example/gestiondechets/DashAdmin.fxml");
            } else if (role.equals("CITOYEN")) {
                chargerPage("/com/example/gestiondechets/DashCitoyen.fxml");
            } else if (role.equals("AGENT")) {
                chargerPage("/com/example/gestiondechets/DashTri.fxml");
            } else if (role.equals("CONDUCTEUR")) {
                chargerPage("/com/example/gestiondechets/DashConducteur.fxml");
            }
        } else {
            afficherErreurDialog("Échec de connexion",
                    "Numéro de téléphone ou mot de passe incorrect");
        }
    }

    /**
     * Méthode de validation des identifiants avec détection de rôle
     */
    private String validerIdentifiants(String telephone, String password) {
        // === ADMINISTRATEURS ===
        // Admin principal
        if ("0612345678".equals(telephone) && "admin123".equals(password)) {
            return "ADMIN";
        }

        // Second admin
        if ("0623456789".equals(telephone) && "admin456".equals(password)) {
            return "ADMIN";
        }

        // === CITOYENS ===
        // Exemple spécifique : 056151551
        if ("056151551".equals(telephone)) {
            // Pour ce numéro spécifique, accepter plusieurs mots de passe pour tests
            if ("citoyen123".equals(password) || "password".equals(password) || "123456".equals(password)) {
                return "CITOYEN";
            }
        }

        // Tous les numéros commençant par 05 (fixe au Maroc) = citoyens
        // Pour faciliter les tests pendant le développement
        if (telephone.startsWith("05")) {
            // Accepter n'importe quel mot de passe pour les numéros 05xxx
            // Dans une version réelle, il faudrait vérifier en base de données
            return "CITOYEN";
        }

        // Numéros mobiles 06/07 peuvent être citoyens aussi
        if ("0611111111".equals(telephone) && "citoyen456".equals(password)) {
            return "CITOYEN";
        }

        if ("0622222222".equals(telephone) && "citoyen789".equals(password)) {
            return "CITOYEN";
        }

        // === AGENTS ===
        if ("0633333333".equals(telephone) && "agent123".equals(password)) {
            return "AGENT";
        }

        // === CONDUCTEURS ===
        if ("0644444444".equals(telephone) && "conducteur123".equals(password)) {
            return "CONDUCTEUR";
        }

        // Par défaut, refuser la connexion
        return null;
    }

    /**
     * Méthode pour afficher une information
     */
    private void afficherInfoDialog(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}