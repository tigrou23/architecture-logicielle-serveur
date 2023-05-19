package doc;

import appli.Connect;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Doc implements Document{
    private final String titre;
    private final Integer numero;
    private Abonne reservePar;
    private Abonne empruntePar;
    private Date dateReservation;
    private final int minuteVerif = 5;
    private final int heureMax = 2;

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
            //TODO: travailler ici
            if(Connect.reservation(this, ab)){
                empruntePar = null;
                reservePar = ab;
                Timer timer = new Timer();
                dateReservation = new Date();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        verifierExpirationReservation();
                    }
                }, 0, minuteVerif * 60 * 1000);
            }else{
                System.err.println("Pb avec la base de données lors du retour.");
            }
        }catch (SQLException e){
            System.err.println("Pb avec la base de données lors du retour : " +  e);
        }
    }

    public void verifierExpirationReservation() {
        if (reservePar != null && dateReservation != null) {
            Date maintenant = new Date();
            long differenceEnMillisecondes = maintenant.getTime() - dateReservation.getTime();
            long differenceEnHeures = differenceEnMillisecondes / (60 * 60 * 1000);
            if (differenceEnHeures >= heureMax) {
                try{
                    if(Connect.annulerReservation(this)){
                        empruntePar = null;
                        reservePar = null;
                        System.out.println("La réservation du document : " + this + " a été annulée car le délai de récupération a été dépassé.");
                    }else{
                        System.err.println("Pb avec la base de données lors du retour.");
                    }
                }catch (SQLException e){
                    System.err.println("Pb avec la base de données lors du retour : " +  e);
                }
            }
        }
    }

    public String tempsRestantReservation() {
        if (reservePar != null && dateReservation != null) {
            Date maintenant = new Date();
            long differenceEnMillisecondes = dateReservation.getTime() + heureMax * 60 * 60 * 1000 - maintenant.getTime();
            long differenceEnMinutes = differenceEnMillisecondes / (60 * 1000);

            long heures = differenceEnMinutes / 60;
            long minutes = differenceEnMinutes % 60;

            return String.format("%d heures et %d minutes", heures, minutes);
        } else {
            return "Aucune réservation en cours";
        }
    }

    public String heureFinReservation() {
        if (reservePar != null && dateReservation != null) {
            long heureFinMillis = dateReservation.getTime() + 2 * 60 * 60 * 1000; // Calculer l'heure de fin de réservation (2 heures plus tard)
            SimpleDateFormat formatHeure = new SimpleDateFormat("HH:mm"); // Format d'affichage de l'heure

            return "Ce document est réservé jusqu'à " + formatHeure.format(heureFinMillis);
        } else {
            return "Ce document n'est pas réservé";
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
        return  getClass().getName() + "{" +
                "titre='" + titre + '\'' +
                '}';
    }
}
