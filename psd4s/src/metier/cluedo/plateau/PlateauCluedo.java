package metier.cluedo.plateau;

import exception.PlateauCluedoException;
import metier.cluedo.carte.ELieu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * C'est un tableau à 2 dimensions qui représente la grille de jeu de Cluedo.
 * Cette grille est remplie à partir d'un fichier CSV. D'abord, on lit ce fichier CSV pour remplir un tableau 2D de
 * String (le champ s). Puis, on construit les CaseCluedo (champ t) en lisant le tableau s.
 */
public class PlateauCluedo {
    public static final int NOMBRE_LIGNES_PLATEAU = 25;
    public static final int NOMBRE_COLONNES_PLATEAU = 24;
    private final CaseCluedo[][] t = new CaseCluedo[NOMBRE_LIGNES_PLATEAU][NOMBRE_COLONNES_PLATEAU];
    private final String[][] s = new String[NOMBRE_LIGNES_PLATEAU][NOMBRE_COLONNES_PLATEAU];

    /**
     * C'est le point de départ. On lit le fichier en entier, puis on traite le voisinnage de chaque case
     * @throws PlateauCluedoException
     */
    public PlateauCluedo() throws PlateauCluedoException {
        initialiserPlateauAvecDesCasesVides();
        parserFichier();
        traiterVoisinnageCases();
    }

    private void initialiserPlateauAvecDesCasesVides() {
        for (int ligne = 0; ligne< NOMBRE_LIGNES_PLATEAU; ligne++) {
            for(int colonne = 0; colonne< NOMBRE_COLONNES_PLATEAU; colonne++) {
                t[ligne][colonne] = new CaseCluedo(ligne, colonne);
            }
        }
    }

    /**
     * On détermine la nature d'une case selon la String trouvée dans s :
     * - si s[l][c] est vide, c'est un couloir
     * - si s[l][c] ne contient qu'un seul caractère, c'est une pièce
     * - si s[l][c] contient 3 caractères, c'est soit un passage entre le couloir et une pièce, soit un passage secret
     * entre 2 pièces
     * @throws PlateauCluedoException
     */
    private void traiterVoisinnageCases() throws PlateauCluedoException {
        for (int ligne = 0; ligne< NOMBRE_LIGNES_PLATEAU; ligne++) {
            for (int colonne = 0; colonne < NOMBRE_COLONNES_PLATEAU; colonne++) {
                if (s[ligne][colonne].isEmpty())
                    traiterCouloir(ligne, colonne);
                else if (s[ligne][colonne].length() == 3)
                    traiterVoisinnagePassage(s[ligne][colonne], ligne, colonne);
                else if (s[ligne][colonne].length() != 1)
                    throw new PlateauCluedoException("La case (" + ligne + "," + colonne + ") est mal remplie : " + s[ligne][colonne]);
            }
        }
    }

