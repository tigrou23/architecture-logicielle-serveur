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

public class ServiceReservation extends Service {
    public ServiceReservation(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getClient().getInputStream()));
            PrintWriter out = new PrintWriter(getClient().getOutputStream(), true);
            String bienvenue = "*** Bienvenue dans le client de réservation de document ***\n";
            bienvenue += Connect.catalogue();
            bienvenue += "\nEntrez votre numéro d'abonné : ";
            out.println(Codage.encode(bienvenue));
            int noClient = Integer.parseInt(Codage.decode(in.readLine()));
            String reponse = null;
            if (Connect.getListeAbonne().containsKey(noClient)){
                Abonne client = Connect.getListeAbonne().get(noClient);
                out.println(Codage.encode("Bonjour " + client.nom() + "\nQuel est le document que vous voulez réserver?\n    -> "));
                int noDocument = Integer.parseInt(Codage.decode(in.readLine()));
                if (Connect.getListeDocument().containsKey(noDocument)){
                    Document document = Connect.getListeDocument().get(noDocument);
                    Class<? extends Document> type = document.getClass();
                    if (Connect.getListeDocument().get(noDocument).empruntePar() != null){
                        out.println(Codage.encode("Ce document est déjà emprunté, voulez-vous avoir une alerte par mail quand il sera rendu ? (y/n)\n    -> "));
                        String reponseAlerte = Codage.decode(in.readLine());
                        if(reponseAlerte.equals("y")){
                            Connect.preReserver(client, document);
                            reponse = "Alerte programmée";
                        }else{
                            reponse = "OK, n'hésitez pas à recommencer si vous changez d'avis";
                        }
                    }
                    else if (Connect.getListeDocument().get(noDocument).reservePar() != null){
                        reponse = Connect.heureFinReservation(Connect.getListeDocument().get(noDocument));
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
