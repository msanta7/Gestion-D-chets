package model;

public class Signalements {
    // Classe modèle pour les signalements
    public static class Signalement {
        private final int id;
        private final String date;
        private final String citoyen;
        private final String typeDechet;
        private final String localisation;
        private final String urgence;
        private String etat;
        private final String telephone;
        private final String description;

        public Signalement(int id, String date, String citoyen, String typeDechet,
                           String localisation, String urgence, String etat,
                           String telephone, String description) {
            this.id = id;
            this.date = date;
            this.citoyen = citoyen;
            this.typeDechet = typeDechet;
            this.localisation = localisation;
            this.urgence = urgence;
            this.etat = etat;
            this.telephone = telephone;
            this.description = description;
        }

        public int getId() { return id; }
        public String getDate() { return date; }
        public String getCitoyen() { return citoyen; }
        public String getTypeDechet() { return typeDechet; }
        public String getLocalisation() { return localisation; }
        public String getUrgence() { return urgence; }
        public String getEtat() { return etat; }
        public String getTelephone() { return telephone; }
        public String getDescription() { return description; }

        public void setEtat(String etat) { this.etat = etat; }
    }
}