    private void parserFichier() throws PlateauCluedoException {
        InputStream stream = getClass().getResourceAsStream("/GrilleCluedo.csv");

        if (stream == null)
            throw new PlateauCluedoException("Le fichier de plateau " + "/GrilleCluedo.csv" + " est introuvable. Vérifiez le dossier 'resources' du projet");

        InputStreamReader isr = new InputStreamReader(stream);

        try (BufferedReader br = new BufferedReader(isr)) { // try-with-resources --> approche moderne qui évite une clause finally avec un try-catch à l'intérieur
            for (int ligne = 0; ligne < NOMBRE_LIGNES_PLATEAU; ligne++) {
                String strLigne = br.readLine();
                String[] strCases = strLigne.split(";", -1);
                if (strCases.length != NOMBRE_COLONNES_PLATEAU)
                    throw new PlateauCluedoException("Le nombre de cases dans la ligne " + ligne + " incorrecte : " + strCases.length);
                for (int colonne = 0; colonne < NOMBRE_COLONNES_PLATEAU; colonne++) {
                    s[ligne][colonne] = strCases[colonne];
                    if (!strCases[colonne].isEmpty()) { // la case appartient à une piece, donc on initialise tout de suite la piece
                        int p = strCases[colonne].charAt(0) - '0';
                        if (p != 0) {
                            ELieu lieu = ELieu.values()[p - 1];
                            t[ligne][colonne].setPiece(lieu);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new PlateauCluedoException(e.getMessage());
        }
    }

    private void traiterVoisinnagePassage(String strCase, int ligne, int colonne) {
        if (strCase.endsWith("H"))
            ajouterVoisinPiece(t[ligne][colonne],t[ligne-1][colonne]);
        else if (strCase.endsWith("B"))
            ajouterVoisinPiece(t[ligne][colonne],t[ligne+1][colonne]);
        else if (strCase.endsWith("G"))
            ajouterVoisinPiece(t[ligne][colonne],t[ligne][colonne-1]);
        else if (strCase.endsWith("D"))
            ajouterVoisinPiece(t[ligne][colonne],t[ligne][colonne+1]);
        else {
            // Passage secret de xPy. Il faut chercher la case yPx
            int numPieceX = strCase.charAt(0)-'0';
            ELieu lieuX = ELieu.values()[numPieceX-1];
            int numPieceY = strCase.charAt(2)-'0';
            ELieu lieuY = ELieu.values()[numPieceY-1];
            ajouterToutesLesCasesDUnePieceCommeVoisinsDesCasesDUneAutrePiece(lieuX,lieuY);
            // On fera l'inverse automatiquement lorsque l'on tombera sur yPx
        }
    }

    private void ajouterToutesLesCasesDUnePieceCommeVoisinsDesCasesDUneAutrePiece(ELieu lieuA, ELieu lieuB) {
        for (int ligne = 0; ligne< NOMBRE_LIGNES_PLATEAU; ligne++) {
            for (int colonne = 0; colonne < NOMBRE_COLONNES_PLATEAU; colonne++) {
                if (lieuA.equals(t[ligne][colonne].getPiece()))
                    ajouterUneCaseDUnePieceCommeVoisinDesCasesDUneAutrePiece(t[ligne][colonne],lieuB);
            }
        }
    }

    private void ajouterUneCaseDUnePieceCommeVoisinDesCasesDUneAutrePiece(CaseCluedo caseDeLieuA, ELieu lieuB) {
        for (int ligne = 0; ligne< NOMBRE_LIGNES_PLATEAU; ligne++) {
            for (int colonne = 0; colonne < NOMBRE_COLONNES_PLATEAU; colonne++) {
                if (lieuB.equals(t[ligne][colonne].getPiece()))
                    t[ligne][colonne].ajouterVoisin(caseDeLieuA);
            }
        }
    }

    private void ajouterVoisinPiece(CaseCluedo casePiece, CaseCluedo caseVoisine) {
        // en fait, la case voisine est à ajouter à toutes les cases de la pièce
        for (int ligne = 0; ligne < NOMBRE_LIGNES_PLATEAU; ligne++) {
            for (int colonne = 0; colonne < NOMBRE_COLONNES_PLATEAU; colonne++) {
                if (t[ligne][colonne].getPiece()==casePiece.getPiece()) {
                    t[ligne][colonne].ajouterVoisin(caseVoisine);
                }
            }
        }
    }


    private void traiterCouloir(int ligne, int colonne) {
        if (ligne-1 >= 0 && (s[ligne-1][colonne].isEmpty() || s[ligne-1][colonne].endsWith("PB")))
            t[ligne][colonne].ajouterVoisin(t[ligne-1][colonne]);

        if (ligne+1 < NOMBRE_LIGNES_PLATEAU && (s[ligne+1][colonne].isEmpty() || s[ligne+1][colonne].endsWith("PH")))
            t[ligne][colonne].ajouterVoisin(t[ligne+1][colonne]);

        if (colonne-1 >= 0 && (s[ligne][colonne-1].isEmpty() || s[ligne][colonne+1].endsWith("PD")))
            t[ligne][colonne].ajouterVoisin(t[ligne][colonne-1]);

        if (colonne+1 < NOMBRE_COLONNES_PLATEAU && (s[ligne][colonne+1].isEmpty()  || s[ligne][colonne-1].endsWith("PG")))
            t[ligne][colonne].ajouterVoisin(t[ligne][colonne+1]);
    }


    public CaseCluedo getCase(int ligne, int colonne) throws PlateauCluedoException {
        if (ligne < 0 || ligne >= NOMBRE_LIGNES_PLATEAU || colonne < 0 || colonne >= NOMBRE_COLONNES_PLATEAU)
            throw new PlateauCluedoException("Coordonnées en dehors du plateau...");
        return t[ligne][colonne];
    }
}
