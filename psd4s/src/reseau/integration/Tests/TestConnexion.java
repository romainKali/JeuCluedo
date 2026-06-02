package reseau.integration.Tests;

import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le traitement de la commande protocolaire {@code @CONNEXION}.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lors de la connexion d'un
 * nouveau joueur au serveur Cluedo.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>la connexion correcte de plusieurs joueurs ;</li>
 *     <li>l'attribution des personnages sélectionnés ;</li>
 *     <li>la diffusion des informations de connexion aux autres joueurs ;</li>
 *     <li>l'envoi des informations concernant les joueurs déjà présents ;</li>
 *     <li>la gestion des personnages inexistants ;</li>
 *     <li>la détection des pseudos déjà utilisés ;</li>
 *     <li>la détection des personnages déjà utilisés ;</li>
 *     <li>l'interdiction de rejoindre une partie déjà commencée.</li>
 * </ul>
 *
 * Chaque test vérifie à la fois les messages envoyés par le serveur
 * et les modifications d'état réalisées dans le {@link metier.Jeu.Superviseur}.
 */
public class TestConnexion {
    Serveur serveur;
    ConnexionUtilisateur Alice;
    ConnexionUtilisateur Bob;
    ConnexionUtilisateur Charles;
    FakeThreadConnexion fakeThreadAlice;
    FakeThreadConnexion fakeThreadBob;
    FakeThreadConnexion fakeThreadCharles;

    /**
     * Initialise un environnement de test composé :
     * <ul>
     *     <li>d'un serveur de test ;</li>
     *     <li>de trois connexions simulées (Alice, Bob et Charles) ;</li>
     *     <li>de trois faux threads permettant de capturer les messages
     *     envoyés par le serveur ;</li>
     *     <li>d'un superviseur réinitialisé avant chaque test.</li>
     * </ul>
     *
     * Les joueurs ne sont pas encore connectés au protocole Cluedo.
     */
    @BeforeEach
    public void setUp(){
        serveur = new Serveur();
        serveur.getSuperviseur().resetSuperviseur();

        Alice = new ConnexionUtilisateur(serveur);
        Bob =  new ConnexionUtilisateur(serveur);
        Charles = new ConnexionUtilisateur(serveur);

        fakeThreadAlice = (FakeThreadConnexion) Alice.getThreadConnexion();
        fakeThreadBob = (FakeThreadConnexion) Bob.getThreadConnexion();
        fakeThreadCharles = (FakeThreadConnexion) Charles.getThreadConnexion();
    }

    @Test
    public void testProtocoleConnexion(){
        assertEquals(EPersonnage.Mademoiselle_Rose, serveur.stringToCarte("P0"));
        assertEquals(EPersonnage.Colonel_Moutarde, serveur.stringToCarte("P1"));
        assertEquals(EPersonnage.Madame_Leblanc,  serveur.stringToCarte("P2"));


        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");


        assertEquals("@CONNEXION_REUSSIE", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getFirst());
        assertEquals("@INFOS_AUTRESJOUEURS", fakeThreadAlice.getMessagesEnvoyesParLeServeur().get(1));
        assertEquals("@CONNEXION Bob P1", fakeThreadAlice.getMessagesEnvoyesParLeServeur().get(2));
        assertEquals("@CONNEXION Charles P2", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());

        assertEquals("@CONNEXION_REUSSIE", fakeThreadBob.getMessagesEnvoyesParLeServeur().getFirst());
        assertEquals("@INFOS_AUTRESJOUEURS Alice P0", fakeThreadBob.getMessagesEnvoyesParLeServeur().get(1));
        assertEquals("@CONNEXION Charles P2", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());

        assertEquals("@CONNEXION_REUSSIE", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getFirst());
        assertEquals("@INFOS_AUTRESJOUEURS Alice P0 Bob P1", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getListeJoueurs().get(0));
        assertEquals(Bob.getJoueur(), serveur.getSuperviseur().getListeJoueurs().get(1));
        assertEquals(Charles.getJoueur(), serveur.getSuperviseur().getListeJoueurs().get(2));
    }

    @Test
    public void testProtocoleConnexionIncorrectRespectantLeRegex(){
        Alice.traiterMessage("@CONNEXION Alice P9");

        assertEquals("@ERREUR_PARAM personnage que vous avez envoyé est incorrect", fakeThreadAlice.messagesEnvoyesParLeServeur.getFirst());
    }

    @Test
    public void testProtocoleConnexionAvecDesElementsDejaUtilises(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        assertEquals(1, serveur.getSuperviseur().getListeJoueurs().size());


        Bob.traiterMessage("@CONNEXION Alice P1");
        assertEquals("@ERREUR Le pseudo que vous avez choisi est déjà utilisé", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());

        Bob.traiterMessage("@CONNEXION Bob P0");
        assertEquals("@ERREUR Le personnage que vous avez choisi est déjà utilisé", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());

        Bob.traiterMessage("@CONNEXION Alice P0");
        assertEquals("@ERREUR Votre pseudo et votre personnage sont déjà utilisés", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());


        assertEquals(1, serveur.getSuperviseur().getListeJoueurs().size());

        Bob.traiterMessage("@CONNEXION Bob P1");
        fakeThreadBob.afficherTousLesMessagesEnvoyesParLeServeur();
    }

    @Test
    public void testProtocoleConnexionAlorsQueLaPartieADejaCommencee(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");

        Alice.traiterMessage("@DEMARRER");

        assertEquals(3, serveur.size());
        assertEquals(3, serveur.getSuperviseur().getListeJoueurs().size());


        ConnexionUtilisateur Dylan = new ConnexionUtilisateur(serveur);
        FakeThreadConnexion fakeThreadDylan = (FakeThreadConnexion) Dylan.getThreadConnexion();


        Dylan.traiterMessage("@CONNEXION Dylan P3");
        assertEquals("@ERREUR Vous ne pouvez pas rejoindre la partie car celle-ci a déjà commencée", fakeThreadDylan.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(3, serveur.getSuperviseur().getListeJoueurs().size());
        assertEquals(3, serveur.size());
    }
}