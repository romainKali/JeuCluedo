package reseau;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * La classe ThreadAcceptConnexion est complète. C'est un Thread contenant la boucle infinie qui
 * écoute si une connexion arrive. Et lorsqu'elle arrive, le message "Connexion!" est affiché dans le
 * terminal du serveur. Mais surtout, un objet ConnexionUtilisateur est créé. Cet objet représente
 * le client distant. L'objet est créé et aussi ajouté à la liste des utuilisateurs connectés. Cette
 * liste est gérée par le serveur lui-même, c'est-à-dire la classe métier. Attention, ici, l'utilisateur
 * distant vient à peine de se connecter. Donc il n'a pas eu le temps de s'identifier. Il faudra
 * d'abord qu'il envoie un message @CONNEXION contenant son pseudo. C'est dans l'objet ConnexionUtilisateur
 * que cela se passera.
 */
public class ThreadAcceptConnexion extends Thread{
    private Serveur serveur;
    private ServerSocket serverSocket;

    public ThreadAcceptConnexion(Serveur serveur) {
        this.serveur = serveur;
        try {
            this.serverSocket = new ServerSocket(serveur.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        start();
    }

    @Override
    public void run() {
        try {
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connexion !");
                ConnexionUtilisateur connexion = new ConnexionUtilisateur(socket, serveur);
                serveur.add(connexion);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
