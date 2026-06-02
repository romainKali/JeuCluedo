package reseau.experts.soupcon;

import enums.EArme;
import enums.ELieu;
import enums.EPersonnage;
import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertSoupconner extends Expert {
	
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_SOUPCON_EMIS);
    }



	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		String[] partStrings = message.split(" ");
		
		main.soupconner(partStrings[1],
				(EPersonnage) InterfaceMain.stringToCarte(partStrings[2]),
				(EArme) InterfaceMain.stringToCarte(partStrings[3]),
				(ELieu) InterfaceMain.stringToCarte(partStrings[4])
				);
	}
}
