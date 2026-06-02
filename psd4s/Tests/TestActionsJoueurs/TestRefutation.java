package TestActionsJoueurs;

import exception.JoueurNePouvantRefuterException;
import exception.ReglesException;
import exception.SoupconNonRefutableException;
import metier.Jeu.Hypothese;
import metier.Jeu.Joueur;
import metier.Jeu.Superviseur;
import metier.cluedo.carte.Carte;
import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

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






public class TestRefutation {
    /*
    Enigme :
    - Personnage : Professeur_Violet
    - Lieu : Cuisine
    - Arme : Revolver
    */

    /*
    Alice :
    - Mademoiselle_Rose
    - Bureau
    - Salle_de_bal
    - Poignard
    - Corde
    - Salon
    */

    /*
    Bob :
    - Colonel_Moutarde
    - Bibliotheque
    - Hall
    - Chandelier
    - Cle_anglaise
    - Salle_de_billard
    */

    /*
    Charles :
    - Madame_Leblanc
    - Reverend_Olive
    - Madame_Pervenche
    - Veranda
    - Salle_a_manger
    - Matraque
    */

    Superviseur superviseur;
    Joueur Alice;
    Joueur Bob;
    Joueur Charles;


    @BeforeEach
    public void setUp() {
        superviseur = Superviseur.getInstance();
        superviseur.resetSuperviseur();

        Alice = new Joueur("Alice", EPersonnage.Mademoiselle_Rose);
        Bob = new Joueur("Bob", EPersonnage.Colonel_Moutarde);
        Charles = new Joueur("Charles", EPersonnage.Reverend_Olive);

        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);
        superviseur.ajouterJoueur(Charles);

        Hypothese enigme = new Hypothese(ELieu.Cuisine, EPersonnage.Professeur_Violet, EArme.Poignard);

        ArrayList<Carte> mainAlice = new ArrayList<>();
        ArrayList<Carte> mainBob = new ArrayList<>();
        ArrayList<Carte> mainCharles = new ArrayList<>();

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

        superviseur.demarrer();
        Alice.setCartes(mainAlice);
        Bob.setCartes(mainBob);
        Charles.setCartes(mainCharles);
        superviseur.setEnigme(enigme);
    }

    @Test
    public void testOrdreDesJoueurs(){
        assertEquals(Alice,  superviseur.getJoueurCourant(), "Le joueur courant devrait être Alice");
        superviseur.mettreFinAuTour(Alice);
        assertEquals(Bob, superviseur.getJoueurCourant(), "Le joueur courant devrait être Bob");
        superviseur.mettreFinAuTour(Bob);
        assertEquals(Charles, superviseur.getJoueurCourant(), "Le joueur courant doit être Charles");
    }

    @Test
    public void testAliceLanceUnSoupconEtBobLeRefute(){
        assertFalse(Alice.getPeutSoupconner(), "Alice ne devrait pas être en mesure de pouvoir soupconner");

        superviseur.setNbDeplacementsJoueurCourant(15);
        for (int i = 1; i <= 6; i++){
            superviseur.deplacer(Alice, superviseur.getPlateau().getCase(i, 16));
        }
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(6, 17));
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(5, 17));

        assertTrue(Alice.getPeutSoupconner(), "Alice devrait être en mesure de pouvoir soupconner");

        superviseur.soupcon(Alice, EPersonnage.Colonel_Moutarde, EArme.Poignard);
        assertEquals(Bob, superviseur.getListeJoueurs().get(superviseur.getIndexJoueurDevantRefuterSoupcon()));

        // Alice ne doit pas être en mesure de mettre fin à son tour alors que le soupcon qu'elle vient d'émettre n'a pas encore été réfuté
        assertThrows(ReglesException.class, ()->{
            superviseur.mettreFinAuTour(Alice);
        });

        assertDoesNotThrow(()->{
            superviseur.refuterSoupcon(Bob, EPersonnage.Colonel_Moutarde);
        });

        assertNull(superviseur.getSoupconEnCours());

        // Puisque le soupcon a été réfuté, Alice peut enfin mettre fin à son tour
        assertDoesNotThrow(()->{
            superviseur.mettreFinAuTour(Alice);
        });
    }

    @Test
    public void testBobEssayeDeNePasRefuterLeSoupconAlorsQuIlEstEnMesureDePouvoirLeFaire(){
        superviseur.setNbDeplacementsJoueurCourant(15);
        for (int i = 1; i <= 6; i++){
            superviseur.deplacer(Alice, superviseur.getPlateau().getCase(i, 16));
        }
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(6, 17));
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(5, 17));

        superviseur.soupcon(Alice, EPersonnage.Colonel_Moutarde, EArme.Poignard);

        assertThrows(ReglesException.class, ()->{
            superviseur.refuterSoupcon(Bob, null);
        }, "Bob ne devrait pas être autorisé à ne pas refuter le soupcon");
    }


    @Test
    public void testAliceLanceUnSoupconQuePersonneNePeutRefuter(){
        // On déplace Alice dans la cuisine
        Alice.setPosition(superviseur.getPlateau().getCase(17, 19));
        superviseur.setNbDeplacementsJoueurCourant(1);
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(18, 19));

        /**
         * Alice émet un soupcon qui est égal à l'énigme
         * */
        superviseur.soupcon(Alice, EPersonnage.Professeur_Violet, EArme.Poignard);

        assertThrows(JoueurNePouvantRefuterException.class, ()->{
            superviseur.refuterSoupcon(Bob, null);
        });

        assertEquals(Charles, superviseur.getListeJoueurs().get(superviseur.getIndexJoueurDevantRefuterSoupcon()));

        /**
         * Puisque le dernier joueur n'a pas été en mesure de refuter le soupcon émis ALORS
         * un soupconNonRefutableException (Et non un JoueurNePouvantRefuterException)
         * */
        assertThrows(SoupconNonRefutableException.class, ()->{
            superviseur.refuterSoupcon(Charles, null);
        });
    }

    @Test
    public void testRefuterUnSoupconAvecUneCarteQueLonNePossedePas(){
        Alice.setPosition(superviseur.getPlateau().getCase(17, 19));
        superviseur.setNbDeplacementsJoueurCourant(1);
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(18, 19));

        superviseur.soupcon(Alice, EPersonnage.Colonel_Moutarde, EArme.Poignard);

        assertThrows(ReglesException.class, ()->{
            superviseur.refuterSoupcon(Bob, EArme.Poignard);
        });
    }

    @Test
    public void testEmettreUnSoupconAlorsQuIlNYaQUnSeulJoueur(){
        Bob.setEstElimine(true);
        Charles.setEstElimine(true);

        Alice.setPosition(superviseur.getPlateau().getCase(17, 19));
        superviseur.setNbDeplacementsJoueurCourant(1);
        superviseur.deplacer(Alice, superviseur.getPlateau().getCase(18, 19));

        assertThrows(ReglesException.class, ()->{
            superviseur.soupcon(Alice, EPersonnage.Colonel_Moutarde, EArme.Chandelier);
        });
    }
}
