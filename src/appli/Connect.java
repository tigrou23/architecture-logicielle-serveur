package appli;

import doc.Abonne;
import doc.Document;
import doc.types.Dvd;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import java.util.Properties;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sound.sampled.*;


public class Connect {
    private final static Map<Integer,Document> listeDocument;
    private final static int heureMax = 2;
    private final static int minuteVerif = 5;
    private final static Map<Integer,Abonne> listeAbonne;
    private final static Map<Document,Date> documentReserve;
    private final static Map<Document,ArrayList<Abonne>> documentPreReserve;
    private final static String CONFIG_PATH = "src/ressources/config.properties";
    private static Connection conn;

    static{
        listeDocument = new HashMap<>();
        listeAbonne = new HashMap<>();
        documentReserve = new HashMap<>();
        documentPreReserve = new HashMap<>();
    }

    //Attributs Musique
    private static Mixer mixer;
    private static Clip clip;

    /**
     * Constructeur de la classe Connect. Cette classe va permettre de se connecter à
     * la base de données et de récupérer les données nécessaires au fonctionnement
     * de l'application.

     * Un Timer permettra de vérifer toutes les 5 minutes si un document est
     * bien emprunté dans les 2 heures qui suivent la réservation.
     */

    public Connect(){

        try{
            Properties properties = new Properties();
            FileInputStream inputStream = new FileInputStream(CONFIG_PATH);
            properties.load(inputStream);

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("server.address") + ":" + properties.getProperty("server.port") + "/" + properties.getProperty("server.database"), properties.getProperty("server.username"), properties.getProperty("server.password"));

            Statement stmt = conn.createStatement();
            ResultSet abonne_res = stmt.executeQuery("SELECT * FROM abonne");

            Abonne abonne = null;

            while (abonne_res.next()) {
                abonne = new Abonne(abonne_res.getInt(1), abonne_res.getString(2), abonne_res.getDate(3));
                listeAbonne.put(abonne.numero(),abonne);
            }

            abonne_res.close();
            stmt.close();

            stmt = conn.createStatement();
            ResultSet dvd_res = stmt.executeQuery("select * from vue_dvd;");

            Document dvd = null;
            Integer reservePar = null;
            Integer empruntePar = null;

            while (dvd_res.next()) {
                reservePar = dvd_res.getInt(6);
                empruntePar = dvd_res.getInt(4);
                if(reservePar != 0){
                    dvd = new Dvd(dvd_res.getInt(1), dvd_res.getString(2), dvd_res.getBoolean(3), listeAbonne.get(reservePar));
                }
                else if(empruntePar != 0){
                    dvd = new Dvd(dvd_res.getInt(1), dvd_res.getString(2), listeAbonne.get(empruntePar),dvd_res.getBoolean(3));
                }else{
                    dvd = new Dvd(dvd_res.getInt(1), dvd_res.getString(2), dvd_res.getBoolean(3));
                }
                listeDocument.put(dvd.numero(),dvd);
            }

            dvd_res.close();
            stmt.close();

            Timer timer_verifierExpirationReservation = new Timer();
            timer_verifierExpirationReservation.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        verifierExpirationReservation();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, minuteVerif * 60 * 1000);

            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //Chargement de la musique
        Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();

        mixer = null;
        for (Mixer.Info info : mixInfos) {
            if (info.getDescription().contains("Direct Audio Device")) {
                mixer = AudioSystem.getMixer(info);
                break;
            }
        }

        if (mixer == null) {
            System.err.println("Aucun mixeur compatible trouvé.");
            return;
        }

        DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
        try{ clip = (Clip) mixer.getLine(dataInfo);}
        catch (LineUnavailableException e) {e.printStackTrace();}


