package com.example.gestiondechets;

public class Utilisateur {

    private int id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String role;
    private String password;
    private boolean estactive;
    private String creationDate;

    // ---- Constructor ----
    public Utilisateur(int id, String nom, String email,
                       String telephone, boolean estactive,String role
                       ) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = "adresse";
        this.role = role;
        this.password = "password";
        this.estactive = estactive;
    }
    public Utilisateur(int id, String nom, String email,String adresse,
                       String telephone, boolean estactive,String role,String password
    ) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.role = role;
        this.password = password;
        this.estactive = estactive;
    }


    // ---- Empty constructor (optional but useful) ----
    public Utilisateur(int id_utilisateur,
                       String nom,
                       String telephone,
                       String adresse,
                       String role,
                       boolean estactive,
                       String date_creation) {

        this.id = id_utilisateur;
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
        this.role = role;
        this.estactive = estactive;
        this.creationDate = date_creation;
    }
    // ---- Getters ----
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getAdresse() { return adresse; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    public String getCreationDate(){
        return creationDate;
    }
    public boolean isEstactive() { return estactive; }

    // ---- Setters ----
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setRole(String role) { this.role = role; }
    public void setPassword(String password) { this.password = password; }
    public void setEstactive(boolean estactive) { this.estactive = estactive; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                ", role='" + role + '\'' +
                ", estactive=" + estactive +
                '}';
    }
}
