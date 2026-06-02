package reseau.integration.Tests;

import metier.Jeu.Hypothese;
import metier.cluedo.carte.Carte;
import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;
import reseau.integration.FakeThreadConnexion;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le traitement de la commande protocolaire {@code @INDICE},
 * utilisée pour réfuter un soupçon en cours.
 *
 * Cette suite de tests d'intégration valide les interactions entre
 * la couche réseau et la couche métier lors de la phase de réfutation
 * d'un soupçon dans une partie de Cluedo.
 *
 * Les scénarios couverts vérifient notamment :
 * <ul>
 *     <li>la réfutation correcte d'un soupçon par un joueur possédant une carte valide ;</li>
 *     <li>le déroulement complet d'une chaîne de réfutations ;</li>
 *     <li>la gestion d'un soupçon impossible à réfuter ;</li>
 *     <li>la transmission des cartes de réfutation au joueur ayant émis le soupçon ;</li>
 *     <li>l'interdiction de réfuter lorsqu'on n'est pas le joueur désigné ;</li>
 *     <li>l'interdiction d'utiliser une carte que l'on ne possède pas ;</li>
 *     <li>l'interdiction d'utiliser une carte n'appartenant pas au soupçon ;</li>
 *     <li>l'interdiction de réfuter lorsqu'aucun soupçon n'est en cours ;</li>
 *     <li>l'interdiction de réfuter avant le démarrage de la partie.</li>
 * </ul>
 *
 * Chaque test vérifie à la fois les messages échangés entre les clients,
 * les transitions d'état du {@link metier.Jeu.Superviseur} ainsi que
 * l'évolution du soupçon en cours.
 */
public class TestRefutation {
    Serveur serveur;
    ConnexionUtilisateur Alice;
    ConnexionUtilisateur Bob;
    ConnexionUtilisateur Charles;
    FakeThreadConnexion fakeThreadAlice;
    FakeThreadConnexion fakeThreadBob;
    FakeThreadConnexion fakeThreadCharles;
    Hypothese enigme;
    ArrayList<Carte> mainAlice;
    ArrayList<Carte> mainBob;
    ArrayList<Carte> mainCharles;

