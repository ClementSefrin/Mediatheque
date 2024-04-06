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

    public boolean bannir() {
        return estBanni = true;
    }

    public LocalDateTime putDateBannissement() {
        return dateBannissement = LocalDateTime.now().plusDays(30);
    }

    public String getDateBannissement() {
        if (dateBannissement == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return dateBannissement.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonne abonne = (Abonne) o;
        return numero == abonne.numero && Objects.equals(nom, abonne.nom) && Objects.equals(dateNaissance, abonne.dateNaissance);
    }
}
