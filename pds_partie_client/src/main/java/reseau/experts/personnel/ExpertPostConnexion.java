package reseau.experts.personnel;


import enums.EPersonnage;
import ihm.InterfaceMain;
import reseau.experts.Expert;

public class ExpertPostConnexion extends Expert {
    @Override
    public boolean saitTraiter(String message) {
        return message.matches(InterfaceMain.REGEX_POSTCON);
    }

    @Override
    public void executerTraitement(String message, InterfaceMain main) {
        String[] partStrings = message.split(" ");

        for (int i = 1 ; i < partStrings.length ; i += 2) {
            main.addJoueur(partStrings[i], (EPersonnage) InterfaceMain.stringToCarte(partStrings[i + 1]) );
        }
    }
}