    /**
     * Initialise un environnement de test composé :
     * <ul>
     *     <li>d'un serveur de test ;</li>
     *     <li>de trois connexions simulées représentant Alice, Bob et Charles ;</li>
     *     <li>de trois faux threads permettant de capturer les messages envoyés par le serveur ;</li>
     *     <li>d'une énigme connue afin de contrôler les scénarios de réfutation ;</li>
     *     <li>de mains de cartes déterministes pour chaque joueur.</li>
     * </ul>
     *
     * L'utilisation de mains prédéfinies permet de reproduire précisément
     * les différentes situations de réfutation testées.
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

        enigme = new Hypothese(ELieu.Cuisine, EPersonnage.Professeur_Violet, EArme.Poignard);

        mainAlice = new ArrayList<>();
        mainBob = new ArrayList<>();
        mainCharles = new ArrayList<>();

        mainAlice.add(EPersonnage.Mademoiselle_Rose);
        mainAlice.add(ELieu.Bureau);
        mainAlice.add(ELieu.Salle_de_bal);
        mainAlice.add(ELieu.Salon);
        mainAlice.add(EArme.Poignard);
        mainAlice.add(EArme.Corde);

        mainBob.add(EPersonnage.Colonel_Moutarde);
        mainBob.add(ELieu.Bibliotheque);
        mainBob.add(ELieu.Hall);
        mainBob.add(ELieu.Salle_de_billard);
        mainBob.add(EArme.Chandelier);
        mainBob.add(EArme.Cle_anglaise);

        mainCharles.add(EPersonnage.Madame_Leblanc);
        mainCharles.add(EPersonnage.Reverend_Olive);
        mainCharles.add(EPersonnage.Madame_Pervenche);
        mainCharles.add(ELieu.Veranda);
        mainCharles.add(ELieu.Salle_a_manger);
        mainCharles.add(EArme.Matraque);
    }

    @Test
    public void testProtocoleRefutation(){
        Alice.traiterMessage("@DEMARRER");


        Alice.getJoueur().setCartes(mainAlice);
        Bob.getJoueur().setCartes(mainBob);
        Charles.getJoueur().setCartes(mainCharles);
        serveur.getSuperviseur().setEnigme(enigme);


        assertNull(serveur.getSuperviseur().getSoupconEnCours());
        assertNull(serveur.getSuperviseur().getJoueurDevantRefuterSoupcon());


        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(8, 7));
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 8 6");


        assertEquals("P1", Serveur.carteToString(EPersonnage.Colonel_Moutarde));
        assertEquals("A0", Serveur.carteToString(EArme.Poignard));
        assertEquals("L1", Serveur.carteToString(ELieu.Bibliotheque));


        Alice.traiterMessage("@SOUPCONNER P1 A0");
        assertEquals("@SOUPCON_EMIS Alice P1 A0 L1", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());


        assertNotNull(serveur.getSuperviseur().getSoupconEnCours());
        assertEquals(Bob.getJoueur(), serveur.getSuperviseur().getJoueurDevantRefuterSoupcon());


        Bob.traiterMessage("@INDICE L1");


        assertEquals("@CARTE_REFUTANT_SOUPCON L1 Bob", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@SOUPCON_REFUTE Bob", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@SOUPCON_REFUTE Bob", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleRefutationAvecSoupconEgalALEnigme(){
        Alice.traiterMessage("@DEMARRER");


        Alice.getJoueur().setCartes(mainAlice);
        Bob.getJoueur().setCartes(mainBob);
        Charles.getJoueur().setCartes(mainCharles);
        serveur.getSuperviseur().setEnigme(enigme);


        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(17, 19));
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 18 19");


        assertEquals("P5", Serveur.carteToString(EPersonnage.Professeur_Violet));
        assertEquals("A0", Serveur.carteToString(EArme.Poignard));
        assertEquals("L8", Serveur.carteToString(ELieu.Cuisine));


        Alice.traiterMessage("@SOUPCONNER P5 A0");


        assertNotNull(serveur.getSuperviseur().getSoupconEnCours());
        assertNotNull(serveur.getSuperviseur().getJoueurDevantRefuterSoupcon());


        assertEquals("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());


        Bob.traiterMessage("@INDICE P1");
        assertEquals("@ERREUR Vous ne possédez aucune carte permettant de réfuter ce soupçon", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());

        Bob.traiterMessage("@INDICE null");


        assertEquals("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());
        Charles.traiterMessage("@INDICE null");

        assertEquals("@INFORMATION_REFUTATION aucun des joueurs présent n'a été en mesure de réfuter le soupcon émis par Alice", fakeThreadAlice.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@INFORMATION_REFUTATION aucun des joueurs présent n'a été en mesure de réfuter le soupcon émis par Alice", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertEquals("@INFORMATION_REFUTATION aucun des joueurs présent n'a été en mesure de réfuter le soupcon émis par Alice", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());

        assertNull(serveur.getSuperviseur().getSoupconEnCours());
        assertNull(serveur.getSuperviseur().getJoueurDevantRefuterSoupcon());

        fakeThreadAlice.afficherTousLesMessagesEnvoyesParLeServeur();
        fakeThreadBob.afficherTousLesMessagesEnvoyesParLeServeur();
        fakeThreadCharles.afficherTousLesMessagesEnvoyesParLeServeur();
    }

    @Test
    public void testProtocoleRefutationAvecLeMauvaisJoueur(){
        Alice.traiterMessage("@DEMARRER");


        Alice.getJoueur().setCartes(mainAlice);
        Bob.getJoueur().setCartes(mainBob);
        Charles.getJoueur().setCartes(mainCharles);
        serveur.getSuperviseur().setEnigme(enigme);


        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(17, 19));
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 18 19");


        assertEquals("P5", Serveur.carteToString(EPersonnage.Professeur_Violet));
        assertEquals("A0", Serveur.carteToString(EArme.Poignard));
        assertEquals("L8", Serveur.carteToString(ELieu.Cuisine));


        Alice.traiterMessage("@SOUPCONNER P5 A0");

        assertEquals("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());

        Charles.traiterMessage("@INDICE null");
        assertEquals("@ERREUR Vous n'êtes pas autoriser à refuter le soupcon en cours", fakeThreadCharles.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleRefutationAvecUneCarteQueLeJoueurNePossedePas(){
        Alice.traiterMessage("@DEMARRER");


        Alice.getJoueur().setCartes(mainAlice);
        Bob.getJoueur().setCartes(mainBob);
        Charles.getJoueur().setCartes(mainCharles);
        serveur.getSuperviseur().setEnigme(enigme);


        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(17, 19));
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 18 19");


        assertEquals("P5", Serveur.carteToString(EPersonnage.Professeur_Violet));
        assertEquals("A0", Serveur.carteToString(EArme.Poignard));
        assertEquals("L8", Serveur.carteToString(ELieu.Cuisine));


        Alice.traiterMessage("@SOUPCONNER P5 A0");

        assertEquals("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        Bob.traiterMessage("@INDICE P5");
        assertEquals("@ERREUR Vous ne pouvez refuter un soupcon avec une carte que vous ne possédez pas", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleRefutationAvecUneCarteNAppartenantPasAuSoupcon(){
        Alice.traiterMessage("@DEMARRER");


        Alice.getJoueur().setCartes(mainAlice);
        Bob.getJoueur().setCartes(mainBob);
        Charles.getJoueur().setCartes(mainCharles);
        serveur.getSuperviseur().setEnigme(enigme);


        Alice.getJoueur().setPosition(serveur.getSuperviseur().getPlateau().getCase(17, 19));
        Alice.traiterMessage("@LANCER_DES");
        Alice.traiterMessage("@ALLER_VERS 18 19");


        assertEquals("P5", Serveur.carteToString(EPersonnage.Professeur_Violet));
        assertEquals("A0", Serveur.carteToString(EArme.Poignard));
        assertEquals("L8", Serveur.carteToString(ELieu.Cuisine));


        Alice.traiterMessage("@SOUPCONNER P5 A0");


        assertNotNull(serveur.getSuperviseur().getSoupconEnCours());
        assertNotNull(serveur.getSuperviseur().getJoueurDevantRefuterSoupcon());


        assertEquals("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());


        Bob.traiterMessage("@INDICE P1");
        assertEquals("@ERREUR Vous ne possédez aucune carte permettant de réfuter ce soupçon", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
    }

    @Test
    public void testProtocoleRefutationAvecAucunSoupconEnCours(){
        Alice.traiterMessage("@DEMARRER");
        Bob.traiterMessage("@INDICE P1");
        assertEquals("@ERREUR On ne peut refuter un soupcon qui n'existe pas !", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
        assertNull(serveur.getSuperviseur().getSoupconEnCours());
    }

    @Test
    public void testProtocoleRefutationAlorsQueLaPartieNAPasDemarree(){
        Bob.traiterMessage("@INDICE P1");
        assertEquals("@ERREUR Aucune carte ne peut refuter un soupcon lorsque la partie n'a pas commencee", fakeThreadBob.getMessagesEnvoyesParLeServeur().getLast());
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