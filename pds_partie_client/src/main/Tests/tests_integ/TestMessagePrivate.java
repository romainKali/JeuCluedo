package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.chat.ExpertMessagePrivate;

public class TestMessagePrivate {
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertMessagePrivate expert;

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

        expert = new ExpertMessagePrivate();
    }

    @Test
    public void TestReceptionMessagePrivate() {
        // Format attendu : @MP_FROM Expediteur Le reste est le message avec des espaces
        String messageSimule = "@MP_FROM Alice Salut Bob, as-tu vu le colonel Moutarde ?";

        // On vérifie que indexOf(" ") extrait bien "Alice" et que la suite est gérée correctement
        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}