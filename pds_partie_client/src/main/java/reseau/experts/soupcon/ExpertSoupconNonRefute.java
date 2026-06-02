package reseau.experts.soupcon;

import bdd.DaoCarnet;
import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertSoupconNonRefute extends Expert {
    
	public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_SOUPCON_NON_REFUTE);
    }

	public void executerTraitement(String message, InterfaceMain main) {
        String[] partStrings = message.split(" ");

        if (partStrings.length < 2) return;

        String nom_refuteur = partStrings[1];
        String localNom = main.getNom();

        if (main.getSoupcon() == null) {
            main.printerr("Erreur : Impossible de traiter un soupçon inexistant.");
            return;
        }
        // Le joueur n'a pas la carte, on note les 3 croix rouges (0) dans le carnet local
        DaoCarnet.noterIndice(localNom, nom_refuteur, main.getSoupcon().getPersonnage().name(), 0);
        DaoCarnet.noterIndice(localNom, nom_refuteur, main.getSoupcon().getArme().name(), 0);
        DaoCarnet.noterIndice(localNom, nom_refuteur, main.getSoupcon().getLieu().name(), 0);

        // On prévient le joueur sur son interface
        main.printerr(nom_refuteur + " passe (aucune carte). Carnet mis à jour !");
    }
}
