package reseau.experts.chat;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertMessagePrivate extends Expert {

	@Override
	public boolean saitTraiter(String message) {
		return message.matches(InterfaceMain.REGEX_MP_FROM);
	}

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		
		int firstSpace = message.indexOf(" ");
		int secondSpace = message.indexOf(" ", firstSpace + 1);
		
		main.privateMessage(message.substring(firstSpace + 1, secondSpace), message.substring(secondSpace + 1));
	}

}