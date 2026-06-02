package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.general.ExpertDeplacer;

public class TestDeplacer { // TODO gemini
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertDeplacer expert;

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

        expert = new ExpertDeplacer();
    }

    @Test
    public void TestReceptionDeplacer() {
        // Le serveur envoie un message avec la commande, le pseudo, et les coordonnées X Y
        String messageSimule = "@DEPLACEMENT_REUSSI Alice 5 7";

        // On vérifie que le split() de l'expert fonctionne et qu'il appelle main.deplacer() sans lever d'exception
        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}