package TestActionsJoueurs;

import exception.DemarragePartieException;
import exception.JoueurException;
import metier.Jeu.Joueur;
import metier.Jeu.Superviseur;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestMettreFinAuTour {
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

        superviseur.demarrer();
    }


    @Test
    public void TestMettreFinAuTourPointeurNull(){
        assertThrows(IllegalArgumentException.class, ()->{
            superviseur.mettreFinAuTour(null);
        });
    }

    @Test
    public void TestMettreFinAuTourAvecJoueurNonCourant(){
        assertThrows(JoueurException.class, ()->{
            superviseur.mettreFinAuTour(Bob);
        });
    }

    @Test
    public void TestMettreFinAuTourJoueurCourantAlorsQueLaPartieNAPasDemarre(){
        superviseur.setPartieCommencee(false);

        assertThrows(DemarragePartieException.class, ()->{
            superviseur.mettreFinAuTour(superviseur.getJoueurCourant());
        }, "Il devrait être impossible de mettre fin au tour du joueur courant alors que la partie n'a pas commencé");
    }

    @Test
    public void TestControleAliceNEstPlusLeJoueurCourant(){
        assertEquals(Alice, superviseur.getJoueurCourant(), "Le joueur courant devrait être le joueur Alice");

        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());

        assertNotEquals(Alice, superviseur.getJoueurCourant(), "Le joueur courant devrait être différent du joueur Alice");
    }

    @Test
    public void TestCycleRotationDesJoueurs(){
        assertEquals(Alice, superviseur.getJoueurCourant(), "Le joueur courant devrait être le joueur Alice");
        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());

        assertEquals(Bob, superviseur.getJoueurCourant(), "Le joueur courant devrait être le joueur Bob");
        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());

        assertEquals(Charles, superviseur.getJoueurCourant(), "Le joueur courant devrait être le joueur Charles");
        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());

        assertEquals(Alice, superviseur.getJoueurCourant(),"Le joueur courant devrait être le joueur Alice");
    }

    @Test
    public void TestControleDuNbDeplacementsDeLAncienJoueurCourantEtNouveauJoueurCourant(){
        superviseur.lancerDes(superviseur.getJoueurCourant());
        assertTrue(superviseur.getNbDeplacementsJoueurCourant() >= 2 && superviseur.getNbDeplacementsJoueurCourant() <= 12, "Le nbDeplacements du joueur courant devrait être dans l'interval [2;12]");

        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());
        assertEquals(0, superviseur.getNbDeplacementsJoueurCourant(), "Le nouveau joueur courant n'a pas lancé les dés donc son nb de déplacements devrait valoir 0");
    }
}
