package reseau.experts.notifications;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertDefaite extends Expert{

    @Override
    public boolean saitTraiter(String message) {
    	return message.matches(InterfaceMain.REGEX_DEFAITE);
    	}

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		main.defaite();
	}
}
