package appli;

import doc.Abonne;
import doc.Document;
import doc.types.Dvd;

import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Connect {
    private final static Map<Integer,Document> listeDocument;
    private final static Map<Integer,Abonne> listeAbonne;
    private final String CONFIG_PATH = "src/ressources/config.properties";
    private static Connection conn;

    static{
        listeDocument = new HashMap<>();
        listeAbonne = new HashMap<>();
    }
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
                    System.out.println("reservation " + dvd);
                }
                else if(empruntePar != 0){
                    dvd = new Dvd(dvd_res.getInt(1), dvd_res.getString(2), listeAbonne.get(empruntePar),dvd_res.getBoolean(3));
                    System.out.println("emprunt " + dvd);
                }else{
                    dvd = new Dvd(dvd_res.getInt(1), dvd_res.getString(2), dvd_res.getBoolean(3));
                }
                listeDocument.put(dvd.numero(),dvd);
            }

            dvd_res.close();
            stmt.close();

            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean reservation(Document doc, Abonne ab) throws SQLException {
        Statement stmt = conn.createStatement();
        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean bool = stmt.execute("SELECT reserver(" + doc.numero() + ", " + ab.numero() + ", '" + formater.format(aujourdhui) +"') from DUAL;");
        stmt.close();
        return bool;
    }

    public static boolean emprunt(Document doc, Abonne ab) throws SQLException {
        Statement stmt = conn.createStatement();
        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean bool = stmt.execute("SELECT emprunter(" + doc.numero() + ", " + ab.numero() + ", '" + formater.format(aujourdhui) +"') from DUAL;");
        stmt.close();
        return bool;
    }

    public static boolean retour(Document doc) throws SQLException {
        Statement stmt = conn.createStatement();
        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean bool = stmt.execute("SELECT rendre(" + doc.numero() + ", '" + formater.format(aujourdhui) +"') from DUAL;");
        stmt.close();
        return bool;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e) {
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
