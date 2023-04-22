package appli;

import java.io.IOException;
import serveur.Serveur;
import services.ServiceEmprunt;
import services.ServiceReservation;
import services.ServiceRetour;
import java.sql.*;

class Appli {
    private final static int PORT_RESERVATION = 1000;
    private final static int PORT_EMPRUNT = 1001;
    private final static int PORT_RETOUR = 1002;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        try {
            new Thread(new Serveur(ServiceReservation.class, PORT_RESERVATION)).start();
            System.out.println("Service réservation lancé sur le port " + PORT_RESERVATION);

            new Thread(new Serveur(ServiceEmprunt.class, PORT_EMPRUNT)).start();
            System.out.println("Service emprunt lancé sur le port " + PORT_EMPRUNT);

            new Thread(new Serveur(ServiceRetour.class, PORT_RETOUR)).start();
            System.out.println("Service retour lancé sur le port " + PORT_RETOUR);

        } catch (IOException e) {
            System.err.println("Pb lors de la création du serveur : " +  e);
        }
        Connect connect = new Connect();
        connect.closeConnection();
    }
}