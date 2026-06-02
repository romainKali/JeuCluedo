package reseau.experts.notifications;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertVictoire extends Expert{

    @Override
    public boolean saitTraiter(String message) {
    	return message.matches(InterfaceMain.REGEX_VICTOIRE);
    	}

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		String[] partStrings = message.split(" ");
		main.victoire(partStrings[1]);
	}
}
