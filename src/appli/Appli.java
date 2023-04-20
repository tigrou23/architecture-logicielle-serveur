package appli;

import java.io.FileInputStream;
import java.io.IOException;

import serveur.Serveur;
import services.ServiceEmprunt;
import services.ServiceReservation;
import services.ServiceRetour;

import java.sql.*;
import java.util.Properties;

class Appli {
    private final static int PORT_RESERVATION = 1000;
    private final static int PORT_EMPRUNT = 1001;
    private final static int PORT_RETOUR = 1002;
    private final static String CONFIG_PATH = "src/ressources/config.properties";

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(CONFIG_PATH);
        properties.load(inputStream);
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

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("server.address") + ":" + properties.getProperty("server.port") + "/" + properties.getProperty("server.database"), properties.getProperty("server.username"), properties.getProperty("server.password"));
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery("SELECT * FROM pilote");
        while (res.next())
            System.out.println(res.getString(1));
        inputStream.close();

    }
}