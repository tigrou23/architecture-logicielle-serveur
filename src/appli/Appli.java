package appli;

import serveur.Serveur;
import services.ServiceEmprunt;
import services.ServiceReservation;
import services.ServiceRetour;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class Appli {
    private static final String CONFIG_PATH = "src/ressources/config.properties";

    /**
     * Méthode du serveur qui lance les services
     * @param args non utilisé
     * @throws IOException si les services ne peuvent pas être lancés
     */
    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(CONFIG_PATH);
        properties.load(inputStream);
        int PORT_RESERVATION = Integer.parseInt(properties.getProperty("service.reservation"));
        int PORT_EMPRUNT = Integer.parseInt(properties.getProperty("service.emprunt"));
        int PORT_RETOUR = Integer.parseInt(properties.getProperty("service.retour"));

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
    }
}