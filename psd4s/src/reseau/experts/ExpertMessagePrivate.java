package reseau.experts;

import reseau.ConnexionUtilisateur;
import reseau.ServeurException;

public class ExpertMessagePrivate extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexMP_TO);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur)  {
    	
    	int firstspace = message.indexOf(" ");
    	int secondspace = message.indexOf(" ", firstspace + 1);
    	
        try {
			utilisateur.getServeur().messagePrive(utilisateur, message.substring(firstspace + 1, secondspace), message.substring(secondspace + 1));
		} catch (ServeurException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}