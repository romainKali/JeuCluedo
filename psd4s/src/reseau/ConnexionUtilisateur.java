package reseau;

import java.net.Socket;
import java.util.Objects;
import java.util.regex.Pattern;

import exception.ExpertException;
import metier.Jeu.Joueur;
import reseau.experts.Expert;
import reseau.experts.ExpertAccuser;
import reseau.experts.ExpertConnexion;
import reseau.experts.ExpertDeconnexion;
import reseau.experts.ExpertDemarrer;
import reseau.experts.ExpertDeplacer;
import reseau.experts.ExpertFinTour;
import reseau.experts.ExpertLancerDes;
import reseau.experts.ExpertMessagePrivate;
import reseau.experts.ExpertMessagePublic;
import reseau.experts.ExpertRefuterSoupcon;
import reseau.experts.ExpertSoupconner;
import reseau.integration.FakeThreadConnexion;

/**
 * Représente la connexion entre un utilisateur distant et le serveur.
 *
 * Cette classe joue le rôle d'intermédiaire entre la couche réseau
 * ({@link ThreadConnexion}) et la couche métier ({@link Serveur}).
 * Elle est responsable de :
 * <ul>
 *     <li>la gestion du protocole de communication ;</li>
 *     <li>la validation des messages reçus ;</li>
 *     <li>la transmission des messages aux experts appropriés ;</li>
 *     <li>l'envoi des messages vers le client distant ;</li>
 *     <li>la conservation des informations associées à l'utilisateur
 *     connecté (pseudo, joueur, état de validité).</li>
 * </ul>
 *
 * Les commandes reçues sont analysées puis traitées par une chaîne
 * d'experts suivant le patron de conception
 * <em>Chain of Responsibility</em>.
 */
public class ConnexionUtilisateur {
    private ThreadConnexion threadConnexion = null;

    private String pseudo;
    private Serveur serveur;
    private Socket socket;
    private boolean valide = false;

    private Joueur joueur;

    private Expert chaineExperts;

    /**
     * Construit une nouvelle connexion utilisateur associée à une socket réseau.
     *
     * @param socket socket utilisée pour communiquer avec le client distant
     * @param serveur serveur auquel appartient cette connexion
     */
    public ConnexionUtilisateur(Socket socket, Serveur serveur) {
        // On ne fait pas de contrôle. C'est le serveur qui appelle ce constructeur et on fait l'hypothèse
        // qu'il n'est pas buggé...
        this.socket = socket;
        this.serveur = serveur;

        initExperts();

        this.threadConnexion = new ThreadConnexion(this, socket);
    }

    /**
     * Constructeur utilisé exclusivement pour les tests d'intégration.
     *
     * Une fausse connexion réseau est créée à l'aide d'un
     * {@link FakeThreadConnexion}.
     *
     * @param serveur serveur auquel appartient cette connexion
     */
    public ConnexionUtilisateur(Serveur serveur) {
        this.socket = null;
        this.serveur = serveur;

        initExperts();
        this.threadConnexion = new FakeThreadConnexion(this);
    }

    /**
     * Initialise la chaîne d'experts chargée d'interpréter les commandes
     * du protocole.
     *
     * Chaque expert est responsable d'un type de commande particulier.
     */
    public void initExperts() {
        Expert expAccuse = new ExpertAccuser();
        Expert expConnexion = new ExpertConnexion();
        Expert expDeconnexion = new ExpertDeconnexion();
        Expert expDemarrer = new ExpertDemarrer();
        Expert expDeplacer = new ExpertDeplacer();
        Expert expFinTour = new ExpertFinTour();
        Expert expLancerDes = new ExpertLancerDes();
        Expert expRefuterSoupcon = new ExpertRefuterSoupcon();
        Expert expSoupconner = new ExpertSoupconner();
        Expert expMessagePublic = new ExpertMessagePublic();
        Expert expMessagePrive = new ExpertMessagePrivate();

        expAccuse.setSuivant(expConnexion);
        expConnexion.setSuivant(expDeconnexion);
        expDeconnexion.setSuivant(expDemarrer);
        expDemarrer.setSuivant(expDeplacer);
        expDeplacer.setSuivant(expFinTour);
        expFinTour.setSuivant(expLancerDes);
        expLancerDes.setSuivant(expRefuterSoupcon);
        expRefuterSoupcon.setSuivant(expSoupconner);
        expSoupconner.setSuivant(expMessagePrive);
        expMessagePrive.setSuivant(expMessagePublic);

        chaineExperts = expAccuse;
    }

