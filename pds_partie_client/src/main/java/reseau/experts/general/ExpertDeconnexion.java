package reseau.experts.general;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertDeconnexion extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_DECONNEXION);
    }

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		String[] partStrings = message.split(" ");
		main.traiterDeconnexion(partStrings[1], false);
	}
}