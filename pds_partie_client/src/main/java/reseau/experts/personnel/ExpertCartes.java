package reseau.experts.personnel;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertCartes extends Expert{

    @Override
    public boolean saitTraiter(String message) { 
    	return message.matches(InterfaceMain.REGEX_VOS_CARTES);
    }


	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		main.initCartes(message.substring(message.indexOf(" ") + 1 ));
	}
}
