package com.example.gestiondechets;

import java.sql.Timestamp;

public class Collecte {
    private int idCollecte;
    private int idIntervention;
    private int idAgentCollecteur;
    private Timestamp dateCollecte;
    private double quantiteCollectee;
    private String adresse;
    private String statut;

    // Constructeurs
    public Collecte() {}

    public Collecte(int idCollecte, int idIntervention, int idAgentCollecteur,
                    Timestamp dateCollecte, double quantiteCollectee,
                    String adresse, String statut) {
        this.idCollecte = idCollecte;
        this.idIntervention = idIntervention;
        this.idAgentCollecteur = idAgentCollecteur;
        this.dateCollecte = dateCollecte;
        this.quantiteCollectee = quantiteCollectee;
        this.adresse = adresse;
        this.statut = statut;
    }

    // Getters et Setters
    public int getIdCollecte() {
        return idCollecte;
    }

    public void setIdCollecte(int idCollecte) {
        this.idCollecte = idCollecte;
    }

    public int getIdIntervention() {
        return idIntervention;
    }

    public void setIdIntervention(int idIntervention) {
        this.idIntervention = idIntervention;
    }

    public int getIdAgentCollecteur() {
        return idAgentCollecteur;
    }

    public void setIdAgentCollecteur(int idAgentCollecteur) {
        this.idAgentCollecteur = idAgentCollecteur;
    }

    public Timestamp getDateCollecte() {
        return dateCollecte;
    }

    public void setDateCollecte(Timestamp dateCollecte) {
        this.dateCollecte = dateCollecte;
    }

    public double getQuantiteCollectee() {
        return quantiteCollectee;
    }

    public void setQuantiteCollectee(double quantiteCollectee) {
        this.quantiteCollectee = quantiteCollectee;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}