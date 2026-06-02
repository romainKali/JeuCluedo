package reseau.experts.erreur;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertErreurParam extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_ERREUR_PARAM);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
        main.printerr(message);
    }
}

