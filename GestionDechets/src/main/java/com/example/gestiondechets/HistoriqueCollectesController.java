package com.example.gestiondechets;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class HistoriqueCollectesController implements Initializable {

    @FXML private Label nomAgentCollecteurLabel;

    @FXML private Label nombreResultatsLabel;
    @FXML private Label messageLabel;

    @FXML private TextField searchField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;

    @FXML private TableView<Collecte> collectesTableView;
    @FXML private TableColumn<Collecte, Integer> idColonne;
    @FXML private TableColumn<Collecte, Integer> idInterventionColonne;
    @FXML private TableColumn<Collecte, String> adresseColonne;
    @FXML private TableColumn<Collecte, LocalDate> dateColonne;
    @FXML private TableColumn<Collecte, Double> quantiteColonne;
    @FXML private TableColumn<Collecte, String> statutColonne;
    @FXML private TableColumn<Collecte, String> agentColonne;
    @FXML private TableColumn<Collecte, String> actionsColonne;

    @FXML private Pagination pagination;
    @FXML private Button voirDetailsBtn;
    @FXML private Button supprimerBtn;

    private ObservableList<Collecte> collectesList = FXCollections.observableArrayList();
    private FilteredList<Collecte> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les colonnes
        initializeTableColumns();

        // Charger les données
        loadCollectesData();

        // Configurer la pagination
        setupPagination();

        // Configurer les filtres
        setupFilters();

        // Gérer la sélection dans le tableau
        setupSelectionListener();


        // Configurer les date pickers
        dateDebutPicker.setValue(LocalDate.now().minusMonths(1));
        dateFinPicker.setValue(LocalDate.now());
    }

    private void initializeTableColumns() {
        idColonne.setCellValueFactory(new PropertyValueFactory<>("idCollecte"));
        idInterventionColonne.setCellValueFactory(new PropertyValueFactory<>("idIntervention"));
        adresseColonne.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        dateColonne.setCellValueFactory(new PropertyValueFactory<>("dateCollecte"));
        quantiteColonne.setCellValueFactory(new PropertyValueFactory<>("quantiteCollectee"));
        statutColonne.setCellValueFactory(new PropertyValueFactory<>("statut"));
        agentColonne.setCellValueFactory(new PropertyValueFactory<>("nomAgent"));

        // Colonne actions avec boutons
        actionsColonne.setCellFactory(column -> new TableCell<Collecte, String>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    Collecte collecte = getTableView().getItems().get(getIndex());
                    editCollecte(collecte);
                });

                deleteBtn.setOnAction(event -> {
                    Collecte collecte = getTableView().getItems().get(getIndex());
                    deleteCollecte(collecte);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadCollectesData() {
        // Charger les données depuis la base de données
        // Exemple avec des données fictives
        collectesList.clear();
        collectesList.addAll(
                new Collecte(1, 101, "123 Rue Principale", LocalDate.now().minusDays(2), 150.5, "Terminée", "Jean Dupont"),
                new Collecte(2, 102, "456 Avenue Centrale", LocalDate.now().minusDays(1), 200.0, "Terminée", "Marie Martin"),
                new Collecte(3, 103, "789 Boulevard Nord", LocalDate.now(), 175.3, "En cours", "Pierre Durand")
        );

        filteredData = new FilteredList<>(collectesList, p -> true);
        SortedList<Collecte> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(collectesTableView.comparatorProperty());
        collectesTableView.setItems(sortedData);
    }

    private void setupPagination() {
        int itemsPerPage = 10;
        int pageCount = (int) Math.ceil((double) collectesList.size() / itemsPerPage);
        pagination.setPageCount(pageCount);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int fromIndex = newIndex.intValue() * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, collectesList.size());
            collectesTableView.setItems(FXCollections.observableArrayList(
                    collectesList.subList(fromIndex, toIndex)
            ));
        });
    }

    private void setupFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(collecte -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return collecte.getAdresse().toLowerCase().contains(lowerCaseFilter)
                        || String.valueOf(collecte.getIdCollecte()).contains(lowerCaseFilter)
                        || String.valueOf(collecte.getIdIntervention()).contains(lowerCaseFilter)
                        || collecte.getNomAgent().toLowerCase().contains(lowerCaseFilter);
            });
            updateResultsCount();
        });
    }

    private void setupSelectionListener() {
        collectesTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean itemSelected = newValue != null;
                    voirDetailsBtn.setDisable(!itemSelected);
                    supprimerBtn.setDisable(!itemSelected);
                }
        );
    }


    private void updateResultsCount() {
        int count = filteredData.size();
        nombreResultatsLabel.setText(count + " collecte" + (count != 1 ? "s" : "") + " trouvée" + (count != 1 ? "s" : ""));
    }

    @FXML
    private void appliquerFiltres() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        if (debut != null && fin != null && debut.isAfter(fin)) {
            showAlert("Erreur", "La date de début doit être avant la date de fin", Alert.AlertType.ERROR);
            return;
        }

        filteredData.setPredicate(collecte -> {
            boolean matchesSearch = true;
            boolean matchesDate = true;

            // Filtre de recherche
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchTerm = searchField.getText().toLowerCase();
                matchesSearch = collecte.getAdresse().toLowerCase().contains(searchTerm)
                        || String.valueOf(collecte.getIdCollecte()).contains(searchTerm)
                        || String.valueOf(collecte.getIdIntervention()).contains(searchTerm)
                        || collecte.getNomAgent().toLowerCase().contains(searchTerm);
            }

            // Filtre par date
            if (debut != null && fin != null) {
                LocalDate dateCollecte = collecte.getDateCollecte();
                matchesDate = !dateCollecte.isBefore(debut) && !dateCollecte.isAfter(fin);
            }

            return matchesSearch && matchesDate;
        });
    }



    @FXML
    private void voirDetailsCollecte() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Afficher les détails
            showAlert("Détails",
                    String.format("ID: %d\nIntervention: %d\nAdresse: %s\nDate: %s\nQuantité: %.1f kg\nStatut: %s\nAgent: %s",
                            selected.getIdCollecte(),
                            selected.getIdIntervention(),
                            selected.getAdresse(),
                            selected.getDateCollecte(),
                            selected.getQuantiteCollectee(),
                            selected.getStatut(),
                            selected.getNomAgent()),
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void supprimerCollecte() {
        Collecte selected = collectesTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer la collecte #" + selected.getIdCollecte());
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette collecte ? Cette action est irréversible.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                collectesList.remove(selected);
                messageLabel.setText("Collecte #" + selected.getIdCollecte() + " supprimée avec succès");
            }
        }
    }

    private void editCollecte(Collecte collecte) {
        // Implémenter l'édition
        messageLabel.setText("Édition de la collecte #" + collecte.getIdCollecte());
    }

    private void deleteCollecte(Collecte collecte) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la collecte #" + collecte.getIdCollecte());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette collecte ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            collectesList.remove(collecte);
            messageLabel.setText("Collecte #" + collecte.getIdCollecte() + " supprimée");
        }
    }

    @FXML
    private void retourDashboard() {
        // Navigation vers le dashboard
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void effectuerNouvelleCollecte() {
        // Navigation vers la page de collecte
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("effectuer-collecte.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void historiqueCollectes() {
        // Déjà sur cette page
        messageLabel.setText("Vous êtes déjà sur la page historique");
    }

    @FXML
    private void handleLogout() {
        // Implémenter la déconnexion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Se déconnecter");
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Navigation vers la page de login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) nomAgentCollecteurLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Classe interne Collecte (à adapter selon votre modèle)
    public static class Collecte {
        private final int idCollecte;
        private final int idIntervention;
        private final String adresse;
        private final LocalDate dateCollecte;
        private final double quantiteCollectee;
        private final String statut;
        private final String nomAgent;

        public Collecte(int idCollecte, int idIntervention, String adresse,
                        LocalDate dateCollecte, double quantiteCollectee,
                        String statut, String nomAgent) {
            this.idCollecte = idCollecte;
            this.idIntervention = idIntervention;
            this.adresse = adresse;
            this.dateCollecte = dateCollecte;
            this.quantiteCollectee = quantiteCollectee;
            this.statut = statut;
            this.nomAgent = nomAgent;
        }

        // Getters
        public int getIdCollecte() { return idCollecte; }
        public int getIdIntervention() { return idIntervention; }
        public String getAdresse() { return adresse; }
        public LocalDate getDateCollecte() { return dateCollecte; }
        public double getQuantiteCollectee() { return quantiteCollectee; }
        public String getStatut() { return statut; }
        public String getNomAgent() { return nomAgent; }
        public String getActions() { return ""; } // Pour la colonne actions
    }
}