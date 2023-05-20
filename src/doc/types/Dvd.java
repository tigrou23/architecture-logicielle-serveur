package doc.types;

import doc.Abonne;
import doc.Doc;

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

    /**
     * @return true si le document est un DVD pour adulte, false sinon
     */
    public boolean pourAdulte() {
        return adulte;
    }
}
