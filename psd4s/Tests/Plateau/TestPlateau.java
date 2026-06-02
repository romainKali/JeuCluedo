package Plateau;

import metier.cluedo.carte.*;
import exception.*;

import metier.cluedo.plateau.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestPlateau {
    private PlateauCluedo plateau;

    @BeforeEach
    public void setUp() throws PlateauCluedoException {
        plateau = new PlateauCluedo();
    }

    @Test
    public void testInitialisationStructurePrincipaleDuPlateau() throws PlateauCluedoException {

        assertEquals(ELieu.Bureau, plateau.getCase(0, 0).getPiece(),
                "Le coin haut gauche devrait être le bureau");

        assertEquals(ELieu.Salon,
                plateau.getCase(0, PlateauCluedo.NOMBRE_COLONNES_PLATEAU - 1).getPiece(),
                "Le coin haut droit devrait être le salon");

        assertEquals(ELieu.Veranda,
                plateau.getCase(PlateauCluedo.NOMBRE_LIGNES_PLATEAU - 2, 0).getPiece(),
                "Le coin bas gauche devrait être la véranda");

        assertEquals(ELieu.Cuisine,
                plateau.getCase(PlateauCluedo.NOMBRE_LIGNES_PLATEAU - 2, PlateauCluedo.NOMBRE_COLONNES_PLATEAU - 1).getPiece(),
                "Le coin bas droit devrait être la cuisine");

        assertNull(plateau.getCase(4, 6).getPiece(),
                "La porte doit être une case de couloir");

        assertTrue(plateau.getCase(4, 6).estVoisin(plateau.getCase(3, 6)),
                "La porte doit être voisine de la pièce");

        assertTrue(plateau.getCase(3, 0).estVoisin(plateau.getCase(
                                PlateauCluedo.NOMBRE_LIGNES_PLATEAU - 2,
                                PlateauCluedo.NOMBRE_COLONNES_PLATEAU - 1)),
                "Le passage secret entre le bureau et la cuisine devrait exister");
    }

    @Test
    public void testPassageSecretEntreBureauEtCuisine() throws PlateauCluedoException {

        CaseCluedo caseBureau = plateau.getCase(0, 0);
        CaseCluedo caseCuisine = plateau.getCase(23, 23);

        assertTrue(caseBureau.getVoisinnage().contains(caseCuisine), "Le bureau devrait être relié à la cuisine par un passage secret");

        assertTrue(caseCuisine.getVoisinnage().contains(caseBureau), "La cuisine devrait être reliée au bureau par un passage secret");
    }

    @Test
    public void testMurEntrePieceEtCouloirBloquantLePassage() throws PlateauCluedoException {

        CaseCluedo caseBureau = plateau.getCase(3, 1);
        CaseCluedo caseCouloir = plateau.getCase(4, 1);

        assertEquals(ELieu.Bureau, caseBureau.getPiece(), "La case devrait appartenir au bureau");

        assertNull(caseCouloir.getPiece(), "La case devrait être un couloir");

        assertFalse(caseCouloir.getVoisinnage().contains(caseBureau), "Le couloir ne doit pas être voisin du bureau à travers un mur");
    }

    @Test
    public void testCoordonneesInvalides() {

        assertThrows(PlateauCluedoException.class, () -> plateau.getCase(25, 0), "Une ligne hors plateau devrait lever une exception");

        assertThrows(PlateauCluedoException.class, () -> plateau.getCase(-1, 0), "Une ligne négative devrait lever une exception");
    }
}
