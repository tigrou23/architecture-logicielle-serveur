package services;

import appli.Connect;
import codage.Codage;
import doc.Abonne;
import doc.Document;
import doc.types.Dvd;
import serveur.Service;

import java.io.*;
import java.net.Socket;

public class ServiceEmprunt extends Service {
    public ServiceEmprunt(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            InputStream in = getClient().getInputStream();
            PrintWriter out = new PrintWriter(getClient().getOutputStream(), true);
            out.println(Codage.encode("""
                    *** Bienvenue dans le client d'emprunt de document ***
                    Entrez votre numéro client :
                        ->\s""",0));
            byte[] tableau = new byte[1024];
            int taille = in.read(tableau);
            int noClient = Integer.parseInt(Codage.decode(tableau,taille));
            String reponse = null;
            if (Connect.getListeAbonne().containsKey(noClient)){
                if(!Connect.estBanni(noClient)){
                    Abonne client = Connect.getListeAbonne().get(noClient);
                    out.println(Codage.encode("Bonjour " + client.nom() + "\nQuel est le document que vous voulez emprunter?\n    -> ",0));
                    byte[] tableau1 = new byte[1024];
                    int taille1 = in.read(tableau1);
                    int noDocument = Integer.parseInt(Codage.decode(tableau,taille1));
                    if (Connect.getListeDocument().containsKey(noDocument)){
                        Document document = Connect.getListeDocument().get(noDocument);
                        Class<? extends Document> type = document.getClass();
                        if (Connect.getListeDocument().get(noDocument).empruntePar() != null){
                            reponse = "Document déjà emprunté";
                        }
                        else{
                            if(Connect.getListeDocument().get(noDocument).reservePar() != null){
                                if(Connect.getListeDocument().get(noDocument).reservePar().numero() != client.numero()){
                                    long attente = Connect.heureFinReservation(Connect.getListeDocument().get(noDocument));
                                    reponse = "Ce document est réservé jusqu'à " + attente;
                                }else{
                                    if(type == Dvd.class){
                                        if(((Dvd) document).pourAdulte() && !client.estAdulte()){
                                            reponse = "Vous ne semblez pas avoir l'âge requis pour ce DVD...";
                                        }else{
                                            document.emprunt(client);
                                            reponse = "Document emprunté";
                                        }
                                    }
                                }
                            }else{
                                if(type == Dvd.class){
                                    if(((Dvd) document).pourAdulte() && !client.estAdulte()){
                                        reponse = "Vous ne semblez pas avoir l'âge requis pour ce DVD...";
                                    }else{
                                        document.emprunt(client);
                                        reponse = "Document emprunté";
                                    }
                                }
                            }
                        }
                    }
                    else{
                        reponse = "Document non existant";
                    }
                }else{
                    reponse = "Vous êtes banni, vous ne pouvez pas emprunter de document jusqu'au " + Connect.getDateFinBan(noClient) + "";
                }
            }
            else{
                reponse = "Client non existant";
            }
            out.println("fin - " + reponse);
            out.close();
            in.close();
            getClient().close();
        } catch (IOException e) {
            // Fin du service d'inversion
        }
        try {
            getClient().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
