package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertFinTour extends Expert{
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexFIN_TOUR);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        utilisateur.getServeur().traiterFinTour(utilisateur);
    }
}
