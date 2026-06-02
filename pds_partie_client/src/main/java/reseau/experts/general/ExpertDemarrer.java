package reseau.experts.general;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertDemarrer extends Expert {
	

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_PARTIE_COMMENCEE);
    }

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		
		String[] partString = message.split(" ");
		
		for (int i = 2; i < partString.length; i += 3) {
			main.initPion(partString[i] ,Integer.parseInt( partString[i+1]) , Integer.parseInt(partString[i+2]));
		}
		main.demarrerPartie(partString[1]);
	}

}
