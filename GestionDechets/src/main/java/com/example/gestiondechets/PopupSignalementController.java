package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PopupSignalementController {

    // Éléments FXML
    @FXML private TextField adresseField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView previewImageView;
    @FXML private Label photoLabel;
    @FXML private Button btnValider, btnChoisirPhoto, btnSupprimerPhoto;

    // RadioButtons
    @FXML private RadioButton faibleRadio, moyenRadio, eleveRadio;

    private File photoFile;
    private Stage stage;
    private int citoyenId;
    private Runnable onSignalementAdded;

    // Add these missing methods
    public void setCitoyenId(int citoyenId) {
        this.citoyenId = citoyenId;
    }

    public void setOnSignalementAdded(Runnable onSignalementAdded) {
        this.onSignalementAdded = onSignalementAdded;
    }

    @FXML
    public void initialize() {
        // Désactiver le bouton valider tant que les champs requis ne sont pas remplis
        if (btnValider != null) {
            btnValider.setDisable(true);
        }

        // Activer le bouton valider quand l'adresse est remplie
        if (adresseField != null) {
            adresseField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateValiderButton();
            });
        }

        // Initialiser les boutons photo
        if (btnSupprimerPhoto != null) {
            btnSupprimerPhoto.setDisable(true);
        }

        // Sélectionner le bouton radio moyen par défaut
        if (moyenRadio != null) moyenRadio.setSelected(true);
    }

    private void updateValiderButton() {
        boolean isValid = true;

        // Seule validation : l'adresse doit être remplie
        if (adresseField != null && adresseField.getText().trim().isEmpty()) {
            isValid = false;
        }

        if (btnValider != null) {
            btnValider.setDisable(!isValid);
        }
    }

    @FXML
    private void choisirPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            // Vérifier la taille du fichier (max 5MB)
            if (selectedFile.length() > 5 * 1024 * 1024) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fichier trop volumineux");
                alert.setContentText("Le fichier ne doit pas dépasser 5MB.");
                alert.showAndWait();
                return;
            }

            photoFile = selectedFile;
            if (photoLabel != null) {
                photoLabel.setText(selectedFile.getName());
            }
            if (btnSupprimerPhoto != null) {
                btnSupprimerPhoto.setDisable(false);
            }

            try {
                // Charger l'image pour prévisualisation
                Image image = new Image(new FileInputStream(selectedFile));
                if (previewImageView != null) {
                    previewImageView.setImage(image);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de chargement");
                alert.setContentText("Impossible de charger l'image.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void supprimerPhoto() {
        photoFile = null;
        if (previewImageView != null) {
            previewImageView.setImage(null);
        }
        if (photoLabel != null) {
            photoLabel.setText("Aucune photo sélectionnée");
        }
        if (btnSupprimerPhoto != null) {
            btnSupprimerPhoto.setDisable(true);
        }
    }

    @FXML
    private void validerSignalement() {
        // Récupérer les données
        String adresse = (adresseField != null) ? adresseField.getText().trim() : "";
        String description = (descriptionArea != null) ? descriptionArea.getText().trim() : "";


        // Déterminer le niveau d'urgence
        String urgence = "MOYEN"; // Par défaut
        if (faibleRadio != null && faibleRadio.isSelected()) urgence = "FAIBLE";
        if (moyenRadio != null && moyenRadio.isSelected()) urgence = "MOYEN";
        if (eleveRadio != null && eleveRadio.isSelected()) urgence = "ELEVE";

        // Validation
        if (adresse.isEmpty()) {
            showError("L'adresse est obligatoire");
            return;
        }

        // Insérer dans la base de données
        try {
            Connection conn = Database.connectDB();
            if (conn != null) {
                // Remove id_citoyen from the query
                String query = "INSERT INTO SIGNALEMENT (adresse, description, etat) VALUES (?, ?, 'nouveau')";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, adresse);
                pst.setString(2, description);

                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    // Afficher confirmation
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Signalement envoyé");
                    alert.setHeaderText(null);
                    alert.setContentText("Votre signalement a été enregistré avec succès.\n\n" +
                            "Numéro de suivi: #" + (int)(Math.random() * 10000) + "\n" +
                            "Un agent traitera votre demande sous 48h.");
                    alert.showAndWait();

                    // Notify the dashboard to refresh
                    if (onSignalementAdded != null) {
                        onSignalementAdded.run();
                    }

                    // Fermer la fenêtre
                    fermerPopup();
                }

                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'enregistrement dans la base de données: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void fermerPopup() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}