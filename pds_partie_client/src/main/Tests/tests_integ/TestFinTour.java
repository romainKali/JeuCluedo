package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.notifications.ExpertFinTour;

public class TestFinTour {
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertFinTour expert;

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

        expert = new ExpertFinTour();
    }

    @Test
    public void TestReceptionFinTour() {
        // Le message contient la commande et le nom du prochain joueur
        String messageSimule = "@FIN_TOUR Charlie";

        // On vérifie le substring qui passe le pseudo du joueur suivant
        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}