package reseau.experts.notifications;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertFinTour extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_FIN_TOUR);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
    	String[] parts = message.split(" ");
    	
       main.prochainTour(parts[1]);
    }
}
