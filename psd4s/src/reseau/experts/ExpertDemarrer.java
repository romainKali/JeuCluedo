package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertDemarrer extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexDEMARRER);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        utilisateur.getServeur().traiterDemarrer(utilisateur);
    }
}
