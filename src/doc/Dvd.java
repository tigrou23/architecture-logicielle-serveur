package doc;

public class Dvd implements Document{
    private final String titre;

    private final boolean adulte;

    public Dvd(String titre, boolean adulte) {
        this.titre = titre;
        this.adulte = adulte;
    }

    @Override
    public int numero() {
        return 0;
    }

    @Override
    public Abonne empruntePar() {
        return null;
    }

    @Override
    public Abonne reservePar() {
        return null;
    }

    @Override
    public void reservation(Abonne ab) {

    }

    @Override
    public void emprunt(Abonne ab) {

    }

    @Override
    public void retour() {

    }

    public String toString() {
        return "Dvd{" +
                "titre='" + titre + '\'' +
                ", adulte=" + adulte +
                '}';
    }
}
