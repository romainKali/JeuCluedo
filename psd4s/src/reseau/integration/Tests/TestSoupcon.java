package reseau.integration.Tests;

import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Vérifie le traitement de la commande protocolaire {@code @SOUPCONNER}.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lors de l'émission d'un soupçon
 * dans une partie de Cluedo.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>l'émission correcte d'un soupçon depuis une pièce ;</li>
 *     <li>la création du soupçon en cours dans le superviseur ;</li>
 *     <li>la désignation du premier joueur devant réfuter ;</li>
 *     <li>la diffusion du soupçon à l'ensemble des joueurs ;</li>
 *     <li>le blocage temporaire de la partie tant que le soupçon n'est pas résolu ;</li>
 *     <li>l'interdiction d'émettre un soupçon en dehors d'une pièce ;</li>
 *     <li>la gestion des paramètres invalides ;</li>
 *     <li>l'interdiction pour un joueur non courant d'émettre un soupçon ;</li>
 *     <li>l'interdiction d'émettre un soupçon avant le démarrage de la partie.</li>
 * </ul>
 *
 * Chaque test vérifie à la fois les messages échangés entre les clients
 * et les modifications d'état réalisées dans le {@link metier.Jeu.Superviseur}.
 */
public class TestSoupcon {
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
    public void testProtocoleSoupcon(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(4, 6));


        assertFalse(serveur.getSuperviseur().estDansUnePiece(Alice.getJoueur()));
        Alice.traiterMessage("@ALLER_VERS 3 6");
        assertTrue(serveur.getSuperviseur().estDansUnePiece(Alice.getJoueur()));


        assertEquals("P0", Serveur.carteToString(EPersonnage.Mademoiselle_Rose));
        assertEquals("A0", Serveur.carteToString(EArme.Poignard));
        assertEquals("L0", Serveur.carteToString(ELieu.Bureau));


        assertNull(serveur.getSuperviseur().getSoupconEnCours());


        Alice.traiterMessage("@SOUPCONNER P0 A0");


        assertEquals("@SOUPCON_EMIS Alice P0 A0 L0", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@SOUPCON_EMIS Alice P0 A0 L0", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());


        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());
        assertNotNull(serveur.getSuperviseur().getSoupconEnCours());

        Alice.traiterMessage("@FIN_TOUR");
        assertEquals("@ERREUR Le soupcon émis par le joueur courant n'a pas encore été réfuté !", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        Alice.traiterMessage("@LANCER_DES");
        assertEquals("@ERREUR Le soupcon émis par le joueur courant n'a pas encore été réfuté !", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        Alice.traiterMessage("@ALLER_VERS 2 6");
        assertEquals("@ERREUR Le soupcon émis par le joueur courant n'a pas encore été réfuté !", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        Alice.traiterMessage("@ACCUSER P0 A0 L0");
        assertEquals("@ERREUR Le soupcon émis par le joueur courant n'a pas encore été réfuté !", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleSoupconAvecJoueurCourantQuiNEstPasDansUnePiece(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(4, 6));


        assertFalse(serveur.getSuperviseur().estDansUnePiece(Alice.getJoueur()));
        Alice.traiterMessage("@SOUPCONNER P0 A0");
        assertEquals("@ERREUR Le joueur doit être dans une pièce pour émettre un soupcon", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertNull(serveur.getSuperviseur().getSoupconEnCours());
    }

    @Test
    public void testProtocoleSoupconAvecMauvaisParametre(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(4, 6));
        Alice.traiterMessage("@ALLER_VERS 3 6");
        Alice.traiterMessage("@SOUPCONNER P7 A9");
        assertEquals("@ERREUR_PARAM le ou les noms des éléments passés dans le soupcon sont incorrects", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertNull(serveur.getSuperviseur().getSoupconEnCours());
    }

    @Test
    public void testProtocoleSoupconAvecJoueurNonCourant(){
        Alice.traiterMessage("@DEMARRER");
        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());

        Bob.traiterMessage("@SOUPCONNER P0 A0");
        assertEquals("@ERREUR Seul le joueur courant est autorisé à réaliser un soupçon", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertNull(serveur.getSuperviseur().getSoupconEnCours());
    }

    @Test
    public void testProtocoleSoupconAlorsQueLaPartieNAPasDemarree(){
        Alice.traiterMessage("@SOUPCONNER P0 A0");
        assertEquals("@ERREUR La partie n'a pas commencé, il est donc impossible de lancer un soupçon", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertFalse(serveur.getSuperviseur().getPartieCommencee());
    }
}
/**
 * Coordonnées des portes de chacune des pièces :
 *
 * Bureau :
 * - Porte : (3,6)
 * - Entrée depuis : (4,6)
 *
 * Bibliothèque :
 * - Portes : (8,6) et (10,3)
 * - Entrées depuis : (8,7) et (11,3)
 *
 * Salle de billard :
 * - Portes : (12,2) et (15,5)
 * - Entrées depuis : (11,2) et (15,6)
 *
 * Véranda :
 * - Porte : (19,5)
 * - Entrée depuis : (19,6)
 *
 * Salle de bal :
 * - Portes : (17,9), (17,14), (19,8), (19,15)
 * - Entrées depuis : (16,9), (16,14), (19,7), (19,16)
 *
 * Hall :
 * - Portes : (4,9), (6,11), (6,12)
 * - Entrées depuis : (4,8), (7,11), (7,12)
 *
 * Salon :
 * - Porte : (5,17)
 * - Entrée depuis : (6,17)
 *
 * Salle à manger :
 * - Portes : (9,17) et (12,16)
 * - Entrées depuis : (8,17) et (12,15)
 *
 * Cuisine :
 * - Porte : (18,19)
 * - Entrée depuis : (17,19)
 * */
