package com.example.gestiondechets;

import java.time.LocalDateTime;

public class Collecte {
    private int idCollecte;
    private LocalDateTime dateCollecte;
    private double quantiteCollectee;
    private int idIntervention;
    private int idAgentCollecteur;
    private String statut; // Ajoutez ce champ

    // Getters et Setters
    public int getIdCollecte() { return idCollecte; }
    public void setIdCollecte(int idCollecte) { this.idCollecte = idCollecte; }

    public LocalDateTime getDateCollecte() { return dateCollecte; }
    public void setDateCollecte(LocalDateTime dateCollecte) { this.dateCollecte = dateCollecte; }

    public double getQuantiteCollectee() { return quantiteCollectee; }
    public void setQuantiteCollectee(double quantiteCollectee) { this.quantiteCollectee = quantiteCollectee; }

    public int getIdIntervention() { return idIntervention; }
    public void setIdIntervention(int idIntervention) { this.idIntervention = idIntervention; }

    public int getIdAgentCollecteur() { return idAgentCollecteur; }
    public void setIdAgentCollecteur(int idAgentCollecteur) { this.idAgentCollecteur = idAgentCollecteur; }

    // Ajoutez ces méthodes
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}