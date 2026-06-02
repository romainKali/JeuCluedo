package reseau.integration.Tests;


import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Vérifie le traitement de la commande protocolaire {@code @LANCER_DES}.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lors du lancer de dés.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>le lancer correct des dés par le joueur courant ;</li>
 *     <li>l'envoi du résultat uniquement au joueur concerné ;</li>
 *     <li>la mise à jour du nombre de déplacements disponibles ;</li>
 *     <li>le respect de l'intervalle possible du lancer, entre 2 et 12 ;</li>
 *     <li>le refus de lancer les dés avant le démarrage de la partie ;</li>
 *     <li>le refus de lancer les dés par un joueur non courant ;</li>
 *     <li>le refus d'un second lancer de dés pendant le même tour.</li>
 * </ul>
 */
public class TestLancerDes {
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
     * joueurs déjà connectés.
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
    public void testProtocoleLancerDes(){

        Alice.traiterMessage("@DEMARRER");

        assertEquals(Alice.getJoueur(), serveur.getSuperviseur().getJoueurCourant());
        assertEquals(0, serveur.getSuperviseur().getNbDeplacementsJoueurCourant());

        assertEquals(5, fakeThreadBob.getMessagesEnvoyesParLeServeur().size());

        Alice.traiterMessage("@LANCER_DES");

        assertEquals(5, fakeThreadBob.getMessagesEnvoyesParLeServeur().size());
        assertTrue(fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast().startsWith("@LANCER_DES_REUSSI"));
        assertTrue(serveur.getSuperviseur().getNbDeplacementsJoueurCourant() >= 2);
        assertTrue(serveur.getSuperviseur().getNbDeplacementsJoueurCourant() <= 12);
    }

    @Test
    public void testProtocoleLancerDesAvantDemarragePartie(){
        Alice.traiterMessage("@LANCER_DES");
        assertEquals("@ERREUR Il est impossible de lancer les dés si la partie n'a pas démarrée", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleLancerDesParJoueurNonCourant(){
        Bob.traiterMessage("@DEMARRER");
        Bob.traiterMessage("@LANCER_DES");
        assertEquals("@ERREUR Seul le joueur courant peut lancer les dés", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleLancerDes2fois(){
        Alice.traiterMessage("@DEMARRER");
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@LANCER_DES");
        assertEquals("@ERREUR Le joueur courant a déjà lancé les dés", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
    }
}
