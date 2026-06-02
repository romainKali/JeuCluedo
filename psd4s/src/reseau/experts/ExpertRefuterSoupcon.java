package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertRefuterSoupcon extends Expert{	
	
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexINDICE);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        String[] tabMots = message.split(" ");

        if (tabMots[1].equals("null")){
            tabMots[1] = null;
        }

        utilisateur.getServeur().traiterRefuterSoupcon(utilisateur, tabMots[1]);
    }
}
