package TestActionsJoueurs;

import exception.DemarragePartieException;
import exception.JoueurException;
import exception.PositionJoueurException;
import exception.ReglesException;
import metier.Jeu.Joueur;
import metier.Jeu.Superviseur;
import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestSoupcon {
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


    /**
     * ----------------------------------- PARTIE-SOUPCON -----------------------------------
     * RQ : Les tests dédiés à la réfutation sont dans un autre fichier
     * */


    @Test
    public void testLancerSoupconAvecJoueurCourantAlorsQueLaPartieNAPasCommencee(){
        assertThrows(DemarragePartieException.class, ()->{
           superviseur.soupcon(superviseur.getJoueurCourant(), EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        });
    }

    @Test
    public void testVerificationSoupconEgalNullQuandAucunSoupconNAEteEmis(){
        superviseur.demarrer();

        assertNull(superviseur.getSoupconEnCours(), "Le soupcon en cours devrait être null puisque aucun joueur n'a lancé de soupcon");
    }

    @Test
    public void testLancerSoupconAvecJoueurNonCourantAlorsQueLaPartieACommencee(){
        superviseur.demarrer();
        assertThrows(JoueurException.class, ()->{
            superviseur.soupcon(Bob, EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        });
    }

    @Test
    public void testLancerSoupconAvecJoueurCourantNeSeTrouvantPasDansUnePiece(){
        superviseur.demarrer();
        assertThrows(PositionJoueurException.class, ()->{
            superviseur.soupcon(superviseur.getJoueurCourant(), EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        });
    }

    @Test
    public void testVerificationPeutSoupconnerJoueurCourant(){
        superviseur.demarrer();

        Joueur joueurCourant = superviseur.getJoueurCourant();

        assertFalse(joueurCourant.getPeutSoupconner(), "Le joueur courant ne devrait pas être en mesure de pouvoir émettre un soupcon");

        // Le déplacement du joueurCourant dans une pièce met à jour PeutSoupconner
        joueurCourant.setPosition(superviseur.getPlateau().getCase(4,6));
        superviseur.setNbDeplacementsJoueurCourant(1);
        superviseur.deplacer(joueurCourant, superviseur.getPlateau().getCase(1,1));

        assertTrue(joueurCourant.getPeutSoupconner(), "Le joueur courant devrait être en mesure de pouvoir soupconner");
    }



    @Test
    public void testLancerSoupconAvecJoueurCourantDansUnePiece(){
        superviseur.demarrer();

        Joueur joueurCourant = superviseur.getJoueurCourant();
        joueurCourant.setPosition(superviseur.getPlateau().getCase(4,6));
        superviseur.setNbDeplacementsJoueurCourant(1);
        superviseur.deplacer(joueurCourant, superviseur.getPlateau().getCase(1,1));

        assertDoesNotThrow(()->{
           superviseur.soupcon(joueurCourant, EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        });

        assertEquals(EPersonnage.Mademoiselle_Rose, superviseur.getSoupconEnCours().getPersonnage(), "Le personnage devrait être celui du soupcon émis");
        assertEquals(ELieu.Bureau, superviseur.getSoupconEnCours().getLieu(), "Le lieu devrait être celui du soupcon émis");
        assertEquals(EArme.Poignard, superviseur.getSoupconEnCours().getArme(), "L'arme devrait être celle du soupcon émis");
    }


    @Test
    public void testVerificationLancementDUnDeuxiemeSoupconImpossible(){
        superviseur.demarrer();

        Joueur joueurCourant = superviseur.getJoueurCourant();
        joueurCourant.setPeutSoupconner(true);
        joueurCourant.setPosition(superviseur.getPlateau().getCase(1, 1));

        superviseur.soupcon(joueurCourant, EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        assertThrows(ReglesException.class, ()->{
            superviseur.soupcon(joueurCourant, EPersonnage.Colonel_Moutarde, EArme.Poignard);
        });
    }


    @Test
    public void testLancerUnAutreSoupconApresLaRefutationDuPremierImpossible(){
        superviseur.demarrer();

        Joueur joueurCourant = superviseur.getJoueurCourant();
        joueurCourant.setPeutSoupconner(true);
        joueurCourant.setPosition(superviseur.getPlateau().getCase(1, 1));

        superviseur.soupcon(joueurCourant, EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        superviseur.terminerRefutation();
        assertThrows(ReglesException.class, ()->{
            superviseur.soupcon(joueurCourant, EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        });
    }

    @Test
    public void testLancerUnAutreSoupconApresUnDeplacement(){
        superviseur.demarrer();

        Joueur joueurCourant = superviseur.getJoueurCourant();
        joueurCourant.setPeutSoupconner(true);
        joueurCourant.setPosition(superviseur.getPlateau().getCase(1, 1));

        superviseur.soupcon(joueurCourant, EPersonnage.Mademoiselle_Rose, EArme.Poignard);
        superviseur.terminerRefutation();
        superviseur.setNbDeplacementsJoueurCourant(1);
        superviseur.deplacer(joueurCourant, superviseur.getPlateau().getCase(22,22));

        assertDoesNotThrow(() -> {
            superviseur.soupcon(joueurCourant, EPersonnage.Colonel_Moutarde, EArme.Poignard);
        });
    }





}
