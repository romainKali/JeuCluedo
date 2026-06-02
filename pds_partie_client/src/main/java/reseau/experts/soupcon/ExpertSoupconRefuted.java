package reseau.experts.soupcon;

import static bdd.DaoCarnet.etatPossessionCarte;

import bdd.DaoCarnet;
import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertSoupconRefuted extends Expert {

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_SOUPCON_REFUTE);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
        String[] partStrings = message.split(" ");

        if (partStrings.length < 2) return;

        String nom_refuteur = partStrings[1];
        String localNom = main.getNom();

        if (main.getSoupcon() == null) {
            main.printerr("Erreur : Impossible de traiter un soupçon inexistant.");
            return;
        }Integer etatArme = etatPossessionCarte(localNom, nom_refuteur, main.getSoupcon().getArme().name());
        Integer etatLieu = etatPossessionCarte(localNom, nom_refuteur, main.getSoupcon().getLieu().name());
        Integer etatPerso = etatPossessionCarte(localNom, nom_refuteur, main.getSoupcon().getPersonnage().name());

        // On vérifie qu'ils ne sont pas nuls AVANT de vérifier s'ils valent 1 ou 0
        boolean armeConnue = (etatArme != null && (etatArme == 1 || etatArme == 0));
        boolean lieuConnu = (etatLieu != null && (etatLieu == 1 || etatLieu == 0));
        boolean persoConnu = (etatPerso != null && (etatPerso == 1 || etatPerso == 0));

        if (!(armeConnue || lieuConnu || persoConnu)) {
            // Le joueur a la carte on marque point interrogation
            DaoCarnet.noterIndice(localNom, nom_refuteur, main.getSoupcon().getPersonnage().name(), 2);
            DaoCarnet.noterIndice(localNom, nom_refuteur, main.getSoupcon().getArme().name(), 2);
            DaoCarnet.noterIndice(localNom, nom_refuteur, main.getSoupcon().getLieu().name(), 2);
        }
        

        main.resetSoupconEtRefutage();

    }

}