package com.example.gestiondechets;

import javafx.beans.property.*;

public class Triage {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final StringProperty typeDechet = new SimpleStringProperty();
    private final DoubleProperty quantite = new SimpleDoubleProperty();
    private final StringProperty statut = new SimpleStringProperty();

    public Triage(int id, String date, String typeDechet, double quantite, String statut) {
        setId(id);
        setDate(date);
        setTypeDechet(typeDechet);
        setQuantite(quantite);
        setStatut(statut);
    }

    // Getters et Setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getDate() { return date.get(); }
    public void setDate(String value) { date.set(value); }
    public StringProperty dateProperty() { return date; }

    public String getTypeDechet() { return typeDechet.get(); }
    public void setTypeDechet(String value) { typeDechet.set(value); }
    public StringProperty typeDechetProperty() { return typeDechet; }

    public double getQuantite() { return quantite.get(); }
    public void setQuantite(double value) { quantite.set(value); }
    public DoubleProperty quantiteProperty() { return quantite; }

    public String getStatut() { return statut.get(); }
    public void setStatut(String value) { statut.set(value); }
    public StringProperty statutProperty() { return statut; }
}
