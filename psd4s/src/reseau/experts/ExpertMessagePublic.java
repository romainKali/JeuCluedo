package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertMessagePublic extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexTO_ALL);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        utilisateur.getServeur().messagePublic(utilisateur,  message.substring(message.indexOf(" ") + 1));
    }
}