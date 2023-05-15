package doc.types;

import appli.Connect;
import doc.Abonne;
import doc.Doc;

import java.sql.Date;
import java.sql.SQLException;


public class Dvd extends Doc {

    private final boolean adulte;

    public Dvd(Integer numero, String titre, boolean adulte, Abonne abonne) {
        super(numero, titre, abonne);
        this.adulte = adulte;
    }

    public Dvd(Integer numero, String titre, Abonne abonne, boolean adulte) {
        super(numero, abonne, titre);
        this.adulte = adulte;
    }

    public Dvd(Integer numero, String titre, boolean adulte) {
        super(numero, titre);
        this.adulte = adulte;
    }

    public boolean pourAdulte() {
        return adulte;
    }
}
