package appli;

import doc.Abonne;
import doc.Document;
import doc.Dvd;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class Connect {
    private final static ArrayList<Document> listeDocument;
    private final static ArrayList<Abonne> listeAbonne;
    private final String CONFIG_PATH = "src/ressources/config.properties";
    private Connection conn;

    static{
        listeDocument = new ArrayList<>();
        listeAbonne = new ArrayList<>();
    }
    public Connect(){

        try{
            Properties properties = new Properties();
            FileInputStream inputStream = new FileInputStream(CONFIG_PATH);
            properties.load(inputStream);

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("server.address") + ":" + properties.getProperty("server.port") + "/" + properties.getProperty("server.database"), properties.getProperty("server.username"), properties.getProperty("server.password"));
            Statement stmt = conn.createStatement();
            ResultSet dvd_res = stmt.executeQuery("SELECT * FROM dvd inner join document d on dvd.ID_dvd = d.ID_document");

            Document dvd = null;

            while (dvd_res.next()) {
                dvd = new Dvd(dvd_res.getString(4), dvd_res.getBoolean(2));
                listeDocument.add(dvd);
            }

            dvd_res.close();
            stmt.close();

            stmt = conn.createStatement();
            ResultSet abonne_res = stmt.executeQuery("SELECT * FROM abonne");

            Abonne abonne = null;

            while (abonne_res.next()) {
                abonne = new Abonne(abonne_res.getInt(1), abonne_res.getString(2), abonne_res.getDate(3));
                listeAbonne.add(abonne);
            }

            //à retirer
            for (Document d : listeDocument) {
                System.out.println(d);
            }

            //à retirer
            for (Abonne a : listeAbonne) {
                System.out.println(a);
            }

            abonne_res.close();
            stmt.close();
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
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

    public static ArrayList<Document> getListeDocument() {
        return listeDocument;
    }

    public static ArrayList<Abonne> getListeAbonne() {
        return listeAbonne;
    }

}
