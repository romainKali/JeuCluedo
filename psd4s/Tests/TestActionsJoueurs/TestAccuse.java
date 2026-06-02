package TestActionsJoueurs;

import exception.*;
import metier.Jeu.*;
import metier.cluedo.carte.*;
import metier.cluedo.carte.EPersonnage;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestAccuse {
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
    public void testLancerAccusationAvantDemarrageDeLaPartie() {
        assertThrows(DemarragePartieException.class, () -> {
            superviseur.accuser(Alice, EPersonnage.Colonel_Moutarde, EArme.Chandelier, ELieu.Bibliotheque);
        });
    }


    @Test
    public void testLancerAcusationAvecJoueurNonCourant() {
        superviseur.demarrer();

        assertThrows(JoueurException.class, () -> {
            superviseur.accuser(Bob, EPersonnage.Colonel_Moutarde, EArme.Chandelier, ELieu.Bibliotheque);
        });

    }


    @Test
    public void testLancerAccusationAvecUnPointeurNullCommeJoueur() {
        superviseur.demarrer();

        assertThrows(IllegalArgumentException.class, () -> {
            superviseur.accuser(null, EPersonnage.Colonel_Moutarde, EArme.Chandelier, ELieu.Bibliotheque);
        });
    }


    @Test
	public void testAccusationCorrecte() {
		superviseur.demarrer();
		
		Hypothese enigme = superviseur.getEnigme();

		assertThrows(VictoireException.class, () -> {
		    superviseur.accuser(Alice, enigme.getPersonnage(), enigme.getArme(), enigme.getLieu());
		} );

		assertFalse(superviseur.getPartieCommencee());
		assertNull(superviseur.getEnigme());
	}



    @Test
    public void testAccusationAvecPointeurNullDansPersonnage() {
        superviseur.demarrer();

        assertThrows(IllegalArgumentException.class, () -> {
            superviseur.accuser(Alice, null, EArme.Chandelier, ELieu.Bibliotheque);
        });
    }

	
	@Test
	public void testAccusationAvecPointeurNullDansArme() {
		superviseur.demarrer();
		
		assertThrows(IllegalArgumentException.class, () -> {
			superviseur.accuser(Alice, EPersonnage.Colonel_Moutarde, null, ELieu.Bibliotheque);
		});
	}


	@Test
	public void testAccusationAvecPointeurNullDansLieu() {
		superviseur.demarrer();
		
		assertThrows(IllegalArgumentException.class, () -> {
			superviseur.accuser(Alice, EPersonnage.Colonel_Moutarde, EArme.Chandelier, null);
		});
	}
}