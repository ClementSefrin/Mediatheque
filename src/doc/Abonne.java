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
    private LocalDateTime dateBannissement = null;
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
        LocalDate aujourdhui = LocalDate.now();
        LocalDate dateNaissance = this.dateNaissance.toLocalDate();
        return aujourdhui.minusYears(AGE_MAJEUR).isAfter(dateNaissance);
    }

    public void debannir() {
        estBanni = false;
        dateBannissement = null;
        System.out.println("L'abonne a ete debanni.");
    }

    public boolean estBanni() {
        if (dateBannissement == null) return false;
        if (LocalDateTime.now().isAfter(dateBannissement)) debannir();
        return estBanni ;
    }

    public void bannir() {
        estBanni = true;
        // pour tester un bannissement de 1 minute
        //dateBannissement = LocalDateTime.now().plusMinutes(0);
        dateBannissement = LocalDateTime.now().plusDays(DUREE_BAN);
         System.out.println("Vous avez ete banni pour 30 jours " + getDateBannissement());
    }

    public String getDateBannissement() {
        System.out.println(dateBannissement);
        if (dateBannissement == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return dateBannissement.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonne abonne = (Abonne) o;
        return numero == abonne.numero && Objects.equals(nom, abonne.nom)
                && Objects.equals(dateNaissance, abonne.dateNaissance);
    }
}
