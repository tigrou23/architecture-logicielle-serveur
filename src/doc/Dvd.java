package doc;

import appli.Connect;

import java.sql.Date;
import java.sql.SQLException;


public class Dvd implements Document{
    private final String titre;
    private final Integer numero;
    private final boolean adulte;
    private Abonne reservePar;
    private Abonne empruntePar;

    public Dvd(Integer numero, String titre, boolean adulte, Abonne empruntePar) {
        this.numero = numero;
        this.titre = titre;
        this.adulte = adulte;
        this.reservePar = null;
        this.empruntePar = empruntePar;
    }

    public Dvd(Integer numero, String titre, Abonne reservePar, boolean adulte) {
        this.numero = numero;
        this.titre = titre;
        this.adulte = adulte;
        this.reservePar = reservePar;
        this.empruntePar = null;
    }

    public Dvd(Integer numero, String titre, boolean adulte) {
        this.numero = numero;
        this.titre = titre;
        this.adulte = adulte;
        this.reservePar = null;
        this.empruntePar = null;
    }

    @Override
    public int numero() {
        return numero;
    }

    @Override
    public Abonne empruntePar() {
        return empruntePar;
    }

    @Override
    public Abonne reservePar() {
        return reservePar;
    }

    @Override
    public void reservation(Abonne ab) {
        //TODO: ajouter les preconditions
        reservePar = ab;
        Connect.reservation(this, ab);
    }

    @Override
    public void emprunt(Abonne ab) {
        //TODO: ajouter les preconditions
        empruntePar = ab;
        reservePar = null;
        Connect.emprunt(this, ab);
    }

    @Override
    public void retour() {
        //TODO: ajouter les preconditions
        try{
            if(Connect.retour(this)){
                empruntePar = null;
                reservePar = null;
            }else{
                System.err.println("Pb avec la base de données lors du retour.");
            }
        }catch (SQLException e){
            System.err.println("Pb avec la base de données lors du retour : " +  e);
        }
    }

    public String toString() {
        return "Dvd{" +
                "titre='" + titre + '\'' +
                ", adulte=" + adulte +
                '}';
    }
}
