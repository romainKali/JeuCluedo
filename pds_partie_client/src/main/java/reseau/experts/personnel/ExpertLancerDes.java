package reseau.experts.personnel;

import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertLancerDes extends Expert{

    @Override
    public boolean saitTraiter(String message) { 
    	return message.matches(InterfaceMain.REGEX_LANCER_DES_REUSSI);
    }


	@Override
	public void executerTraitement(String message, InterfaceMain main) {
		String[] partStrings = message.split(" ");
		
		main.lancerdes(Integer.parseInt(partStrings[1]));
	}
}
