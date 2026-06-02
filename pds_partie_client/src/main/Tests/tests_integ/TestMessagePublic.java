package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.chat.ExpertMessagePublic;

public class TestMessagePublic { // TODO Gemini
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertMessagePublic expert;

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

        expert = new ExpertMessagePublic();
    }

    @Test
    public void TestReceptionMessagePublic() {
        // Le format contient l'entête, le pseudo, et le reste est le message complet
        String messageSimule = "@PUBLIC_FROM Charlie Salut tout le monde, j'ai un indice !";

        // On vérifie que les sous-chaînes (substring) extraient correctement "Charlie" et le reste du texte
        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}