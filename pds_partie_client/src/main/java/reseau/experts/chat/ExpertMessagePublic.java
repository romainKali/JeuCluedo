package reseau.experts.chat;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertMessagePublic extends Expert {

	@Override
	public boolean saitTraiter(String message) {
		return message.matches(InterfaceMain.REGEX_PUBLIC_FROM);
	}

	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		int firstSpace = message.indexOf(" ");
		int secondSpace = message.indexOf(" ", firstSpace + 1);
		
		main.publicMessage(message.substring(firstSpace + 1, secondSpace), message.substring(secondSpace + 1));
		
	}

}