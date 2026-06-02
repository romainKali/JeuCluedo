package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.soupcon.ExpertSoupconner;

public class TestSoupcon { // TODO gemini
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertSoupconner expert;

    @BeforeEach
    public void initAll() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit JavaFX déjà initialisé
        }

        main = new InterfaceMain();
        reseau = new FakeThreadReseau(main);
        main.setReseau(reseau);

        expert = new ExpertSoupconner();
    }

    @Test
    public void TestReceptionSoupcon() {
        // Le serveur notifie qu'un soupçon a été émis.
        // Format attendu par ton split() : COMMANDE Joueur Personnage Arme Lieu
        String messageSimule = "@SOUPCON_EMIS Bob P1 A3 L5";

        // On vérifie que la conversion stringToCarte se fait bien et que la méthode soupconner() est appelée
        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}