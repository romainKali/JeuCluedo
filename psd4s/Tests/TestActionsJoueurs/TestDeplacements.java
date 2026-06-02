package TestActionsJoueurs;


import exception.*;
import metier.Jeu.*;
import metier.cluedo.carte.*;
import metier.cluedo.plateau.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestDeplacements {
    Superviseur superviseur;
    Joueur Alice;
    Joueur Bob;
    Joueur Charles;
    CaseCluedo c87;
    CaseCluedo c97;


    @BeforeEach
    public void setUp() throws PlateauCluedoException {
        superviseur = Superviseur.getInstance();
        superviseur.resetSuperviseur();
        Alice = new Joueur("Alice", EPersonnage.Mademoiselle_Rose);
        Bob = new Joueur("Bob", EPersonnage.Colonel_Moutarde);
        Charles = new Joueur("Charles", EPersonnage.Reverend_Olive);


        c87 = superviseur.getPlateau().getCase(8,7);
        c97 = superviseur.getPlateau().getCase(9,7);


        superviseur.ajouterJoueur(Alice);
        superviseur.ajouterJoueur(Bob);
        superviseur.ajouterJoueur(Charles);


        superviseur.demarrer();

        /**
         * Alice -> position(8,7) avec 1 déplacement
         * Bob -> position(9,7)
         * */
        Alice.setPosition(c87);
        superviseur.setNbDeplacementsJoueurCourant(1);

        Bob.setPosition(c97);
    }


    @Test
    public void testDeplacementAliceSurBob(){
        assertThrows(DeplacementJoueurException.class, ()->{
            superviseur.deplacer(superviseur.getJoueurCourant(), c97);
        }, "Le joueur ne devrait pas être en mesure de pouvoir se déplacer vers une case déjà occupée par un autre joueur");

    }

    @Test
    public void testDeplacementAliceVersCaseNonVoisine(){
        assertThrows(DeplacementJoueurException.class, ()->{
            superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(10,7));
        }, "Le joueur ne devrait pas être en mesure de pouvoir se déplacer vers une case qui ne lui est pas voisine");
    }

    @Test
    public void testDeplacementAliceVersCaseVoisine() throws PlateauCluedoException {
        assertDoesNotThrow(()->{
            superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(7,7));
        }, "Le joueur devrait être en mesure de pouvoir se déplacer vers une case qui lui est voisine");

        // On vérifie que la case (7;7) appartienne bien à un joueur de la liste
        assertTrue(superviseur.estCeQueLaCaseAppartientAUnJoueur(superviseur.getPlateau().getCase(7,7)), "La case (7;7) devrait appartenir à un des joueurs");
        // On vérifie que le joueur qui est sur la case (7;7) est Alice
        assertEquals(Alice, superviseur.donnerJoueurPresentSurLaCase(superviseur.getPlateau().getCase(7,7)), "La case (7;7) devrait appartenir au joueur Alice");
        // On vérifie que la case (8;7) est bien libre
        assertFalse(superviseur.estCeQueLaCaseAppartientAUnJoueur(superviseur.getPlateau().getCase(8,7)), "La case (8;7) ne devrait appartenir à aucun des joueurs");
    }

    @Test
    public void testMAJDuNombreDeDeplacementsAlice() throws PlateauCluedoException {
        superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(7,7));

        // On vérifie que le joueur Alice ne possède plus de déplacement
        assertEquals(0, superviseur.getNbDeplacementsJoueurCourant());


        // On vérifie que le joueur Alice ne peut pas se déplacer
        assertThrows(DeplacementJoueurException.class, ()->{
            superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(8,7));
        }, "Une exception devrait être lancée car le joueur n'a plus assez de déplacements");
    }

    @Test
    public void testPieceAlice_case86() {
        assertDoesNotThrow(()->{
            superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(8,6));
        }, "Le joueur devrait être en mesure de pouvoir se déplacer dans la case (8;6)");

        assertEquals(ELieu.Bibliotheque, superviseur.pieceJoueur(Alice), "Le joueur devrait se trouver dans la bibliotheque");
    }

    @Test
    public void testPieceAlice_case80() {
        assertDoesNotThrow(()->{
            superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(8,0));
        }, "Le joueur devrait être en mesure de pouvoir se déplacer dans la case (8;0)");

        assertEquals(ELieu.Bibliotheque, superviseur.pieceJoueur(Alice), "Le joueur devrait se trouver dans la bibliotheque");
    }

    @Test
    public void testDeplacementAliceVersLeBureau() throws PlateauCluedoException {
        Alice.setPosition(superviseur.getPlateau().getCase(4,6));
        superviseur.setNbDeplacementsJoueurCourant(2);

        superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(1,1));

        // On vérifie que la case (4;6) est maintenant libre
        assertThrows(PositionJoueurException.class, () -> {
            superviseur.donnerJoueurPresentSurLaCase(superviseur.getPlateau().getCase(4,6));
        }, "Une PositionJoueurException devrait être lancé car aucun joueur ne doit se trouver sur la case (4;6)");

        // On vérifie que la case (1;1) est maintenant occupée
        assertTrue(superviseur.estCeQueLaCaseAppartientAUnJoueur(superviseur.getPlateau().getCase(1,1)),
                "Un joueur devrait être présent sur la case (1;1)");

        // On vérifie que la case (1;1) est occupée par Alice
        assertEquals(Alice,
                superviseur.donnerJoueurPresentSurLaCase(superviseur.getPlateau().getCase(1,1)),
                "Le joueur Alice devrait être présent sur la case (1;1)");

        // On vérifie que Alice se trouve dans le Bureau
        assertEquals(ELieu.Bureau, superviseur.pieceJoueur(Alice),
                "Le joueur Alice devrait se trouver dans le bureau");

        // On vérifie qu'il ne reste plus qu'1 déplacement à Alice
        assertEquals(1, superviseur.getNbDeplacementsJoueurCourant(),
                "Alice devrait avoir encore 1 déplacement");
    }

    @Test
    public void testDeplacementAliceDuBureauVersLaCuisineParPassageSecret() throws PlateauCluedoException {
        Alice.setPosition(superviseur.getPlateau().getCase(1,1));
        superviseur.setNbDeplacementsJoueurCourant(1);

        assertDoesNotThrow(() -> {
            superviseur.deplacer(superviseur.getJoueurCourant(), superviseur.getPlateau().getCase(22,22));
        }, "Le joueur devrait pouvoir se déplacer vers la case (22;22) via le passage secret");

        // On vérifie que la case (1;1) est maintenant libre
        assertThrows(PositionJoueurException.class, () -> {
            superviseur.donnerJoueurPresentSurLaCase(superviseur.getPlateau().getCase(1,1));
        }, "Une PositionJoueurException devrait être lancée car aucun joueur ne doit se trouver sur la case (1;1)");

        // On vérifie que la case (22;22) contient bien Alice
        assertEquals(Alice,
                superviseur.donnerJoueurPresentSurLaCase(superviseur.getPlateau().getCase(22,22)),
                "Le joueur Alice devrait être présent sur la case (22;22)");

        // On vérifie que Alice se trouve bien dans la cuisine
        assertEquals(ELieu.Cuisine, superviseur.pieceJoueur(Alice),
                "Le joueur Alice devrait se trouver dans la cuisine");

        // On vérifie que Alice possède 0 déplacement
        assertEquals(0, superviseur.getNbDeplacementsJoueurCourant(),
                "Alice ne devrait plus avoir de déplacement");
    }

    //--------------------Tests Supplémentaires----------------------------
    @Test
    public void testDeplacementAliceVersPositionActuelle() {
        assertThrows(DeplacementJoueurException.class, () -> {
            superviseur.deplacer(superviseur.getJoueurCourant(), c87);
        }, "Le joueur Alice ne devrait pas être en mesure de pouvoir se déplacer vers sa position actuelle");
    }

    @Test
    public void testDeplacementAliceVersUneCaseNAppartenantPasAuPlateau() {
        assertThrows(ReglesException.class, () -> {
            superviseur.deplacer(superviseur.getJoueurCourant(), new CaseCluedo(8, 6));
        }, "Le joueur Alice ne devrait pas être en mesure de pouvoir se déplacer vers une case n'appartenant pas au plateau");
    }
}