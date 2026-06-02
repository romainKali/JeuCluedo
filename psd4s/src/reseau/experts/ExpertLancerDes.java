package reseau.experts;

import reseau.ConnexionUtilisateur;

public class ExpertLancerDes extends Expert{
    @Override
    public boolean saitTraiter(String message) { return message.matches(ConnexionUtilisateur.regexLANCER_DES); }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        utilisateur.getServeur().traiterLancerDes(utilisateur);
    }
}
