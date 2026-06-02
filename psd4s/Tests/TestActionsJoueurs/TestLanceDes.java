package TestActionsJoueurs;

import exception.DemarragePartieException;
import exception.JoueurException;
import exception.ReglesException;
import metier.Jeu.Joueur;
import metier.Jeu.Superviseur;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestLanceDes {
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
    }

    @Test
    public void TestLancerDesAlorsQueLaPartieNAPasCommence(){
        assertThrows(DemarragePartieException.class, ()->{
            superviseur.lancerDes(superviseur.getJoueurCourant());
        }, "Aucun joueur ne devrait être en mesure de pouvoir lancer les dés lorsque la partie n'a pas démarré");
    }

    @Test
    public void TestLancerDesJoueurCourant(){
        superviseur.demarrer();
        assertDoesNotThrow(()->{
           superviseur.lancerDes(superviseur.getJoueurCourant());
       }, "Le joueur courant devrait être en mesure de lancer les dés");
    }

    @Test
    public void TestLancerDesJoueurNonCourant(){
        superviseur.demarrer();
        assertThrows(JoueurException.class, ()->{
            superviseur.lancerDes(Bob);
        });
    }

    @Test
    public void TestLancerDesJoueurElimine(){
        superviseur.demarrer();
        superviseur.eliminerJoueurCourantETMettreFinAuTour();

        assertThrows(JoueurException.class, ()->{
            superviseur.lancerDes(Alice);
        });
    }

    @Test
    public void TestLancerDesPointeurNull(){
        superviseur.demarrer();
        assertThrows(IllegalArgumentException.class, ()->{
            superviseur.lancerDes(null);
        });
    }

    @Test
    public void TestMAJNbDeplacementsJoueurCourantApresLancerDeDes(){
        superviseur.demarrer();
        assertEquals(0, superviseur.getNbDeplacementsJoueurCourant(), "Le joueur courant devrait avoir son nbDeplacements égal à 0");
        superviseur.lancerDes(superviseur.getJoueurCourant());
        assertTrue(superviseur.getNbDeplacementsJoueurCourant() >= 2 && superviseur.getNbDeplacementsJoueurCourant() <= 12, "Le nbDeplacements du joueur courant devrait être dans l'interval [2;12]");
    }

    @Test
    public void Test2ndLancerDesJoueurCourant(){
        superviseur.demarrer();
        superviseur.lancerDes(superviseur.getJoueurCourant());
        assertThrows(ReglesException.class, ()->{
            superviseur.lancerDes(superviseur.getJoueurCourant());
        }, "Le joueur courant n'est pas censé pouvoir lancer 2 fois les dés");
    }

    @Test
    public void TestValiditeIntervalLancerDes(){
        superviseur.demarrer();
        for (int i = 0; i < 10; i++){
            superviseur.lancerDes(superviseur.getJoueurCourant());
            assertTrue(superviseur.getNbDeplacementsJoueurCourant() >= 2 && superviseur.getNbDeplacementsJoueurCourant() <= 12, "Le nbDeplacements du joueur courant devrait être dans l'interval [2;12]");
            superviseur.mettreFinAuTour(superviseur.getJoueurCourant());
        }
    }

   @Test
    public void TestLancerDesApresTourSuivant(){
       superviseur.demarrer();
        assertEquals(Alice, superviseur.getJoueurCourant());
        superviseur.lancerDes(superviseur.getJoueurCourant());
        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());

        assertEquals(Bob,  superviseur.getJoueurCourant());
        assertDoesNotThrow(()->{
            superviseur.lancerDes(superviseur.getJoueurCourant());
        });
   }
}
