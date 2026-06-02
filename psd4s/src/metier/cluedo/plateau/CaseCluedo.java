package metier.cluedo.plateau;

import java.util.ArrayList;

import metier.cluedo.carte.ELieu;

/**
 * Une case du plateau contient le joueur qui s'y trouve (s'il y en a un, sinon c'est null), la piece dans laquelle
 * se trouve la case (si elle se trouve dans une pièce, sinon  c'est un couloir et c'est null). Enfin, une case
 * contient la liste de ses cases voisines.
 * Un couloir contigu à une porte a comme voisin toutes les case de la piece de l'autre coté de la porte
 * Toutes les cases d'une pièce ont comme voisin le couloir de l'autre côté de la porte
 * Si la piece a N portes, les cases de la pièce ont toutes comme voisins les N couloirs de l'autre côté de ces portes
 * Si la pièce a un passage secret, toutes les cases de la pièce ont comme voisins toutes les cases de l'autre pièce
 *
 * En l'état, cette classe n'est sûrement pas complète. Il faut tenir compte du Joueur !
 * C'est à vous de faire le nécessaire...
 */
public class CaseCluedo {
    private int ligne, colonne;
    private ELieu piece = null;
    private ArrayList<CaseCluedo> listeVoisins = new ArrayList<CaseCluedo>();

    public CaseCluedo(int ligne, int colonne) {
        this.ligne = ligne;
        this.colonne = colonne;
    }


    public void ajouterVoisin(CaseCluedo aCase) {
        if (aCase == null) {
            throw new NullPointerException("La case est null");
        }

        if (listeVoisins.contains(aCase)) return;
        listeVoisins.add(aCase);
        aCase.ajouterVoisin(this);
    }

    public boolean estVoisin(CaseCluedo aCase) {
        return listeVoisins.contains(aCase);
    }

    public void setPiece(ELieu lieu) {
        this.piece = lieu;
    }

    public ELieu getPiece() {
        return piece;
    }

    public int getLigne() {
        return ligne;
    }
    public int getColonne() {
        return colonne;
    }

    public ArrayList<CaseCluedo> getVoisinnage() {
        return listeVoisins;
    }

    @Override
    public String toString() {
        return "Case{" +
                "ligne=" + ligne +
                ", colonne=" + colonne +
                '}';
    }
}
