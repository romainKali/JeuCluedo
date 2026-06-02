package reseau.experts.notifications;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertElim extends Expert {

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_JOUEUR_ELIM);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
    	String[] parts = message.split(" ");
    	
        main.traiterElimination(parts[1]);
    }
}
