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
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;

public class RapportsController {

    @FXML private Label userNameLabel;
    @FXML private Label monthSignalements;
    @FXML private Label monthCollectes;
    @FXML private Label monthDechets;
    @FXML private Label monthRecyclage;

    // Period radio buttons
    @FXML private RadioButton dailyRadio;
    @FXML private RadioButton weeklyRadio;
    @FXML private RadioButton monthlyRadio;
    @FXML private RadioButton yearlyRadio;
    @FXML private RadioButton customRadio;

    // Date pickers
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Report type radio buttons
    @FXML private RadioButton signalementsReport;
    @FXML private RadioButton collectesReport;
    @FXML private RadioButton dechetsReport;
    @FXML private RadioButton recyclageReport;

    @FXML private Button generateBtn;

    private Connection conn;
    private ToggleGroup periodGroup;
    private ToggleGroup reportTypeGroup;

    @FXML
    public void initialize() {
        conn = Database.connectDB();

        // Set up toggle groups
        periodGroup = new ToggleGroup();
        dailyRadio.setToggleGroup(periodGroup);
        weeklyRadio.setToggleGroup(periodGroup);
        monthlyRadio.setToggleGroup(periodGroup);
        yearlyRadio.setToggleGroup(periodGroup);
        customRadio.setToggleGroup(periodGroup);

        reportTypeGroup = new ToggleGroup();
        signalementsReport.setToggleGroup(reportTypeGroup);
        collectesReport.setToggleGroup(reportTypeGroup);
        dechetsReport.setToggleGroup(reportTypeGroup);
        recyclageReport.setToggleGroup(reportTypeGroup);

        // Set default selections
        monthlyRadio.setSelected(true);
        dechetsReport.setSelected(true);

        // Initialize date pickers
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // Disable custom date pickers initially
        startDatePicker.setDisable(true);
        endDatePicker.setDisable(true);

        // Add listener to enable/disable custom date pickers
        customRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            startDatePicker.setDisable(!newVal);
            endDatePicker.setDisable(!newVal);
        });

        // Set user name
        if (Database.getActiveUser() != null) {
            userNameLabel.setText("Admin : " + Database.getActiveUser().getNom());
        }

        // Load monthly statistics
        chargerStatistiquesMois();
    }

    private void chargerStatistiquesMois() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();

        // 1. Signalements du mois
        String sqlSignalements = """
            SELECT COUNT(*) as count FROM SIGNALEMENT 
            WHERE date_signalement >= ? AND date_signalement <= ?
        """;

        // 2. Collectes du mois
        String sqlCollectes = """
            SELECT COUNT(*) as count FROM COLLECTE 
            WHERE date_collecte >= ? AND date_collecte <= ?
        """;

        // 3. Déchets triés du mois
        String sqlDechets = """
            SELECT SUM(quantite) as total FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
        """;

        // 4. Taux de recyclage
        String sqlRecyclage = """
            SELECT 
                (SELECT COUNT(*) FROM DECHET WHERE statut_tri IN ('recycle', 'trie') AND date_tri >= ? AND date_tri <= ?) * 100.0 /
                NULLIF((SELECT COUNT(*) FROM DECHET WHERE date_tri >= ? AND date_tri <= ?), 0) as taux
        """;

        try {
            // Signalements
            try (PreparedStatement ps = conn.prepareStatement(sqlSignalements)) {
                ps.setDate(1, Date.valueOf(startOfMonth));
                ps.setDate(2, Date.valueOf(endOfMonth));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    monthSignalements.setText(String.valueOf(rs.getInt("count")));
                }
            }

            // Collectes
            try (PreparedStatement ps = conn.prepareStatement(sqlCollectes)) {
                ps.setDate(1, Date.valueOf(startOfMonth));
                ps.setDate(2, Date.valueOf(endOfMonth));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    monthCollectes.setText(String.valueOf(rs.getInt("count")));
                }
            }

            // Déchets triés
            try (PreparedStatement ps = conn.prepareStatement(sqlDechets)) {
                ps.setDate(1, Date.valueOf(startOfMonth));
                ps.setDate(2, Date.valueOf(endOfMonth));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    monthDechets.setText(String.format("%.1f", total));
                }
            }

            // Taux recyclage
            try (PreparedStatement ps = conn.prepareStatement(sqlRecyclage)) {
                ps.setDate(1, Date.valueOf(startOfMonth));
                ps.setDate(2, Date.valueOf(endOfMonth));
                ps.setDate(3, Date.valueOf(startOfMonth));
                ps.setDate(4, Date.valueOf(endOfMonth));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double taux = rs.getDouble("taux");
                    monthRecyclage.setText(String.format("%.1f%%", taux));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les statistiques");
        }
    }

    @FXML
    private void generateReport() {
        // Validate selections
        if (reportTypeGroup.getSelectedToggle() == null) {
            showAlert("Sélection manquante", "Veuillez sélectionner un type de rapport", Alert.AlertType.WARNING);
            return;
        }

        // Get selected report type
        RadioButton selectedReport = (RadioButton) reportTypeGroup.getSelectedToggle();
        String reportType = selectedReport.getText();

        // Get date range
        LocalDate startDate, endDate;

        if (customRadio.isSelected()) {
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                showAlert("Dates manquantes", "Veuillez sélectionner les dates pour la période personnalisée", Alert.AlertType.WARNING);
                return;
            }
            startDate = startDatePicker.getValue();
            endDate = endDatePicker.getValue();
        } else {
            // Calculate date range based on period selection
            LocalDate today = LocalDate.now();
            if (dailyRadio.isSelected()) {
                startDate = today;
                endDate = today;
            } else if (weeklyRadio.isSelected()) {
                startDate = today.minusDays(7);
                endDate = today;
            } else if (monthlyRadio.isSelected()) {
                startDate = today.withDayOfMonth(1);
                endDate = today;
            } else { // yearly
                startDate = today.withDayOfYear(1);
                endDate = today;
            }
        }

        // Generate PDF based on report type
        try {
            if (reportType.equals("Rapport des Signalements")) {
                generateSignalementsReport(startDate, endDate);
            } else if (reportType.equals("Rapport des Collectes")) {
                generateCollectesReport(startDate, endDate);
            } else if (reportType.equals("Rapport des Déchets")) {
                generateDechetsReport(startDate, endDate);
            } else if (reportType.equals("Rapport de Recyclage")) {
                generateRecyclageReport(startDate, endDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de générer le rapport: " + e.getMessage());
        }
    }

    private void generateDechetsReport(LocalDate startDate, LocalDate endDate) throws FileNotFoundException {
        // FIXED: Use LocalDateTime instead of LocalDate
        String fileName = "rapport_dechets_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Add title
            Paragraph title = new Paragraph("RAPPORT DES DÉCHETS TRIÉS")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold();
            document.add(title);

            // Add period
            Paragraph period = new Paragraph("Période: " +
                    startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
                    endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
            document.add(period);

            document.add(new Paragraph("\n"));

            // 1. Summary statistics
            String summarySql = """
            SELECT 
                COUNT(*) as total_dechets,
                SUM(quantite) as total_quantite,
                AVG(quantite) as moyenne_quantite,
                COUNT(DISTINCT id_agent_tri) as agents_impliques
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
        """;

            try (PreparedStatement ps = conn.prepareStatement(summarySql)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Paragraph summaryTitle = new Paragraph("RÉSUMÉ STATISTIQUE")
                            .setBold()
                            .setFontSize(14);
                    document.add(summaryTitle);

                    document.add(new Paragraph("Nombre total de déchets triés: " + rs.getInt("total_dechets")));
                    document.add(new Paragraph("Quantité totale triée: " + String.format("%.2f kg", rs.getDouble("total_quantite"))));
                    document.add(new Paragraph("Quantité moyenne par triage: " + String.format("%.2f kg", rs.getDouble("moyenne_quantite"))));
                    document.add(new Paragraph("Agents de tri impliqués: " + rs.getInt("agents_impliques")));
                }
            }

            document.add(new Paragraph("\n"));

            // 2. By waste type
            String typeSql = """
            SELECT 
                type_dechet,
                COUNT(*) as nombre,
                SUM(quantite) as quantite_totale,
                AVG(quantite) as quantite_moyenne
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
            GROUP BY type_dechet
            ORDER BY quantite_totale DESC
        """;

            Paragraph typeTitle = new Paragraph("DÉTAILS PAR TYPE DE DÉCHET")
                    .setBold()
                    .setFontSize(14);
            document.add(typeTitle);

            // Create table for waste types
            Table typeTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
                    .useAllAvailableWidth();

            typeTable.addHeaderCell("Type de déchet");
            typeTable.addHeaderCell("Nombre");
            typeTable.addHeaderCell("Quantité totale (kg)");
            typeTable.addHeaderCell("Moyenne (kg)");

            try (PreparedStatement ps = conn.prepareStatement(typeSql)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    typeTable.addCell(rs.getString("type_dechet"));
                    typeTable.addCell(String.valueOf(rs.getInt("nombre")));
                    typeTable.addCell(String.format("%.2f", rs.getDouble("quantite_totale")));
                    typeTable.addCell(String.format("%.2f", rs.getDouble("quantite_moyenne")));
                }
            }

            document.add(typeTable);
            document.add(new Paragraph("\n"));

            // 3. By category
            String categorieSql = """
            SELECT 
                categorie,
                COUNT(*) as nombre,
                SUM(quantite) as quantite_totale
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
            GROUP BY categorie
            ORDER BY quantite_totale DESC
        """;

            Paragraph catTitle = new Paragraph("DÉTAILS PAR CATÉGORIE")
                    .setBold()
                    .setFontSize(14);
            document.add(catTitle);

            Table catTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                    .useAllAvailableWidth();

            catTable.addHeaderCell("Catégorie");
            catTable.addHeaderCell("Nombre");
            catTable.addHeaderCell("Quantité totale (kg)");

            try (PreparedStatement ps = conn.prepareStatement(categorieSql)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    catTable.addCell(rs.getString("categorie"));
                    catTable.addCell(String.valueOf(rs.getInt("nombre")));
                    catTable.addCell(String.format("%.2f", rs.getDouble("quantite_totale")));
                }
            }

            document.add(catTable);
            document.add(new Paragraph("\n"));

            // 4. By toxicity
            String toxiciteSql = """
            SELECT 
                toxicite,
                COUNT(*) as nombre,
                SUM(quantite) as quantite_totale
            FROM DECHET 
            WHERE date_tri >= ? AND date_tri <= ?
            GROUP BY toxicite
            ORDER BY 
                CASE toxicite
                    WHEN 'elevee' THEN 1
                    WHEN 'moyenne' THEN 2
                    WHEN 'faible' THEN 3
                    WHEN 'non' THEN 4
                    ELSE 5
                END
        """;

            Paragraph toxTitle = new Paragraph("DÉTAILS PAR NIVEAU DE TOXICITÉ")
                    .setBold()
                    .setFontSize(14);
            document.add(toxTitle);

            Table toxTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                    .useAllAvailableWidth();

            toxTable.addHeaderCell("Niveau de toxicité");
            toxTable.addHeaderCell("Nombre");
            toxTable.addHeaderCell("Quantité totale (kg)");

            try (PreparedStatement ps = conn.prepareStatement(toxiciteSql)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String toxicite = rs.getString("toxicite");
                    String toxiciteDisplay;
                    switch (toxicite) {
                        case "elevee": toxiciteDisplay = "Élevée"; break;
                        case "moyenne": toxiciteDisplay = "Moyenne"; break;
                        case "faible": toxiciteDisplay = "Faible"; break;
                        case "non": toxiciteDisplay = "Non toxique"; break;
                        default: toxiciteDisplay = toxicite;
                    }
                    toxTable.addCell(toxiciteDisplay);
                    toxTable.addCell(String.valueOf(rs.getInt("nombre")));
                    toxTable.addCell(String.format("%.2f", rs.getDouble("quantite_totale")));
                }
            }

            document.add(toxTable);
            document.add(new Paragraph("\n"));

            // 5. By agent
            String agentSql = """
            SELECT 
                u.nom as agent_nom,
                COUNT(d.id_dechet) as nombre_dechets,
                SUM(d.quantite) as quantite_totale
            FROM DECHET d
            JOIN UTILISATEUR u ON u.id_utilisateur = d.id_agent_tri
            WHERE d.date_tri >= ? AND d.date_tri <= ?
            GROUP BY u.nom
            ORDER BY quantite_totale DESC
        """;

            Paragraph agentTitle = new Paragraph("PERFORMANCE DES AGENTS DE TRI")
                    .setBold()
                    .setFontSize(14);
            document.add(agentTitle);

            Table agentTable = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                    .useAllAvailableWidth();

            agentTable.addHeaderCell("Agent de tri");
            agentTable.addHeaderCell("Nombre de déchets");
            agentTable.addHeaderCell("Quantité totale (kg)");

            try (PreparedStatement ps = conn.prepareStatement(agentSql)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    agentTable.addCell(rs.getString("agent_nom"));
                    agentTable.addCell(String.valueOf(rs.getInt("nombre_dechets")));
                    agentTable.addCell(String.format("%.2f", rs.getDouble("quantite_totale")));
                }
            }

            document.add(agentTable);

            // Footer - FIXED: Use LocalDateTime instead of LocalDate
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setItalic();
            document.add(footer);

            document.close();

            showAlert("Succès", "Rapport généré avec succès: " + fileName, Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage());
        }
    }
    private void generateSignalementsReport(LocalDate startDate, LocalDate endDate) throws FileNotFoundException {
        // Similar structure for signalements report
        // You can implement this similarly
    }

    private void generateCollectesReport(LocalDate startDate, LocalDate endDate) throws FileNotFoundException {
        // Similar structure for collectes report
    }

    private void generateRecyclageReport(LocalDate startDate, LocalDate endDate) throws FileNotFoundException {
        // Similar structure for recyclage report
    }

    // Navigation methods
    @FXML
    private void showDashboard(ActionEvent event) {
        navigateTo(event, "DashAdmin.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        navigateTo(event, "gestUsers.fxml");
    }

    @FXML
    private void showReports(ActionEvent event) {
        // Already on reports page
    }

    @FXML
    private void showSettings(ActionEvent event) {
        navigateTo(event, "settings.fxml");
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