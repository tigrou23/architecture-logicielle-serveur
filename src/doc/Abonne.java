package doc;

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

    public String toString() {
        return "Abonne{" +
                "numero=" + numero +
                ", nom='" + nom + '\'' +
                ", dateNaissance=" + dateNaissance +
                '}';
    }
}
