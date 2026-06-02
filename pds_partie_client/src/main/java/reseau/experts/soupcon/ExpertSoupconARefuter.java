package reseau.experts.soupcon;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertSoupconARefuter extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_SOUPCON_A_REFUTER);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
        main.soupconARefuter();
    }
}
