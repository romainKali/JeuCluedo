package reseau.integration.Tests;

import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le traitement de la commande protocolaire {@code @DEMARRER}.
 *
 * Cette suite de tests d'intégration valide le démarrage d'une partie
 * de Cluedo depuis le serveur.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>le démarrage correct d'une partie avec trois joueurs connectés ;</li>
 *     <li>l'envoi des cartes à chaque joueur ;</li>
 *     <li>la diffusion de l'état initial de la partie ;</li>
 *     <li>le placement initial des joueurs sur le plateau ;</li>
 *     <li>la désignation du joueur courant ;</li>
 *     <li>le refus de démarrage lorsqu'il n'y a pas assez de joueurs ;</li>
 *     <li>le refus de redémarrer une partie déjà commencée.</li>
 * </ul>
 */
public class TestDemarrer {
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
     *     <li>de trois connexions simulées représentant Alice, Bob et Charles ;</li>
     *     <li>de trois faux threads permettant de capturer les messages envoyés par le serveur ;</li>
     *     <li>d'un superviseur réinitialisé avant chaque scénario.</li>
     * </ul>
     *
     * Aucun joueur n'est connecté au protocole au début du test.
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
    public void testProtocoleDemarrer(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");

        Alice.traiterMessage("@DEMARRER");


        assertTrue(fakeThreadAlice.getMessagesEnvoyesParLeServeur().get(4).startsWith("@VOS_CARTES"));
        assertEquals("@PARTIE_COMMENCEE Alice Alice 0 16 Bob 5 0 Charles 7 23", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());

        assertTrue(fakeThreadBob.getMessagesEnvoyesParLeServeur().get(3).startsWith("@VOS_CARTES"));
        assertEquals("@PARTIE_COMMENCEE Alice Alice 0 16 Bob 5 0 Charles 7 23", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());

        assertTrue(fakeThreadCharles.getMessagesEnvoyesParLeServeur().get(2).startsWith("@VOS_CARTES"));
        assertEquals("@PARTIE_COMMENCEE Alice Alice 0 16 Bob 5 0 Charles 7 23", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        // Rq : Alice est écrit 2 fois car c'est elle le joueur courant
        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());

        assertEquals(6,Alice.getJoueur().getCartes().size());
        assertEquals(6, Bob.getJoueur().getCartes().size());
        assertEquals(6, Charles.getJoueur().getCartes().size());
    }

    @Test
    public void testProtocoleDemarrerAlorsQuIlNYaPasAssezDeJoueurs(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        Alice.traiterMessage("@DEMARRER");
        assertEquals("@ERREUR Il faut au moins 3 joueurs pour démarrer la partie.", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleDemarrerAlorsQueLaPartieADejaCommencee(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");
        Alice.traiterMessage("@DEMARRER");

        Alice.traiterMessage("@DEMARRER");

        assertEquals("@ERREUR La partie est déjà démarrée. Le redémarrage est impossible.", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

}
