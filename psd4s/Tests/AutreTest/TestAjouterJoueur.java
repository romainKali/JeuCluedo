package AutreTest;

import exception.*;
import metier.Jeu.*;
import metier.cluedo.carte.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


public class TestAjouterJoueur {
	Superviseur superviseur;
	Joueur Alice;
    Joueur Bob;
    Joueur Charles;
    Joueur Dylan;
    Joueur Erwan;
    Joueur Fanny;

	
	@BeforeEach
	public void setUpEach() {
        superviseur = Superviseur.getInstance();
        superviseur.resetSuperviseur();


        Alice = new Joueur("Alice", EPersonnage.Colonel_Moutarde);
        Bob = new Joueur("Bob", EPersonnage.Madame_Leblanc);
        Charles = new Joueur("Dylan", EPersonnage.Madame_Pervenche);
        Dylan = new Joueur("Samuel",EPersonnage.Mademoiselle_Rose);
        Erwan = new Joueur("J5", EPersonnage.Professeur_Violet);
        Fanny = new Joueur("J6", EPersonnage.Reverend_Olive);
	}
	
	@Test
	public void testAjoutDUnJoueur() {
		assertDoesNotThrow( () -> {
			superviseur.ajouterJoueur(Alice);
		});
		
		assertEquals(1, superviseur.getNbJoueurs());
		assertTrue(superviseur.getListeJoueurs().contains(Alice));
	}

    @Test
    public void testAjoutDUnPointeurNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            superviseur.ajouterJoueur(null);
        });
    }

    @Test
    public void testAjoutDUnJoueurAlorsQueLaPartieEstDejaCommencee() {
        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);
        superviseur.ajouterJoueur(Charles);

        superviseur.demarrer();

        assertThrows(DemarragePartieException.class, ()->{
            superviseur.ajouterJoueur(Dylan);
        });
    }

	
	@Test
	public void testAjoutDUnMemeJoueur() {
		superviseur.ajouterJoueur(Alice);
		
		assertThrows(JoueurException.class, () -> {
			superviseur.ajouterJoueur(Alice);
		});
	}
	
	@Test
	public void testAjoutDUn7emeJoueur() {
		superviseur.ajouterJoueur(Alice);
		superviseur.ajouterJoueur(Bob);
		superviseur.ajouterJoueur(Charles);
		superviseur.ajouterJoueur(Dylan);
		superviseur.ajouterJoueur(Erwan);
		superviseur.ajouterJoueur(Fanny);
		
		assertThrows(JoueurException.class, () -> {
			superviseur.ajouterJoueur(new Joueur("Garance", EPersonnage.Colonel_Moutarde));
		});
	}
	
	@Test
	public void testAjoutDUnJoueurAyantLeMemeNomQUnAutreJoueurDeListeJoueurs() {
		superviseur.ajouterJoueur(Alice);
		
		assertThrows(JoueurException.class, () -> {
			superviseur.ajouterJoueur(new Joueur("Alice", EPersonnage.Mademoiselle_Rose));
		});
	}
	
	
	@Test
	public void testAjoutDUnJoueurAyantLeMemePersonnageQUnAutreJoueurDeListeJoueurs() {
		superviseur.ajouterJoueur(Bob);
		
		assertThrows(JoueurException.class,() -> {
			superviseur.ajouterJoueur(new Joueur("Alice", EPersonnage.Madame_Leblanc));
		});
	}
}