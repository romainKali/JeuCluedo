package reseau.experts.soupcon;

import bdd.DaoCarnet;
import enums.Carte;
import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertCarteRefutante extends Expert {

    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_CARTE_REFUTANT_SOUPCON);
    }


    @Override
    public void executerTraitement(String message, InterfaceMain main) {
        // Format attendu : @CARTE_REFUTANT_SOUPCON P1 Bob
        String[] partStrings = message.split(" ");

        String carteCode = partStrings[1]; // Ex: "P1"
        String joueurNom = partStrings[2]; // Ex: "Bob"
        String localNom = main.getNom();   // celui qui a posé le soupçon

        // On convertit le code ("P1") en objet Carte (Colonel_Moutarde)
        Carte carteRevelee = InterfaceMain.stringToCarte(carteCode);

        if (carteRevelee != null) {
            // On affiche le message dans la console du jeu
            main.printConsole(joueurNom + " vous a montré la carte : " + carteRevelee.name(), null);

            // On note la coche verte (1) dans la base de données
            // Il est crucial d'utiliser carteRevelee.name() pour que la BDD reconnaisse la carte
            DaoCarnet.noterIndice(localNom, joueurNom, carteRevelee.name(), 1);
        }

        // On réinitialise le soupçon local
        main.resetSoupconEtRefutage();
    }
}