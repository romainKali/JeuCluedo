package reseau.experts.soupcon;

import ihm.InterfaceMain;
import javafx.scene.paint.Color;
import reseau.experts.Expert;

public class ExpertInformationRefutation extends Expert {

    @Override
    public boolean saitTraiter(String message) {
        // Intercepte le message envoyé par le serveur
        return message.startsWith("@INFORMATION_REFUTATION");
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
        // On isole le texte après la balise (qui fait 24 caractères avec l'espace)
        String info = message.substring(24);

            // 1. On affiche l'information dans la console du jeu pour prévenir tout le monde
            main.printConsole(info, Color.ORANGE);

            // 2. LA LIGNE MAGIQUE : on réinitialise le soupçon pour débloquer l'interface !
            main.resetSoupconEtRefutage();
    }
}