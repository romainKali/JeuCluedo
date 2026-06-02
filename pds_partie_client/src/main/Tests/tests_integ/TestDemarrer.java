package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.general.ExpertDemarrer;

public class TestDemarrer {
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertDemarrer expert;

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

        expert = new ExpertDemarrer();
    }

    @Test
    public void TestReceptionDemarrer() {
        // Le serveur notifie que la partie commence, à qui c'est le tour,
        // puis la liste des pions avec leurs coordonnées X Y
        String messageSimule = "@PARTIE_COMMENCEE Bob P1 0 0 P2 5 7 P3 1 2";

        // On vérifie que la boucle de l'expert parcourt bien les coordonnées par pas de 3
        // et qu'aucune exception n'est levée (ex: IndexOutOfBoundsException ou NumberFormatException)
        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}