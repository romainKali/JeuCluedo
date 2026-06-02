package reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Représente un thread chargé de gérer les communications réseau
 * associées à un utilisateur connecté.
 *
 * Cette classe assure la lecture des messages provenant d'une
 * {@link Socket} ainsi que l'envoi des réponses vers le client.
 *
 * Elle ne réalise aucune interprétation du protocole de communication.
 * Les messages reçus sont simplement transmis à l'objet
 * {@link ConnexionUtilisateur}, qui se charge de leur validation
 * et de leur traitement.
 *
 * Chaque instance de cette classe est associée à une unique connexion
 * utilisateur et s'exécute dans son propre thread afin de permettre
 * plusieurs communications simultanées avec le serveur.
 */
public class ThreadConnexion extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ConnexionUtilisateur connexion;
    private boolean fin = false;

    /**
     * Crée un thread de communication associé à une connexion utilisateur.
     *
     * Les flux d'entrée et de sortie de la socket sont initialisés puis
     * le thread est automatiquement démarré.
     *
     * @param connexion connexion utilisateur associée
     * @param socket socket utilisée pour communiquer avec le client distant
     */
    public ThreadConnexion(ConnexionUtilisateur connexion, Socket socket) {
        this.connexion = connexion;
        this.socket = socket;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        start();

    }

    /**
     * Constructeur utilisé exclusivement par les tests d'intégration.
     *
     * Aucun flux réseau n'est créé. Ce constructeur permet notamment
     * à {@link reseau.integration.FakeThreadConnexion} de simuler le
     * comportement d'un client sans ouvrir de véritable socket.
     *
     * @param connexion connexion utilisateur associée
     */
    public ThreadConnexion(ConnexionUtilisateur connexion) {
        this.connexion = connexion;
        this.socket = null;
        this.in = null;
        this.out = null;
    }


    /**
     * Exécute la boucle principale de communication.
     *
     * Tant que le thread n'est pas arrêté, cette méthode attend la
     * réception d'un message sur la socket. Chaque message reçu est
     * transmis à la méthode
     * {@link ConnexionUtilisateur#traiterMessage(String)}.
     *
     * Si le client ferme sa connexion ou si une erreur d'entrée/sortie
     * survient, la connexion est considérée comme terminée et le serveur
     * est notifié via l'envoi d'un message null.
     */
    @Override
    public void run() {
        while (!fin) {
            String message;
            try {
                message = this.in.readLine();
                if (message != null) {
                    System.out.println("LE SERVEUR A RECU : [" + message + "]");
                    this.connexion.traiterMessage(message);
                } else  {
                	this.connexion.traiterMessage(null);
                    fin = true;
                }
            } catch (IOException e) {
            	this.connexion.traiterMessage(null);
                fin = true;
            }
        }
    }

    /**
     * Envoie un message au client distant.
     *
     * Si un flux de sortie est disponible, le message est transmis
     * immédiatement à travers la socket.
     *
     * @param message message à envoyer au client
     */
    public void envoyerMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Demande l'arrêt du thread de communication.
     *
     * La boucle principale sera interrompue lors de sa prochaine
     * itération.
     */
    public void fin() {
        fin = true;
    }

    /**
     * Indique si le thread a été marqué comme terminé.
     *
     * @return true si le thread est arrêté, false sinon
     */
    public boolean estFini(){
        return fin;
    }
}
