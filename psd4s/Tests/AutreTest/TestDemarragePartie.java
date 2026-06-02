package AutreTest;

import exception.*;
import metier.Jeu.*;
import metier.cluedo.carte.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestDemarragePartie {
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
    }

    @Test
    public void testRedemarrerPartieEnCours(){
        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);
        superviseur.ajouterJoueur(Charles);

        superviseur.demarrer();

        assertThrows(DemarragePartieException.class, ()->{
            superviseur.demarrer();
        });
    }

    @Test
    public void testDemarrerPartieAvec1JoueurDansLaListe(){
        superviseur.ajouterJoueur(Alice);

        assertThrows(DemarragePartieException.class, ()->{
            superviseur.demarrer();
        });
    }

    @Test
    public void testDemarrerPartieAvec2JoueursDansLaListe(){
        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);

        assertThrows(DemarragePartieException.class, ()->{
            superviseur.demarrer();
        });
    }

    @Test
    public void testDemarrerPartieAvec3JoueursDansLaListe(){
        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);
        superviseur.ajouterJoueur(Charles);

        assertDoesNotThrow(()->{
           superviseur.demarrer();
        });
    }

    @Test
    public void testVerificationDeLAffectationDes6Positions(){
        Joueur Dylan = new Joueur("Dylan", EPersonnage.Professeur_Violet);
        Joueur Erwan = new Joueur("Erwan", EPersonnage.Madame_Pervenche);
        Joueur Fanny = new Joueur("Fanny", EPersonnage.Madame_Leblanc);

        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);
        superviseur.ajouterJoueur(Charles);
        superviseur.ajouterJoueur(Dylan);
        superviseur.ajouterJoueur(Erwan);
        superviseur.ajouterJoueur(Fanny);

        superviseur.demarrer();


        assertEquals(superviseur.getPlateau().getCase(0, 16), Alice.getPosition());
        assertEquals(superviseur.getPlateau().getCase(5, 0), Bob.getPosition());
        assertEquals(superviseur.getPlateau().getCase(7, 23), Charles.getPosition());
        assertEquals(superviseur.getPlateau().getCase(18, 0), Dylan.getPosition());
        assertEquals(superviseur.getPlateau().getCase(24, 9),  Erwan.getPosition());
        assertEquals(superviseur.getPlateau().getCase(24, 14), Fanny.getPosition());
    }
}