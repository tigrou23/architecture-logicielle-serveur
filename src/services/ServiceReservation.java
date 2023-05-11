package services;

import appli.Connect;
import codage.Codage;
import doc.Abonne;
import doc.Document;
import doc.Dvd;
import serveur.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceReservation extends Service {
    public ServiceReservation(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        //TODO: IMPLEMENTER LE SERVICE RESERVATION
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getClient().getInputStream()));
            PrintWriter out = new PrintWriter(getClient().getOutputStream(), true);
            out.println(Codage.encode("*** Bienvenue dans le client de réservation de document ***\n" + "Entrez votre numéro client : "));
            int noClient = Integer.parseInt(Codage.decode(in.readLine()));
            String reponse = null;
            if (Connect.getListeAbonne().containsKey(noClient)){
                Abonne client = Connect.getListeAbonne().get(noClient);
                out.println(Codage.encode("Bonjour " + client.nom() + "\nQuel est le document que vous voulez réserver?\n    -> "));
                int noDocument = Integer.parseInt(Codage.decode(in.readLine()));
                if (Connect.getListeDocument().containsKey(noDocument)){
                    Document document = Connect.getListeDocument().get(noDocument);
                    //TODO: un peu bizarre de faire extends alors qu'on parle d'une interface
                    Class<? extends Document> type = document.getClass();
                    if (Connect.getListeDocument().get(noDocument).empruntePar() != null){
                        reponse = "Document déjà emprunté";
                    }
                    else if (Connect.getListeDocument().get(noDocument).reservePar() != null){
                        reponse = "Document déjà réservé";
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
