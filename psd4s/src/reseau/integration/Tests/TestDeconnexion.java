package reseau.integration.Tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

/**
 * Vérifie le traitement de la commande protocolaire {@code @DECONNEXION}.
 *
 * Cette suite de tests d'intégration valide le comportement du serveur
 * lorsqu'un joueur se déconnecte, avant ou pendant une partie de Cluedo.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>la suppression d'un joueur connecté avant le démarrage de la partie ;</li>
 *     <li>la fermeture du thread de connexion associé au joueur déconnecté ;</li>
 *     <li>l'interruption de la partie lorsqu'un joueur se déconnecte en cours de jeu ;</li>
 *     <li>la notification des autres joueurs lorsqu'une déconnexion survient pendant la partie ;</li>
 *     <li>la mise à jour de la liste des connexions côté serveur et de la liste des joueurs côté superviseur.</li>
 * </ul>
 */
public class TestDeconnexion {
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
     * Aucun joueur n'est encore connecté au protocole Cluedo au début du test.
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
    public void testProtocoleDeconnexionAvantDemarragePartie(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        assertEquals(1, serveur.size());
        assertEquals(1, serveur.getSuperviseur().getListeJoueurs().size());

        Alice.traiterMessage("@DECONNEXION");
        assertEquals(0, serveur.size());
        assertEquals(0, serveur.getSuperviseur().getListeJoueurs().size());

        assertTrue(fakeThreadAlice.estFini());
    }

    @Test
    public void testProtocoleDeconnexionPendantPartie(){
        Alice.traiterMessage("@CONNEXION Alice P0");
        Bob.traiterMessage("@CONNEXION Bob P1");
        Charles.traiterMessage("@CONNEXION Charles P2");

        Alice.traiterMessage("@DEMARRER");

        assertTrue(serveur.getSuperviseur().getPartieCommencee());
        assertEquals(3, serveur.getSuperviseur().getListeJoueurs().size());
        assertEquals(3, serveur.size());

        Alice.traiterMessage("@DECONNEXION");

        assertFalse(serveur.getSuperviseur().getPartieCommencee());
        assertEquals(2, serveur.getSuperviseur().getListeJoueurs().size());
        assertEquals(2, serveur.size());

        assertEquals("@ERREUR_DECONNEXION Alice s'est déconnecté durant la partie",  fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@ERREUR_DECONNEXION Alice s'est déconnecté durant la partie",  fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        assertTrue(fakeThreadAlice.estFini());
    }
}
