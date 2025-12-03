package com.example.gestiondechets;

import java.time.LocalDateTime;

public class Intervention {
    private int idIntervention;
    private int idSignalement;
    private LocalDateTime datePlanification;
    private LocalDateTime dateRealisation;
    private String statut; // planifiee, en_cours, terminee, annulee
    private String notes;
    private int idConducteur;

    // Champs supplémentaires pour affichage (non dans la table)
    private String adresseSignalement;
    private String descriptionSignalement;
    private String etatSignalement;

    // Constructeurs
    public Intervention() {}

    public Intervention(int idSignalement, LocalDateTime datePlanification, String statut, String notes) {
        this.idSignalement = idSignalement;
        this.datePlanification = datePlanification;
        this.statut = statut;
        this.notes = notes;
    }

    // Getters et Setters
    public int getIdIntervention() { return idIntervention; }
    public void setIdIntervention(int idIntervention) { this.idIntervention = idIntervention; }

    public int getIdSignalement() { return idSignalement; }
    public void setIdSignalement(int idSignalement) { this.idSignalement = idSignalement; }

    public LocalDateTime getDatePlanification() { return datePlanification; }
    public void setDatePlanification(LocalDateTime datePlanification) { this.datePlanification = datePlanification; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getIdConducteur() { return idConducteur; }
    public void setIdConducteur(int idConducteur) { this.idConducteur = idConducteur; }

    public String getAdresseSignalement() { return adresseSignalement; }
    public void setAdresseSignalement(String adresseSignalement) { this.adresseSignalement = adresseSignalement; }

    public String getDescriptionSignalement() { return descriptionSignalement; }
    public void setDescriptionSignalement(String descriptionSignalement) { this.descriptionSignalement = descriptionSignalement; }

    public String getEtatSignalement() { return etatSignalement; }
    public void setEtatSignalement(String etatSignalement) { this.etatSignalement = etatSignalement; }

    // Méthodes utilitaires
    public String getDatePlanificationFormatted() {
        if (datePlanification != null) {
            return datePlanification.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    public String getDateRealisationFormatted() {
        if (dateRealisation != null) {
            return dateRealisation.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    public String getStatutFormatted() {
        if (statut == null) return "";
        switch (statut) {
            case "planifiee": return "Planifiée";
            case "en_cours": return "En cours";
            case "terminee": return "Terminée";
            case "annulee": return "Annulée";
            default: return statut;
        }
    }

    @Override
    public String toString() {
        return "Intervention #" + idIntervention + " - " + getStatutFormatted();
    }
}