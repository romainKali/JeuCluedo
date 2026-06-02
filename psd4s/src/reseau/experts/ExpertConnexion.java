package reseau.experts;



import metier.cluedo.carte.EPersonnage;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;

public class ExpertConnexion extends Expert {


    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexCONNEXION);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        String[] tabMessage = message.split(" ");

        String pseudo = tabMessage[1];
        EPersonnage personnage = (EPersonnage) Serveur.stringToCarte(tabMessage[2]);

        utilisateur.getServeur().traiterConnexion(utilisateur, pseudo, personnage);
    }
}
