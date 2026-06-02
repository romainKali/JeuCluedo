package reseau.experts.general;

import enums.EPersonnage;
import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertConnexion extends Expert {
	

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_CONNEXION_REUSSI_G);
    }

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		String[] partStrings = message.split(" ");
		main.addJoueur(partStrings[1], (EPersonnage) InterfaceMain.stringToCarte(partStrings[2]) );
	}

}
