package doc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Abonne {
    private final int numero;
    private final String nom;
    private final Date dateNaissance;
    private static final int AGE_MAJEUR = 16;
    private LocalDateTime dateBanissement = null;
    private boolean estBanni = false;
    private final static int DUREE_BAN = 30;

    public Abonne(int numero, String nom, Date dateNaissance ) {
        this.numero = numero;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
    }

    public int getNumero() {
        return this.numero;
    }

    public String getNom() {
        return this.nom;
    }

    public boolean estMajeur() {
        LocalDate aujourdHui = LocalDate.now();
        LocalDate dateNaiss = dateNaissance.toLocalDate();
        return aujourdHui.minusYears(AGE_MAJEUR).isAfter(dateNaiss);
    }

    public boolean estBanni() {
        if(estBanni == false) {
            return false;
        }
        else{
            if(LocalDate.now().isAfter(dateBanissement.toLocalDate())){
                estBanni = false;
            }
            return estBanni;
        }
    }

    public void bannir() {
        estBanni = true;
         dateBanissement = LocalDateTime.now().plusDays(DUREE_BAN);
         System.out.println("Vous avez été banni pour 30 jours " + getDateBanissement());
    }

    public String getDateBanissement() {
        System.out.println(dateBanissement);
        if (dateBanissement == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return dateBanissement.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonne abonne = (Abonne) o;
        return numero == abonne.numero && Objects.equals(nom, abonne.nom) && Objects.equals(dateNaissance, abonne.dateNaissance);
    }
}
