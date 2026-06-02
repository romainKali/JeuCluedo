package reseau.integration.Tests;

import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Vérifie le traitement de la commande protocolaire {@code @FIN_TOUR}.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lorsqu'un joueur termine son tour.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>le passage correct au joueur suivant ;</li>
 *     <li>la diffusion du nouveau joueur courant à tous les clients ;</li>
 *     <li>le refus de fin de tour par un joueur non courant ;</li>
 *     <li>le refus de fin de tour avant le démarrage de la partie ;</li>
 *     <li>l'impossibilité pour l'ancien joueur courant d'agir après la fin de son tour.</li>
 * </ul>
 */
public class TestMettreFinAuTour {
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
     * Chaque test démarre avec une partie non commencée mais avec trois
     * joueurs déjà enregistrés auprès du serveur.
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
    void testProtocoleMettreFinAuTour(){
        Alice.traiterMessage("@DEMARRER");

        assertEquals(serveur.getSuperviseur().getJoueurCourant(), Alice.getJoueur());
        Alice.traiterMessage("@FIN_TOUR");
        assertEquals(serveur.getSuperviseur().getJoueurCourant(), Bob.getJoueur());
        assertEquals("@FIN_TOUR Bob", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Bob", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Bob", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    void testProtocoleMettreFinAuTourJoueurNonCourant(){
        Alice.traiterMessage("@DEMARRER");
        Bob.traiterMessage("@FIN_TOUR");
        assertEquals("@ERREUR Seul le joueur courant est autorisé à mettre fin au tour", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(serveur.getSuperviseur().getJoueurCourant(), Alice.getJoueur());
    }

    @Test
    void testProtocoleFinTourAvantDemarragePartie(){
        Alice.traiterMessage("@FIN_TOUR");
        assertEquals("@ERREUR La partie n'a pas commencé", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    void testProtocoleFinTourActionsImpossiblePourAncienJoueurCourant(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@FIN_TOUR");
        Alice.traiterMessage("@LANCER_DES");
        assertEquals("@ERREUR Seul le joueur courant peut lancer les dés", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }
}
