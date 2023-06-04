package services;

import appli.Connect;
import codage.Codage;
import doc.Abonne;
import doc.Document;
import doc.types.Dvd;
import serveur.Service;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;

public class ServiceReservation extends Service {
    private InputStream in;
    private OutputStream out;
    public ServiceReservation(Socket socket) {
        super(socket);
    }
    private String decode() throws IOException {
        int det = in.read();
        if(det == 0){
            byte[] tableauBytes = new byte[1024];
            int taille = in.read(tableauBytes);
            in.read(new byte[1024]);
            return Codage.decode(tableauBytes,taille);
        }
        else return null;
    }
    @Override
    public void run() {
        try {
            in = getClient().getInputStream();
            out = getClient().getOutputStream();

            String bienvenue = "*** Bienvenue dans le client de réservation de document ***\n";
            bienvenue += Connect.catalogue();
            bienvenue += "\nEntrez votre numéro d'abonné :\n    -> ";
            out.write(Codage.encode(bienvenue,0));
            out.flush();
            int det = in.read();
            int noClient = -1;
            if(det==0){
                byte[] tableau = new byte[1024];
                int taille = in.read(tableau);
                noClient = Integer.parseInt(Codage.decode(tableau,taille));
            }
            String reponse = null;
            if (Connect.getListeAbonne().containsKey(noClient)){
                if(!Connect.estBanni(noClient)){
                    Abonne client = Connect.getListeAbonne().get(noClient);
                    out.write(Codage.encode("Bonjour " + client.nom() + "\nQuel est le document que vous voulez réserver?\n    -> ",0));
                    out.flush();
                    int noDocument = -1;
                    int det1 = in.read();
                    if(det1==0){
                        byte[] tableau1 = new byte[1024];
                        int taille1 = in.read(tableau1);
                        noDocument = Integer.parseInt(Codage.decode(tableau1,taille1));
                    }
                    if (Connect.getListeDocument().containsKey(noDocument)){
                        Document document = Connect.getListeDocument().get(noDocument);
                        Class<? extends Document> type = document.getClass();
                        if (Connect.getListeDocument().get(noDocument).empruntePar() != null){
                            out.write(Codage.encode("Ce document est déjà emprunté, voulez-vous avoir une alerte par mail quand il sera rendu ? (y/n)\n    -> ",0));
                            out.flush();
                            String reponseAlerte="";
                            int det2 = in.read();
                            if(det2==0){
                                byte[] tableau2 = new byte[1024];
                                int taille2 = in.read(tableau2);
                                reponseAlerte = Codage.decode(tableau2,taille2);
                            }
                            if(reponseAlerte.equals("y")){
                                Connect.preReserver(client, document);
                                reponse = "Alerte programmée";
                            }else{
                                reponse = "OK, n'hésitez pas à recommencer si vous changez d'avis";
                            }
                        }
                        else if (Connect.getListeDocument().get(noDocument).reservePar() != null){
                            long attente = Connect.heureFinReservation(Connect.getListeDocument().get(noDocument));
                            SimpleDateFormat formatHeure = new SimpleDateFormat("HH:mm"); // Format d'affichage de l'heure
                            reponse = "Ce document est réservé jusqu'à " + formatHeure.format(attente);
                            LocalTime heureActuelle = LocalTime.now();
                            long heureEnLong = heureActuelle.toNanoOfDay();
                            long difference = heureEnLong - attente;
                            if(difference<Duration.ofSeconds(30).toNanos()){
                                reponse +=". Accepteriez-vous de vous octroyer un moment d'attente agrémenté de mélodies harmonieuses ? (y/n)";
                                out.write(Codage.encode(reponse,0));
                                out.flush();
                                String reponseAttente="";
                                int det3 = in.read();
                                if(det3==0){
                                    byte[] tableau3 = new byte[1024];
                                    int taille3 = in.read(tableau3);
                                    reponseAttente = Codage.decode(tableau3,taille3);
                                }
                                if(reponseAttente.equals("y")){
                                    String filePath = "src/music/MusiqueCeleste.wav";
                                    byte[] musicData = Codage.encode(filePath, (int) attente);
                                    out.write(musicData);
                                    out.close();
                                    //out.flush();
                                    String reponseFinMusique = "";
                                    int det4 = in.read();
                                    if(det4==0){
                                        byte[] tableau4 = new byte[1024];
                                        int taille4 = in.read(tableau4);
                                        reponseFinMusique = Codage.decode(tableau4,taille4);
                                    }
                                    if(reponseFinMusique.equals("fin")){
                                        Connect.verifierExpirationReservation();
                                        if(Connect.getListeDocument().get(noDocument).reservePar() != null){
                                            reponse = "Malheureusement, le document a été récemment emprunté, privant ainsi votre opportunité de le réserver. Cependant, vous avez récemment eu l'occasion de jouir d'un concert divin sans aucun frais. Lors de votre prochaine tentative, veillez à honorer le grand chaman en offrant une contribution plus substantielle.";
                                        }
                                        else{
                                            reponse = "Votre précieux document a été consciencieusement réservé avec succès. Il vous est aimablement octroyé un délai de deux heures pour venir emprunter cette pièce dans nos locaux.";
                                        }
                                    }
                                }
                                else{
                                    reponse = "OK, n'hésitez pas à recommencer si vous changez d'avis";
                                }
                            }
                        }
                        else{
                            if(type == Dvd.class){
                                if(((Dvd) document).pourAdulte() && !client.estAdulte()){
                                    reponse = "Vous ne semblez pas avoir l'âge requis pour ce DVD...";
                                }else{
                                    document.reservation(client);
                                    reponse = "Document réservé";
                                }
                            }
                        }
                    }
                    else{
                        reponse = "Document non existant";
                    }
                }
                else {
                    reponse = "Vous êtes banni, vous ne pouvez pas réserver de document jusqu'au " + Connect.getDateFinBan(noClient) + "";
                }
            }
            else{
                reponse = "Client non existant";
            }
            out.write(Codage.encode("fin - " + reponse,0));
            out.flush();
            out.close();
            in.close();
            getClient().close();
        } catch (IOException e) {
            // Fin du service d'inversion
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            getClient().close();
        } catch (IOException e2) {
        }
    }
}
