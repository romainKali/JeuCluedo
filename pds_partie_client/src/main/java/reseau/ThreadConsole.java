package reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ihm.Config;
import ihm.InterfaceMain;

/**
 * Thread dédié à la gestion de la communication réseau côté client.
 * <p>
 * Cette classe maintient la connexion TCP (Socket) avec le serveur de jeu.
 * Elle écoute en continu les messages entrants (dans son propre thread pour ne pas bloquer
 * l'interface graphique JavaFX) et fournit des méthodes pour envoyer des messages au serveur.
 * </p>
 */
public class ThreadConsole extends Thread {

    /** Référence à l'interface principale du client pour déléguer le traitement des messages. */
    private InterfaceMain client;

    /** Socket de connexion TCP avec le serveur. */
    private Socket socket;
    /** Flux de lecture pour recevoir les messages du serveur. */
    private BufferedReader in;
    /** Flux d'écriture pour envoyer des messages au serveur. */
    private PrintWriter out;

    /**
     * Construit le thread de communication et établit la connexion réseau.
     * Initialise la socket cliente à l'aide de l'adresse et du port définis dans {@link InterfaceMain},
     * puis prépare les flux d'entrée/sortie.
     *
     * @param client L'instance principale de l'application cliente.
     * @throws RuntimeException Si la connexion au serveur échoue ou si les flux ne peuvent être créés.
     */
    public ThreadConsole(InterfaceMain client) {
        this.client = client;

        try {
            this.socket = new Socket(InterfaceMain.SERVEUR,InterfaceMain.PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transmet un message brut reçu du serveur à l'interface principale
     * pour qu'il soit analysé et traité par la chaîne d'experts.
     *
     * @param s La ligne de texte (message protocolaire) reçue du serveur.
     */
    public void traiterMessage(String s) {
        client.traiterMessage(s);
    }

    /**
     * Boucle principale du thread.
     * Écoute en permanence les messages en provenance du serveur tant que la connexion est active.
     * Lorsque le serveur se déconnecte (message null), la boucle s'arrête et ferme proprement les ressources.
     */
    @Override
    public void run() {
        while (true) {
            String message = recevoirMessage();
            if (message != null) {
                traiterMessage(message);
            }
            else {
                break;
            }
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lit une ligne de texte (un message) depuis le flux d'entrée réseau.
     * Cette méthode est bloquante jusqu'à la réception d'une ligne complète.
     *
     * @return Le message reçu sous forme de chaîne de caractères, ou null en cas de déconnexion/erreur.
     */
    public String recevoirMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Envoie un message textuel au serveur via le flux de sortie réseau.
     * Si l'option de débogage réseau est activée dans {@link Config}, le message est
     * également imprimé dans la console système standard.
     *
     * @param s Le message protocolaire à envoyer au serveur.
     */
    public void envoyerMessage(String s) {
        if (Config.debug_reseau)
            System.out.println(s);

        this.out.println(s);
    }
}
