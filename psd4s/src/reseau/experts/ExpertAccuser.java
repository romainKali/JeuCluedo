package reseau.experts;


import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;

public class ExpertAccuser extends Expert{

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexACCUSE);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        String[] tabMots = message.split(" ");

        utilisateur.getServeur().traiterAccuser(utilisateur,(EPersonnage) Serveur.stringToCarte( tabMots[1]),
        		(EArme)Serveur.stringToCarte( tabMots[2]),
        		(ELieu)Serveur.stringToCarte( tabMots[3])
        		);
    }
}
