package TestCartes;

import exception.*;
import metier.Jeu.*;
import metier.cluedo.carte.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TestCartes {
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
    public void testVerificationCartesDeLEnigmeApresDemarragePartie(){
        assertNull(superviseur.getEnigme(), "L'enigme doit être un pointeur null lorsque la partie n'a pas démarré");

        superviseur.demarrer();

        assertNotNull(superviseur.getEnigme().getPersonnage());
        assertNotNull(superviseur.getEnigme().getLieu());
        assertNotNull(superviseur.getEnigme().getArme());
    }

    @Test
    public void testVerificationQUAucunDesJoueursNePossedentUneCarteDeLEnigme(){
        superviseur.demarrer();

        Carte carteEnigmePersonnage = superviseur.getEnigme().getPersonnage();
        Carte carteEnigmeLieu = superviseur.getEnigme().getLieu();
        Carte carteEnigmeArme = superviseur.getEnigme().getArme();

        for (Joueur joueur : superviseur.getListeJoueurs()){
            assertFalse(joueur.getCartes().contains(carteEnigmePersonnage));
            assertFalse(joueur.getCartes().contains(carteEnigmeLieu));
            assertFalse(joueur.getCartes().contains(carteEnigmeArme));
        }
    }

    @Test
    public void testVerificationNbTotalDeCartesDistribueesEstEgalA21(){
        superviseur.demarrer();

        int totalCartes = 0;
        for (Joueur joueur : superviseur.getListeJoueurs()){
            totalCartes += joueur.getCartes().size();
        }

        assertEquals(21, totalCartes+3);
    }

    @Test
    public void testVerificationQueLesJoueursNOntPasDeCartesLorsqueLaPartieNaPasCommencee(){
        int totalCartes = 0;
        for (Joueur joueur : superviseur.getListeJoueurs()){
            totalCartes += joueur.getCartes().size();
        }

        assertEquals(0, totalCartes);
    }

    @Test
    public void testVerificationQueLesJoueursNOntPlusDeCartesLorsqueLaPartieSeTermine(){
        superviseur.demarrer();

        ArrayList<Joueur> joueursPartie = new ArrayList<>(superviseur.getListeJoueurs());
        superviseur.gestionFinPartie();

        int totalCartes = 0;
        for (Joueur joueur : joueursPartie){
            totalCartes += joueur.getCartes().size();
        }

        assertEquals(0, totalCartes);
    }

    @Test
    public void testEquiteDistributionDesCartesAvec3JoueursAuTotal(){
        superviseur.demarrer();

        for (Joueur joueur : superviseur.getListeJoueurs()){
            assertEquals(6,  joueur.getCartes().size());
        }
    }

    @Test
    public void testEquiteDistributionDesCartesAvec4JoueursAuTotal(){
        Joueur Dylan = new Joueur("Dylan", EPersonnage.Professeur_Violet);
        superviseur.ajouterJoueur(Dylan);

        superviseur.demarrer();

        assertEquals(5, Alice.getCartes().size());
        assertEquals(5, Bob.getCartes().size());
        assertEquals(4, Charles.getCartes().size());
        assertEquals(4, Dylan.getCartes().size());

        assertEquals(4, superviseur.getJoueurCourant().getCartes().size());
        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());
        assertEquals(4, superviseur.getJoueurCourant().getCartes().size());
    }

    @Test
    public void testEquiteDistributionDesCartesAvec5JoueursAuTotal(){
        Joueur Dylan = new Joueur("Dylan", EPersonnage.Professeur_Violet);
        Joueur Erwan = new Joueur("Erwan", EPersonnage.Madame_Pervenche);
        superviseur.ajouterJoueur(Dylan);
        superviseur.ajouterJoueur(Erwan);

        superviseur.demarrer();

        assertEquals(4, Alice.getCartes().size());
        assertEquals(4, Bob.getCartes().size());
        assertEquals(4, Charles.getCartes().size());
        assertEquals(3, Dylan.getCartes().size());
        assertEquals(3,  Erwan.getCartes().size());

        assertEquals(3, superviseur.getJoueurCourant().getCartes().size());
        superviseur.mettreFinAuTour(superviseur.getJoueurCourant());
        assertEquals(3, superviseur.getJoueurCourant().getCartes().size());
    }

    @Test
    public void testEquiteDistributionDesCartesAvec6JoueursAuTotal(){
        Joueur Dylan = new Joueur("Dylan", EPersonnage.Professeur_Violet);
        Joueur Erwan = new Joueur("Erwan", EPersonnage.Madame_Pervenche);
        Joueur Fanny = new Joueur("Fanny", EPersonnage.Madame_Leblanc);
        superviseur.ajouterJoueur(Dylan);
        superviseur.ajouterJoueur(Erwan);
        superviseur.ajouterJoueur(Fanny);

        superviseur.demarrer();

        for (Joueur joueur : superviseur.getListeJoueurs()){
            assertEquals(3,  joueur.getCartes().size());
        }
    }

    @Test
    public void testGetCarteRefuterSoupconPointeurNull(){
        superviseur.demarrer();
        assertThrows(IllegalArgumentException.class, () -> {
            superviseur.getCarteRefutantSoupcon(null);
        });
    }

    @Test
    public void testGetCarteRefuterSoupconPartieNonDemarree(){
        assertThrows(DemarragePartieException.class, ()->{
            superviseur.getCarteRefutantSoupcon(null);
        });
    }



    @Test
    public void testSoupconIdentiqueALEnigme(){
        superviseur.demarrer();

        Hypothese soupconEnigme = new Hypothese(superviseur.getEnigme().getLieu(), superviseur.getEnigme().getPersonnage(), superviseur.getEnigme().getArme());


        assertThrows(SoupconNonRefutableException.class, ()->{
           superviseur.getCarteRefutantSoupcon(soupconEnigme);
        });
    }
}
