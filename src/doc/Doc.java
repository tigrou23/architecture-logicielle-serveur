package doc;

import appli.Connect;

import java.io.IOException;
import java.sql.SQLException;

public abstract class Doc implements Document{
    private final String titre;
    private final Integer numero;
    private Abonne reservePar;
    private Abonne empruntePar;

    public Doc(Integer numero, String titre) {
        this.numero = numero;
        this.titre = titre;
        this.reservePar = null;
        this.empruntePar = null;
    }

    public Doc(Integer numero, Abonne emprunt, String titre) {
        this.numero = numero;
        this.titre = titre;
        this.reservePar = null;
        this.empruntePar = emprunt;
    }

    public Doc(Integer numero, String titre, Abonne reservation) {
        this.numero = numero;
        this.titre = titre;
        this.reservePar = reservation;
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
        try{
            if(Connect.reservation(this, ab)){
                empruntePar = null;
                reservePar = ab;
            }else{
                System.err.println("Pb avec la base de données lors du retour.");
            }
        }catch (SQLException e){
            System.err.println("Pb avec la base de données lors du retour : " +  e);
        }
    }

    @Override
    public void emprunt(Abonne ab) {
        try{
            if(Connect.emprunt(this, ab)){
                empruntePar = ab;
                reservePar = null;
            }else{
                System.err.println("Pb avec la base de données lors du retour.");
            }
        }catch (SQLException e){
            System.err.println("Pb avec la base de données lors du retour : " +  e);
        }
    }

    @Override
    public void retour() {
        try{
            Connect.retour(this);
            empruntePar = null;
            reservePar = null;
        }catch (SQLException e){
            System.err.println("Pb avec la base de données lors du retour : " +  e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return titre;
    }

}
