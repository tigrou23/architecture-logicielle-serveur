package services;

import appli.Connect;
import codage.Codage;
import serveur.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceRetour extends Service {

    public ServiceRetour(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getClient().getInputStream()));
            PrintWriter out = new PrintWriter(getClient().getOutputStream(), true);

            out.println(Codage.encode("*** Bienvenue dans le client de retour de document ***\n" + "Entrez le numéro du document à retourner : "));
            int noDocument = Integer.parseInt(Codage.decode(in.readLine()));

            String reponse = "Document non existant";

            if(Connect.getListeDocument().containsKey(noDocument)){
                if(Connect.getListeDocument().get(noDocument).empruntePar()==null){
                    reponse = "Document non emprunté";
                }else{
                    Connect.getListeDocument().get(noDocument).retour();
                    reponse = "Document retourné";
                }
            }

            out.println("fin - " + reponse);
            out.close();
            in.close();
            getClient().close();        } catch (IOException e) {
            // Fin du service d'inversion
        }
        try {
            getClient().close();
        } catch (IOException e2) {
        }
    }
}
