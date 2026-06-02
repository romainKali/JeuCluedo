package reseau.integration.Tests;

import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Vérifie le traitement de la commande protocolaire {@code @ALLER_VERS}.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lors des déplacements des joueurs
 * sur le plateau de Cluedo.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>le déplacement correct d'un joueur vers une case voisine ;</li>
 *     <li>la mise à jour de la position du joueur ;</li>
 *     <li>la diffusion des déplacements à tous les clients ;</li>
 *     <li>le refus des déplacements vers une case non voisine ;</li>
 *     <li>le refus des déplacements effectués par un joueur non courant ;</li>
 *     <li>le refus des déplacements avant le lancer des dés ;</li>
 *     <li>le refus des déplacements lorsque le joueur n'a plus de déplacements disponibles ;</li>
 *     <li>le refus des déplacements avant le démarrage de la partie ;</li>
 *     <li>le refus des déplacements vers une case occupée ;</li>
 *     <li>le refus des déplacements vers une case inexistante du plateau.</li>
 * </ul>
 *
 * Chaque test vérifie à la fois les messages envoyés par le serveur
 * et les modifications d'état réalisées dans le {@link metier.Jeu.Superviseur}.
 */
public class TestDeplacer {
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
     *     <li>de trois joueurs connectés au protocole Cluedo.</li>
     * </ul>
     *
     * Chaque scénario démarre avec un état de partie identique.
     */
    @BeforeEach
    void setUp()
    {
        serveur = new Serveur();
        serveur.getSuperviseur().resetSuperviseur();

        Alice = new ConnexionUtilisateur(serveur);
        Bob = new ConnexionUtilisateur(serveur);
        Charles = new ConnexionUtilisateur(serveur);

        fakeThreadAlice = (FakeThreadConnexion)Alice.getThreadConnexion();
        fakeThreadBob = (FakeThreadConnexion)Bob.getThreadConnexion();
        fakeThreadCharles = (FakeThreadConnexion)Charles.getThreadConnexion();

        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");
    }

    @Test
    void TestProtocoleDeplacer(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");

        assertEquals(serveur.getSuperviseur().getPlateau().getCase(0,16), Alice.getJoueur().getPosition());
        Alice.traiterMessage("@ALLER_VERS 1 16");
        assertEquals("@DEPLACEMENT_REUSSI Alice 1 16",  fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@DEPLACEMENT_REUSSI Alice 1 16",  fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@DEPLACEMENT_REUSSI Alice 1 16",  fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(serveur.getSuperviseur().getPlateau().getCase(1,16), Alice.getJoueur().getPosition());
    }

    @Test
    void testProtocoleDeplacerAvecCaseNonVoisine(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 2 16");
        assertEquals("@ERREUR Le joueur ne peut pas se déplacer vers une case qui ne lui est pas voisine", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(serveur.getSuperviseur().getPlateau().getCase(0,16), Alice.getJoueur().getPosition());
    }

    @Test
    void testProtocoleDeplacerAvecJoueurNonCourant(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Bob.traiterMessage("@ALLER_VERS 5 1");
        assertEquals("@ERREUR Seul le joueur courant est en mesure de pouvoir se déplacer", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(Bob.getJoueur().getPosition(), serveur.getSuperviseur().getPlateau().getCase(5, 0));
    }

    @Test
    void testProtocoleDeplacerSansAvoirLanceLesDes(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@ALLER_VERS 1 16");
        assertEquals("@ERREUR Le joueur ne peut pas se déplacer puisque vous n'avez pas lancé les dés", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    void testProtocoleDeplacerLeJoueurCourantNePeutPlusSeDeplacer(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        serveur.getSuperviseur().setNbDeplacementsJoueurCourant(1);
        Alice.traiterMessage("@ALLER_VERS 1 16");
        Alice.traiterMessage("@ALLER_VERS 2 16");

        assertEquals("@ERREUR Le joueur n'a plus assez de déplacements", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(serveur.getSuperviseur().getPlateau().getCase(1,16), Alice.getJoueur().getPosition());
    }

    @Test
    void testProtocoleDeplacerLesDesSansAvoirDemarreLaPartie(){
        Alice.traiterMessage("@ALLER_VERS 1 16");
        assertEquals("@ERREUR Un joueur ne peut être déplacé si la partie n'a pas commencé", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    void testProtocoleDeplacerVersUneCaseOccupee(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");

        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(5,1));
        Alice.traiterMessage("@ALLER_VERS 5 0");
        assertEquals("@ERREUR Le joueur n'a pas le droit de se déplacer vers une case déjà occupée par un autre joueur", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(serveur.getSuperviseur().getPlateau().getCase(5,1), Alice.getJoueur().getPosition());
    }

    @Test
    void testProtocoleDeplacerVersUneCaseNonExistant(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 32 153");
        assertEquals("@ERREUR Coordonnées en dehors du plateau...", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(serveur.getSuperviseur().getPlateau().getCase(0,16), Alice.getJoueur().getPosition());
    }
}
