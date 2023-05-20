package doc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Date;

public class Abonne {

    private final int numero;
    private final String nom;
    private final Date dateNaissance;

    public Abonne(int numero, String nom, Date dateNaissance) {
        this.numero = numero;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
    }

    /**
     * @return true si l'abonné est majeur, false sinon
     */
    public boolean estAdulte() {
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(this.dateNaissance.getTimezoneOffset() * -60);
        Instant instant = Instant.ofEpochMilli(this.dateNaissance.getTime());
        LocalDate dateNaissance = instant.atOffset(zoneOffset).toLocalDate();
        LocalDate aujourdHui = LocalDate.now();
        Period age = Period.between(dateNaissance, aujourdHui);
        return age.getYears() >= 18;
    }

    public int numero() {
        return numero;
    }

    public String nom() {
        return nom;
    }

    public String toString() {
        return "Abonne{" +
                "numero=" + numero +
                ", nom='" + nom + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", majeur=" + estAdulte() +
                '}';
    }
}
