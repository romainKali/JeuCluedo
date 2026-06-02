package reseau.experts.general;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertDeplacer extends Expert{

    @Override
    public boolean saitTraiter(String message) {
    	return message.matches(InterfaceMain.REGEX_DEPLACEMENT_REUSSI);
    	}

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		String[] partStrings = message.split(" ");
		
		main.deplacer(partStrings[1] ,Integer.parseInt(partStrings[2]) , Integer.parseInt(partStrings[3]));
	}
}
