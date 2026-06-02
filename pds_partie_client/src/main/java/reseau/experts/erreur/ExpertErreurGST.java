package reseau.experts.erreur;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertErreurGST extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_ERREUR_GST_PARTIE);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
    main.handleErreurGST(message);
    }
}