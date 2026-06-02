package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertDeconnexion extends Expert{

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexDECONNEXION);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        utilisateur.getServeur().traiterDeconnexion(utilisateur);
    }
}
