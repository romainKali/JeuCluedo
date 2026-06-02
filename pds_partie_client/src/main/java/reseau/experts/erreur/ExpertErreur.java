package reseau.experts.erreur;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertErreur extends Expert {
	
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_ERREUR);
    }

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		main.printerr(message.substring(8));
	}
	}
