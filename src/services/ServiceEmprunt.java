package services;

import appli.Connect;
import codage.Codage;
import doc.Abonne;
import doc.Document;
import doc.types.Dvd;
import serveur.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceEmprunt extends Service {
    public ServiceEmprunt(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getClient().getInputStream()));
            PrintWriter out = new PrintWriter(getClient().getOutputStream(), true);
            out.println(Codage.encode("""
                    *** Bienvenue dans le client d'emprunt de document ***
                    Entrez votre numéro client :
                        ->\s"""));
            int noClient = Integer.parseInt(Codage.decode(in.readLine()));
            String reponse = null;
            if (Connect.getListeAbonne().containsKey(noClient)){
                if(!Connect.estBanni(noClient)){
                    Abonne client = Connect.getListeAbonne().get(noClient);
                    out.println(Codage.encode("Bonjour " + client.nom() + "\nQuel est le document que vous voulez emprunter?\n    -> "));
                    int noDocument = Integer.parseInt(Codage.decode(in.readLine()));
                    if (Connect.getListeDocument().containsKey(noDocument)){
                        Document document = Connect.getListeDocument().get(noDocument);
                        Class<? extends Document> type = document.getClass();
                        if (Connect.getListeDocument().get(noDocument).empruntePar() != null){
                            reponse = "Document déjà emprunté";
                        }
                        else{
                            if(Connect.getListeDocument().get(noDocument).reservePar() != null){
                                if(Connect.getListeDocument().get(noDocument).reservePar().numero() != client.numero()){
                                    reponse = Connect.heureFinReservation(Connect.getListeDocument().get(noDocument));
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
        } catch (IOException e2) {
        }
    }
}
