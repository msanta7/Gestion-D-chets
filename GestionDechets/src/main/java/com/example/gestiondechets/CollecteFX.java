package com.example.gestiondechets;

import javafx.beans.property.*;

public class CollecteFX {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final DoubleProperty quantite = new SimpleDoubleProperty();
    private final StringProperty statut = new SimpleStringProperty();

    public CollecteFX(int id, double quantite, String statut) {
        setId(id);
        setQuantite(quantite);
        setStatut(statut);
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public double getQuantite() { return quantite.get(); }
    public void setQuantite(double value) { quantite.set(value); }
    public DoubleProperty quantiteProperty() { return quantite; }

    public String getStatut() { return statut.get(); }
    public void setStatut(String value) { statut.set(value); }
    public StringProperty statutProperty() { return statut; }
}