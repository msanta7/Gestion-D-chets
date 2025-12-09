package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// PDFBox imports for PDF generation
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;

public class GenererRapportTriController {

    @FXML private Label nomAgentLabel;
    @FXML private TextField titreRapportField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> typeDechetCombo;
    @FXML private ComboBox<String> statutTriageCombo;

    private Connection conn;

    @FXML
    public void initialize() {
        conn = Database.connectDB();

        // Set agent name
        if (Database.getActiveUser() != null) {
            nomAgentLabel.setText(Database.getActiveUser().getNom());
        }

        // Set default dates (last month)
        dateDebutPicker.setValue(LocalDate.now().minusMonths(1));
        dateFinPicker.setValue(LocalDate.now());

        // Initialize combo boxes
        initTypeDechetCombo();
        initStatutTriageCombo();

        // Set default title
        titreRapportField.setText("Rapport de Tri - " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }

    private void initTypeDechetCombo() {
        typeDechetCombo.getItems().clear();
        typeDechetCombo.getItems().add("Tous les types");
        typeDechetCombo.getItems().addAll("Plastique", "Verre", "Métal", "Papier", "Organique");
        typeDechetCombo.setValue("Tous les types");
    }

    private void initStatutTriageCombo() {
        statutTriageCombo.getItems().clear();
        statutTriageCombo.getItems().add("Tous les statuts");
        statutTriageCombo.getItems().addAll("non_trie", "en_cours", "trie", "recycle", "elimine");
        statutTriageCombo.setValue("Tous les statuts");
    }

    @FXML
    private void genererRapport() {
        // Validate inputs
        if (titreRapportField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un titre pour le rapport", Alert.AlertType.ERROR);
            return;
        }

        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner les dates de début et de fin", Alert.AlertType.ERROR);
            return;
        }

        if (dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            showAlert("Erreur", "La date de début doit être avant la date de fin", Alert.AlertType.ERROR);
            return;
        }

        // Generate PDF
        try {
            generatePDFReport();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de générer le rapport: " + e.getMessage());
        }
    }

    private void generatePDFReport() {
        String titre = titreRapportField.getText();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String typeDechet = typeDechetCombo.getValue();
        String statut = statutTriageCombo.getValue();

        // Create filename
        String fileName = "rapport_dechets_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // Declare outside so we can reuse
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                float margin = 50;
                float yPosition = 750;
                float lineHeight = 20;

                // 1. TITLE
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(titre.toUpperCase());
                contentStream.endText();

                yPosition -= 30;

                // 2. PERIOD
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Période: " +
                        dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
                        dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();

                yPosition -= 20;

                // 3. FILTERS
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                String filters = "Filtres appliqués: ";
                filters += "Type: " + typeDechet + ", ";
                filters += "Statut: " + statut;
                contentStream.showText(filters);
                contentStream.endText();

                yPosition -= 30;

                // 4. SUMMARY STATISTICS
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("RÉSUMÉ STATISTIQUE");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                // Get summary data
                Object[] summaryData = getSummaryData(dateDebut, dateFin, typeDechet, statut);
                if (summaryData != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Total déchets triés: " + summaryData[0]);
                    contentStream.endText();

                    yPosition -= lineHeight;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Quantité totale: " + summaryData[1] + " kg");
                    contentStream.endText();

                    yPosition -= lineHeight;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Quantité moyenne par triage: " + summaryData[2] + " kg");
                    contentStream.endText();

                    yPosition -= lineHeight;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Nombre d'agents impliqués: " + summaryData[3]);
                    contentStream.endText();
                }

                yPosition -= 30;

                // 5. BY WASTE TYPE
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("DÉTAILS PAR TYPE DE DÉCHET");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                // Get waste type data
                List<Object[]> wasteTypeData = getWasteTypeData(dateDebut, dateFin, typeDechet, statut);
                for (Object[] row : wasteTypeData) {
                    if (yPosition < 100) {
                        // Close current stream and start new page
                        contentStream.close();
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("• %s: %d déchets, %.2f kg total, %.2f kg moyenne",
                            row[0], row[1], row[2], row[3]));
                    contentStream.endText();

                    yPosition -= lineHeight;
                }

                yPosition -= 20;

                // 6. BY CATEGORY
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("RÉPARTITION PAR CATÉGORIE");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                // Get category data
                List<Object[]> categoryData = getCategoryData(dateDebut, dateFin, typeDechet, statut);
                for (Object[] row : categoryData) {
                    if (yPosition < 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                    }

                    String categoryName = getCategoryDisplayName((String) row[0]);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("• %s: %d déchets, %.2f kg",
                            categoryName, row[1], row[2]));
                    contentStream.endText();

                    yPosition -= lineHeight;
                }

                yPosition -= 20;

                // 7. BY AGENT
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PERFORMANCE DES AGENTS");
                contentStream.endText();

                yPosition -= 20;
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                // Get agent data
                List<Object[]> agentData = getAgentData(dateDebut, dateFin, typeDechet, statut);
                for (Object[] row : agentData) {
                    if (yPosition < 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = 750;
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("• %s: %d déchets triés, %.2f kg",
                            row[0], row[1], row[2]));
                    contentStream.endText();

                    yPosition -= lineHeight;
                }

                yPosition -= 30;

                // 8. FOOTER
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Généré le: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                contentStream.endText();