    /**
     * Traite un message reçu depuis le client.
     *
     * La méthode vérifie d'abord que le message respecte le protocole.
     * Si le message est valide, il est transmis à la chaîne d'experts
     * pour exécution.
     *
     * @param message message reçu du client
     */
    public void traiterMessage(String message) {
        if (message == null) {
            valide = false;
            try {
                serveur.remove(this);
            } catch (ServeurException e) {
                // Ignorer l'exception
            }
            return;
        }

        if (!verifieProtocole(message)) {
            this.threadConnexion.envoyerMessage("@ERROR ce que vous dites n'a aucun sens. Votre message est ignoré");
            return;
        }

        // --- LA LIGNE MANQUANTE EST ICI ---
        // On donne le message au premier expert pour qu'il déclenche l'action (comme la connexion)
        try {
        chaineExperts.traiter(message, this);
        }
        catch (ExpertException e) {
        	System.err.println("Erreur d'Expert : " + e.getMessage());
        	this.threadConnexion.envoyerMessage("@ERROR message ne respect aucun regex, veuillez retenter");
        }
    }

    /**
     * Envoie un message formaté au client distant.
     *
     * @param msg message à transmettre
     */
    public void envoyerMessageClient(String msg) {
        if (this.threadConnexion != null) {
            this.threadConnexion.envoyerMessage(msg);
        }
    }

    // ************* méthodes standard d'une classe Java **********************

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public boolean estValide() {
        return valide;
    }

    public void setEstValide(boolean valide) {
        this.valide = valide;
    }

    public Serveur getServeur() {
        return serveur;
    }

    public Socket getSocket() {
        return socket;
    }

    public Joueur getJoueur() { return joueur; }

    public void setJoueur(Joueur joueur) {this.joueur = joueur;}

    public ThreadConnexion getThreadConnexion() {
        return threadConnexion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnexionUtilisateur that = (ConnexionUtilisateur) o;
        return Objects.equals(pseudo, that.pseudo);
    }

    @Override
    public String toString() {
        return "ConnexionUtilisateur{" +
                "pseudo='" + pseudo + '\'' +
                '}';
    }

    // ********* gestion du protocole ***********

    // Les messages reçus cotés serveur
    public  final static String regexMP_TO = "^@MP_TO \\p{Alnum}+ .*$";
    public  final static String regexTO_ALL = "^@TO_ALL .*$";



    // Les Regex du Cluedo
    public final static String regexCONNEXION = "^@CONNEXION \\p{Alnum}+ P\\d+$";
    public final static String regexDECONNEXION = "^@DECONNEXION$";
    public final static String regexDEMARRER = "^@DEMARRER$";
    public  final static String regexLANCER_DES = "^@LANCER_DES$";
    public  final static String regexALLER_VERS = "^@ALLER_VERS \\d+ \\d+$";
    public  final static String regexSOUPCON = "^@SOUPCONNER P\\d+ A\\d+$";
    public  final static String regexACCUSE = "^@ACCUSER P\\d+ A\\d+ L\\d+$";
    public  final static String regexFIN_TOUR = "^@FIN_TOUR$";
    public  final static String regexINDICE = "^@INDICE ([PAL]\\d+|null)$";



    private final static String[] protocole = {regexCONNEXION, regexDECONNEXION, regexMP_TO, regexTO_ALL,
            regexDEMARRER, regexLANCER_DES, regexALLER_VERS, regexSOUPCON,
            regexACCUSE, regexFIN_TOUR, regexINDICE
    };


    /**
     * Vérifie qu'un message respecte au moins une règle du protocole.
     *
     * @param message message à vérifier
     * @return true si le message est valide selon le protocole
     */
    public boolean verifieProtocole(String message) {
        for (String phraseDuProtocole : protocole) {
            if (verifiePhraseDuProtocole(message, phraseDuProtocole))
                return true;
        }
        return false;
    }

    /**
     * Vérifie qu'un message correspond à une expression régulière donnée.
     *
     * @param message message à analyser
     * @param phrase expression régulière du protocole
     * @return true si le message correspond à l'expression
     */
    public boolean verifiePhraseDuProtocole(String message, String phrase) {
        Pattern pattern = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(message).matches();
    }

    /**
     * Envoie un message public à l'utilisateur courant.
     *
     * Le message est formaté conformément au protocole sous la forme :
     * @PUBLIC_FROM pseudo message
     *
     * @param emetteur utilisateur ayant envoyé le message
     * @param message contenu du message
     */
    public void envoyerMessagePublic(ConnexionUtilisateur emetteur, String message) {
        if (this.threadConnexion != null) {
            String messageFormate = "@PUBLIC_FROM " + emetteur.getPseudo() + " " + message;
            this.threadConnexion.envoyerMessage(messageFormate);
        }
    }

    /**
     * Envoie un message privé à l'utilisateur courant.
     *
     * Le message est formaté conformément au protocole sous la forme :
     * @MP_FROM pseudo message
     *
     * @param emetteur utilisateur ayant envoyé le message
     * @param message contenu du message
     */
    public void envoyerMessagePrive(ConnexionUtilisateur emetteur, String message) {
        if (this.threadConnexion != null) {
            String messageFormate = "@MP_FROM " + emetteur.getPseudo() + " " + message;
            this.threadConnexion.envoyerMessage(messageFormate);
        }
    }
}