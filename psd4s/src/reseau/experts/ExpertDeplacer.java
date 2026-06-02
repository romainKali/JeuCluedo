package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertDeplacer extends Expert{
    @Override
    public boolean saitTraiter(String message) {return message.matches(ConnexionUtilisateur.regexALLER_VERS);}

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        String[] tabMots = message.split(" ");
        String pos_x =  tabMots[1];
        String pos_y = tabMots[2];

        utilisateur.getServeur().traiterDeplacement(utilisateur, pos_x, pos_y);
    }
}
