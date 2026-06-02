package reseau.integration.Tests;

import metier.Jeu.Hypothese;
import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Vérifie le traitement de la commande protocolaire {@code @ACCUSER}.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lors d'une accusation dans
 * une partie de Cluedo.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>la victoire d'un joueur lorsqu'il accuse correctement l'énigme ;</li>
 *     <li>l'élimination d'un joueur après une accusation incorrecte ;</li>
 *     <li>le passage automatique au joueur suivant après une élimination ;</li>
 *     <li>l'impossibilité pour un joueur éliminé d'effectuer des actions ;</li>
 *     <li>l'interdiction pour un joueur non courant d'accuser ;</li>
 *     <li>l'interdiction d'accuser avant le démarrage de la partie ;</li>
 *     <li>la gestion des paramètres invalides transmis au protocole ;</li>
 *     <li>la fin de partie lorsque le dernier joueur actif est éliminé.</li>
 * </ul>
 *
 * Chaque test vérifie à la fois les messages envoyés aux clients
 * et les modifications d'état réalisées par le {@link metier.Jeu.Superviseur}.
 */
public class TestAccuse {
    Serveur serveur;
    ConnexionUtilisateur Alice;
    ConnexionUtilisateur Bob;
    ConnexionUtilisateur Charles;
    FakeThreadConnexion fakeThreadAlice;
    FakeThreadConnexion fakeThreadBob;
    FakeThreadConnexion fakeThreadCharles;


    /**
     * Initialise un environnement de test standard composé :
     * <ul>
     *     <li>d'un serveur de test ;</li>
     *     <li>de trois connexions simulées (Alice, Bob et Charles) ;</li>
     *     <li>de trois faux threads permettant de capturer les messages
     *     envoyés par le serveur ;</li>
     *     <li>de trois joueurs connectés au protocole Cluedo.</li>
     * </ul>
     *
     * Chaque test démarre ainsi dans un état identique et indépendant.
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

        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");
    }

    @Test
    public void testProtocoleAccuserVictoire(){
        Alice.traiterMessage("@DEMARRER");

        assertTrue(serveur.getSuperviseur().getPartieCommencee());

        Hypothese enigme = new Hypothese(ELieu.Bureau, EPersonnage.Mademoiselle_Rose, EArme.Poignard);

        serveur.getSuperviseur().setEnigme(enigme);

        Alice.traiterMessage("@ACCUSER P0 A0 L0");
        assertEquals("@VICTOIRE Alice", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@VICTOIRE Alice", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@VICTOIRE Alice", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        assertFalse(serveur.getSuperviseur().getPartieCommencee());
    }

    @Test
    public void testProtocoleAccuserElimination(){
        Alice.traiterMessage("@DEMARRER");

        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());

        Alice.traiterMessage("@ACCUSER P0 A0 L0");

        assertEquals("@JOUEUR_ELIM Alice", fakeThreadAlice.getMessagesEnvoyesParLeServeur().get(fakeThreadAlice.getMessagesEnvoyesParLeServeur().size()-2));
        assertEquals("@JOUEUR_ELIM Alice", fakeThreadBob.getMessagesEnvoyesParLeServeur().get(fakeThreadBob.getMessagesEnvoyesParLeServeur().size()-2));
        assertEquals("@JOUEUR_ELIM Alice", fakeThreadCharles.getMessagesEnvoyesParLeServeur().get(fakeThreadCharles.getMessagesEnvoyesParLeServeur().size()-2));

        assertEquals(Bob.getJoueur(), serveur.getSuperviseur().getJoueurCourant());

        assertEquals("@FIN_TOUR Bob", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Bob", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Bob", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());


        Alice.traiterMessage("@LANCER_DES");
        assertEquals("@ERREUR Un joueur éliminé ne peut lancer les dés", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        Alice.traiterMessage("@FIN_TOUR");
        assertEquals("@ERREUR Un joueur éliminé ne peut mettre fin à son tour", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        Alice.traiterMessage("@ALLER_VERS 1 16");
        assertEquals("@ERREUR Un joueur éliminé ne peut se déplacer", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        Alice.traiterMessage("@ACCUSER P0 A0 L0");
        assertEquals("@ERREUR Un joueur éliminé ne peut lancer une accusation", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());

        assertTrue(Alice.getJoueur().getEstElimine());
        assertFalse(Bob.getJoueur().getEstElimine());
        assertFalse(Charles.getJoueur().getEstElimine());
    }

    @Test
    public void testProtocoleAccuserAvecJoueurNonCourant(){
        Alice.traiterMessage("@DEMARRER");
        Bob.traiterMessage("@ACCUSER P0 A0 L0");
        assertEquals("@ERREUR Seul le joueur courant peut lancer une accusation", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());
        assertFalse(Bob.getJoueur().getEstElimine());
    }

    @Test
    public void testProtocoleAccuserAvantDemarrage(){
        Alice.traiterMessage("@ACCUSER P0 A0 L0");
        assertEquals("@ERREUR On ne peut accuser un joueur lorsque la partie n'a pas commencée", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleAccuserAvecParametresInvalides(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@ACCUSER P7 A7 L7");
        assertEquals("@ERREUR_PARAM un ou plusieurs des éléments de votre accusation sont incorrects", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertFalse(Alice.getJoueur().getEstElimine());
        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());
    }

    @Test
    public void testProtocoleAccuserAlorsQuIlNeRestePlusQ1Joueur(){
        Alice.traiterMessage("@DEMARRER");

        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());
        Alice.traiterMessage("@ACCUSER P0 A0 L0");

        assertEquals("@JOUEUR_ELIM Alice", fakeThreadAlice.getMessagesEnvoyesParLeServeur().get(fakeThreadAlice.getMessagesEnvoyesParLeServeur().size()-2));
        assertEquals("@JOUEUR_ELIM Alice", fakeThreadBob.getMessagesEnvoyesParLeServeur().get(fakeThreadBob.getMessagesEnvoyesParLeServeur().size()-2));
        assertEquals("@JOUEUR_ELIM Alice", fakeThreadCharles.getMessagesEnvoyesParLeServeur().get(fakeThreadCharles.getMessagesEnvoyesParLeServeur().size()-2));

        assertEquals("@FIN_TOUR Bob", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Bob", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Bob", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        assertEquals(Bob.getJoueur(), serveur.getSuperviseur().getJoueurCourant());
        Bob.traiterMessage("@ACCUSER P0 A0 L0");

        assertEquals("@JOUEUR_ELIM Bob", fakeThreadAlice.getMessagesEnvoyesParLeServeur().get(fakeThreadAlice.getMessagesEnvoyesParLeServeur().size()-2));
        assertEquals("@JOUEUR_ELIM Bob", fakeThreadBob.getMessagesEnvoyesParLeServeur().get(fakeThreadBob.getMessagesEnvoyesParLeServeur().size()-2));
        assertEquals("@JOUEUR_ELIM Bob", fakeThreadCharles.getMessagesEnvoyesParLeServeur().get(fakeThreadCharles.getMessagesEnvoyesParLeServeur().size()-2));

        assertEquals("@FIN_TOUR Charles", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Charles", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@FIN_TOUR Charles", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        assertEquals(Charles.getJoueur(), serveur.getSuperviseur().getJoueurCourant());

        Charles.traiterMessage("@ACCUSER P0 A0 L0");

        assertEquals("@DEFAITE Aucun joueur n'a été en mesure de déterminer l'enigme...", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@DEFAITE Aucun joueur n'a été en mesure de déterminer l'enigme...", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@DEFAITE Aucun joueur n'a été en mesure de déterminer l'enigme...", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());
    }
}