        try{
            URL soundURL = Appli.class.getResource("/music/MusiqueCeleste.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Méthode permettant d'effectuer une réservation sur un document directement en BBD
     * @param doc Document à réserver
     * @param ab Abonné qui réserve le document
     * @return true si la réservation a été effectuée, false sinon
     * @throws SQLException Exception SQL
     */
    public static boolean reservation(Document doc, Abonne ab) throws SQLException {
        Statement stmt = conn.createStatement();
        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean bool = stmt.execute("SELECT reserver(" + doc.numero() + ", " + ab.numero() + ", '" + formater.format(aujourdhui) +"') from DUAL;");
        stmt.close();
        synchronized (documentReserve){
            documentReserve.put(doc, new Date());
        }
        return bool;
    }

    /**
     * Méthode permettant d'effectuer un emprunt sur un document directement en BBD
     * @param doc Document à réserver
     * @param ab Abonné qui réserve le document
     * @return true si l'emprunt a été effectué, false sinon
     * @throws SQLException Exception SQL
     */
    public static boolean emprunt(Document doc, Abonne ab) throws SQLException {
        synchronized (documentReserve){
            if(documentReserve.containsKey(doc)){
                documentReserve.remove(doc);
            }
        }
        Statement stmt = conn.createStatement();
        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean bool = stmt.execute("SELECT emprunter(" + doc.numero() + ", " + ab.numero() + ", '" + formater.format(aujourdhui) +"') from DUAL;");
        stmt.close();
        return bool;
    }

    /**
     * Méthode permettant d'effectuer un retour sur un document directement en BBD
     * @param doc Document à réserver
     * @return true si le retour a été effectué, false sinon
     * @throws SQLException Exception SQL
     */
    public static boolean retour(Document doc) throws SQLException, IOException {
        synchronized (documentReserve){
            if(documentReserve.containsKey(doc)){
                documentReserve.remove(doc);
            }
        }
        Statement stmt = conn.createStatement();
        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean bool = stmt.execute("SELECT rendre(" + doc.numero() + ", '" + formater.format(aujourdhui) +"') from DUAL;");
        stmt.close();
        if(documentPreReserve.containsKey(doc)){
            send(doc);
        }
        return bool;
    }

    /**
     * Méthode permettant de fermer la connexion à la base de données
     */
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode permettant de vérifier si une réservation a expiré
     * Si c'est le cas, on annule la réservation et on remet le document à disposition
     * @throws SQLException Exception SQL
     */
    private void verifierExpirationReservation() throws SQLException {
        ArrayList<Document> documentsARetourner = new ArrayList<>();
        synchronized (documentReserve){
            for (Map.Entry<Document, Date> entry : documentReserve.entrySet()) {
                Document doc = entry.getKey();
                Date dateReservation = entry.getValue();
                Date maintenant = new Date();
                long differenceEnMillisecondes = maintenant.getTime() - dateReservation.getTime();
                long differenceEnHeures = differenceEnMillisecondes / (60 * 60 * 1000);
                if (differenceEnHeures >= heureMax) {
                    synchronized (documentsARetourner){
                        documentsARetourner.add(doc);
                    }
                }
            }
        }
        synchronized (documentsARetourner){
            for (Document doc : documentsARetourner) {
                if (annulerReservation(doc)) {
                    doc.retour();
                    System.out.println("La réservation du document : " + doc + " a été annulée car le délai de récupération a été dépassé.");
                } else {
                    System.err.println("Pb avec la base de données lors du retour.");
                }
            }
        }
    }

    /**
     * Méthode permettant d'annuler une réservation directement en BBD
     * @param doc Document à annuler
     * @return true si l'annulation a été effectuée, false sinon
     * @throws SQLException Exception SQL
     */
    private boolean annulerReservation(Document doc) throws SQLException {
        Statement stmt = conn.createStatement();
        boolean bool = stmt.execute("SELECT annulerReservation(" + doc.numero() + ") from DUAL;");
        stmt.close();
        return bool;
    }

    /**
     * Méthode permettant de récupérer l'heure de fin de réservation d'un document
     * @param doc Document dont on veut récupérer l'heure de fin de réservation
     * @return L'heure de fin de réservation
     */
    public static String heureFinReservation(Document doc) {
        long heureFinMillis = documentReserve.get(doc).getTime() + 2 * 60 * 60 * 1000; // Calculer l'heure de fin de réservation (2 heures plus tard)
        SimpleDateFormat formatHeure = new SimpleDateFormat("HH:mm"); // Format d'affichage de l'heure
        musiqueAttente();
        return "Ce document est réservé jusqu'à " + formatHeure.format(heureFinMillis);
    }

    public static void musiqueAttente(){
        clip.start();
        do{
            try{
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }while(clip.isActive());
    }

    /**
     * Méthode permettant de récupérer le catalogue des documents
     * @return le catalogue
     */
    public static String catalogue(){
        StringBuilder sb = new StringBuilder();
        sb.append("\nVoici les documents de la médiathèque :\n");
        for (Document doc : listeDocument.values()){
            sb.append(doc.numero()).append(" - ").append(doc).append("\n");
        }
        return sb.toString();
    }

    /**
     * Ajouter à une hashmap les personnes qui attendent....
     * @param abo Abonné intéressé par l'alerte
     * @param doc Document qui intéresse l'abonné
     */
    public static void preReserver(Abonne abo, Document doc){
        if(!documentPreReserve.containsKey(doc)){
            ArrayList<Abonne> abonnes = new ArrayList<>();
            abonnes.add(abo);
            documentPreReserve.put(doc, abonnes);
        }else{
            documentPreReserve.get(doc).add(abo);
        }
    }

    /**
     * Envoie un e-mail en utilisant les paramètres SMTP spécifiés.
     */
    public static void send(Document doc) throws IOException {

        ArrayList<Abonne> listeAbo = documentPreReserve.get(doc);

        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(CONFIG_PATH);
        properties.load(inputStream);

        Properties props = new Properties();
        props.put("mail.smtp.host", properties.getProperty("smtp.host"));
        props.put("mail.smtp.port", properties.getProperty("smtp.port"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("smtp.userName"), properties.getProperty("smtp.password"));
            }
        });

        try {
            for (Abonne abo : listeAbo){
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(properties.getProperty("smtp.from")));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("hugo0807pereira@gmail.com"));
                message.setSubject("Alerte - Médiathèque");
                message.setText("Bonjour " + abo.nom() + ",\n\n"
                        + "Le document " + doc + " est disponible.\n\n"
                        + "Cordialement,\n"
                        + "L'équipe de la médiathèque.");
                Transport.send(message);
                System.out.println("Le message a été envoyé avec succès.");
            }
            documentPreReserve.remove(doc);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer,Document> getListeDocument() {
        return listeDocument;
    }

    public static Map<Integer,Abonne> getListeAbonne() {
        return listeAbonne;
    }

}
