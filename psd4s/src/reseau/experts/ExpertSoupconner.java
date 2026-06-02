package reseau.experts;

import metier.cluedo.carte.EArme;
import metier.cluedo.carte.EPersonnage;
import reseau.ConnexionUtilisateur;
import reseau.Serveur;

public class ExpertSoupconner extends Expert {

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(ConnexionUtilisateur.regexSOUPCON);
    }

    @Override
    public void executerTraitement(String message, ConnexionUtilisateur utilisateur) {
        String[] tabMots = message.split(" ");
        String personnage = tabMots[1];
        String arme = tabMots[2];

        utilisateur.getServeur().traiterSoupcon(utilisateur,(EPersonnage) Serveur.stringToCarte(personnage), (EArme) Serveur.stringToCarte(arme));
    }
}
