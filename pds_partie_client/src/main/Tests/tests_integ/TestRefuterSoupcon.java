package tests_integ;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ihm.InterfaceMain;
import javafx.application.Platform;
import reseau.experts.soupcon.ExpertCarteRefutante;

public class TestRefuterSoupcon {
    InterfaceMain main;
    FakeThreadReseau reseau;
    ExpertCarteRefutante expert;

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
        main.setNom("MoiLocal"); // Nécessaire pour DaoCarnet.noterIndice (localNom)

        expert = new ExpertCarteRefutante();
    }

    @Test
    public void TestRefutationAvecCarte() {
        // Un joueur montre une carte spécifique pour réfuter
        String messageSimule = "@SOUPCON_REFUTE Bob Corde";

        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }

    @Test
    public void TestRefutationSansCarte() {
        // Le joueur ne peut pas réfuter (il envoie "null")
        // Ici main.getSoupcon() sera null, ce qui est censé être géré par l'expert avec l'affichage d'une erreur
        String messageSimule = "@SOUPCON_REFUTE Bob null";

        assertDoesNotThrow(() -> {
            expert.executerTraitement(messageSimule, main);
        });
    }
}