                // CLOSE THE CONTENT STREAM HERE
                contentStream.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Save the document
            document.save(fileName);

            // Show success message
            showAlert("Succès",
                    "✅ Rapport généré avec succès!\n\n" +
                            "📄 Fichier: " + fileName + "\n" +
                            "📁 Emplacement: " + new File(fileName).getAbsolutePath(),
                    Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du PDF: " + e.getMessage(), e);
        }
    }
    private Object[] getSummaryData(LocalDate dateDebut, LocalDate dateFin, String typeDechet, String statut) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                COUNT(*) as total_dechets,
                SUM(quantite) as total_quantite,
                AVG(quantite) as moyenne_quantite,
                COUNT(DISTINCT id_agent_tri) as agents_impliques
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateDebut));
        params.add(Date.valueOf(dateFin));

        // Add type filter if not "Tous les types"
        if (!"Tous les types".equals(typeDechet)) {
            sql.append(" AND type_dechet = ?");
            params.add(typeDechet);
        }

        // Add status filter if not "Tous les statuts"
        if (!"Tous les statuts".equals(statut)) {
            sql.append(" AND statut_tri = ?");
            params.add(statut);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Object[]{
                        rs.getInt("total_dechets"),
                        String.format("%.2f", rs.getDouble("total_quantite")),
                        String.format("%.2f", rs.getDouble("moyenne_quantite")),
                        rs.getInt("agents_impliques")
                };
            }
        }
        return null;
    }

    private List<Object[]> getWasteTypeData(LocalDate dateDebut, LocalDate dateFin, String typeDechet, String statut) throws SQLException {
        List<Object[]> data = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                type_dechet,
                COUNT(*) as nombre,
                SUM(quantite) as quantite_totale,
                AVG(quantite) as quantite_moyenne
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateDebut));
        params.add(Date.valueOf(dateFin));

        // Add type filter if not "Tous les types"
        if (!"Tous les types".equals(typeDechet)) {
            sql.append(" AND type_dechet = ?");
            params.add(typeDechet);
        } else {
            sql.append(" GROUP BY type_dechet");
        }

        // Add status filter if not "Tous les statuts"
        if (!"Tous les statuts".equals(statut)) {
            if (!"Tous les types".equals(typeDechet)) {
                sql.append(" AND");
            } else {
                sql.append(" HAVING");
            }
            sql.append(" statut_tri = ?");
            params.add(statut);
        }

        sql.append(" ORDER BY quantite_totale DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                        rs.getString("type_dechet"),
                        rs.getInt("nombre"),
                        rs.getDouble("quantite_totale"),
                        rs.getDouble("quantite_moyenne")
                });
            }
        }
        return data;
    }

    private List<Object[]> getCategoryData(LocalDate dateDebut, LocalDate dateFin, String typeDechet, String statut) throws SQLException {
        List<Object[]> data = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                categorie,
                COUNT(*) as nombre,
                SUM(quantite) as quantite_totale
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateDebut));
        params.add(Date.valueOf(dateFin));

        if (!"Tous les types".equals(typeDechet)) {
            sql.append(" AND type_dechet = ?");
            params.add(typeDechet);
        }

        if (!"Tous les statuts".equals(statut)) {
            sql.append(" AND statut_tri = ?");
            params.add(statut);
        }

        sql.append(" GROUP BY categorie ORDER BY quantite_totale DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                        rs.getString("categorie"),
                        rs.getInt("nombre"),
                        rs.getDouble("quantite_totale")
                });
            }
        }
        return data;
    }

    private List<Object[]> getAgentData(LocalDate dateDebut, LocalDate dateFin, String typeDechet, String statut) throws SQLException {
        List<Object[]> data = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                u.nom as agent_nom,
                COUNT(d.id_dechet) as nombre_dechets,
                SUM(d.quantite) as quantite_totale
            FROM DECHET d
            JOIN UTILISATEUR u ON u.id_utilisateur = d.id_agent_tri
            WHERE d.date_tri >= ? AND d.date_tri <= ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateDebut));
        params.add(Date.valueOf(dateFin));

        if (!"Tous les types".equals(typeDechet)) {
            sql.append(" AND d.type_dechet = ?");
            params.add(typeDechet);
        }

        if (!"Tous les statuts".equals(statut)) {
            sql.append(" AND d.statut_tri = ?");
            params.add(statut);
        }

        sql.append(" GROUP BY u.nom ORDER BY quantite_totale DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                        rs.getString("agent_nom"),
                        rs.getInt("nombre_dechets"),
                        rs.getDouble("quantite_totale")
                });
            }
        }
        return data;
    }

    private String getCategoryDisplayName(String category) {
        switch (category) {
            case "menager": return "Ménager";
            case "industriel": return "Industriel";
            case "medical": return "Médical";
            case "agricole": return "Agricole";
            default: return category;
        }
    }

    // Navigation methods
    @FXML
    private void showDashboard(ActionEvent event) {
        navigateTo(event, "DashTri.fxml");
    }

    @FXML
    private void showTriage(ActionEvent event) {
        navigateTo(event, "triage-dechets.fxml");
    }

    @FXML
    private void showRapports(ActionEvent event) {
        // Already on reports page
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                navigateTo(event, "login.fxml");
                Database.logoutUserByPhone(Database.getActiveUser().getTelephone());

            }
        });
    }

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
            showError("Erreur de navigation", "Impossible de charger la page: " + fxmlFile);
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
}