package metier.Jeu;

import java.util.ArrayList;

import exception.JoueurException;
import metier.cluedo.carte.Carte;
import metier.cluedo.carte.EPersonnage;
import metier.cluedo.plateau.CaseCluedo;


/**
 * Représente un joueur participant à une partie de Cluedo.
 * <p>
 * Un joueur est caractérisé par son nom, le personnage qu'il incarne,
 * sa position sur le plateau, les cartes qu'il possède ainsi que
 * différents états liés au déroulement de la partie
 * (élimination, possibilité de soupçonner).
 * </p>
 * <p>
 * Cette classe centralise également les opérations métier liées
 * à la gestion des cartes du joueur et à la réfutation d'un soupçon.
 * </p>
 */
public class Joueur {
    private String nom;
    private EPersonnage personnage;
    private CaseCluedo position;
    private ArrayList<Carte> cartes = new ArrayList<>();
    private boolean estElimine;
    private boolean peutSoupconner;

    // =========================
    // CONSTRUCTEUR
    // =========================

    public Joueur(String nom, EPersonnage personnage) {
        setNom(nom);
        setPersonnage(personnage);
        setEstElimine(true);
    }

    // =========================
    // GETTERS / SETTERS IDENTITE
    // =========================

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom d'un Joueur ne peut pas etre vide ou null");
        }
        this.nom = nom;
    }

    public EPersonnage getPersonnage() {
        return personnage;
    }

    public void setPersonnage(EPersonnage personnage) {
        if (personnage == null) {
            throw new IllegalArgumentException("Un joueur ne peut avoir comme personnage un pointeur null");
        }
        this.personnage = personnage;
    }

    // =========================
    // GETTERS / SETTERS POSITION
    // =========================

    public CaseCluedo getPosition() {
        return position;
    }

    public void setPosition(CaseCluedo caseCluedo) throws JoueurException {
        if (caseCluedo == null) {
            throw new JoueurException("Le joueur ne peut être initialisé sur un pointeur null");
        }
        this.position = caseCluedo;
    }

    public int getX() {
        return position.getLigne();
    }

    public int getY() {
        return position.getColonne();
    }

    // =========================
    // GETTERS / SETTERS ETAT
    // =========================

    public boolean getEstElimine() {
        return estElimine;
    }

    public void setEstElimine(boolean etatJoueur) {
        this.estElimine = etatJoueur;
    }

    public boolean getPeutSoupconner() {
        return peutSoupconner;
    }

    public void setPeutSoupconner(boolean peutSoupconner) {
        this.peutSoupconner = peutSoupconner;
    }

    // =========================
    // GESTION DES CARTES
    // =========================

    public ArrayList<Carte> getCartes() {
        return cartes;
    }

    public void setCartes(ArrayList<Carte> cartes) {
        this.cartes = cartes;
    }

    public int getNbCartes() {
        return cartes.size();
    }

    public void ajouterCarte(Carte carte) throws JoueurException {
        if (cartes.contains(carte)) {
            throw new JoueurException("la carte existe deja");
        }
        cartes.add(carte);
    }

    // =========================
    // METHODES METIER
    // =========================

    /**
     * Retourne la liste des cartes du joueur qui correspondent
     * au soupçon donné en paramètre.
     * <p>
     * Une carte est ajoutée à la liste si elle fait partie
     * du trio (lieu, personnage, arme) du soupçon.
     * </p>
     *
     * @param soupcon le soupçon à comparer aux cartes du joueur
     * @return une liste contenant toutes les cartes du joueur
     *         appartenant au soupçon
     */
    public ArrayList<Carte> getCartesCorrespondantAuSoupcon(Hypothese soupcon) {
        ArrayList<Carte> cartesAppartenantAuSoupcon = new ArrayList<>();

        for (Carte carte : cartes) {
            if (soupcon.contient(carte)) {
                cartesAppartenantAuSoupcon.add(carte);
            }
        }

        return cartesAppartenantAuSoupcon;
    }


    /**
     * Retourne une carte du joueur permettant de réfuter
     * le soupçon donné en paramètre.
     * <p>
     * Cette méthode suppose qu'au moins une carte du joueur
     * correspond au soupçon.
     * </p>
     * <p>
     * Actuellement, la première carte correspondante est retournée.
     * À terme, le choix de la carte devra être réalisé par
     * l'interface graphique.
     * </p>
     *
     * @param soupcon le soupçon à réfuter
     * @return une carte du joueur appartenant au soupçon
     */
    public Carte getCartePourRefuterSoupcon(Hypothese soupcon) {
        ArrayList<Carte> cartesAppartenantAuSoupcon = this.getCartesCorrespondantAuSoupcon(soupcon);


        return cartesAppartenantAuSoupcon.get(0);
    }

    // =========================
    // EQUALS
    // =========================

    @Override
    public boolean equals(Object j) {
        if (j == null) return false;
        if (j == this) return true;
        if (this.getClass() != j.getClass()) return false;
        return this.getNom().equals(((Joueur) j).getNom());
    }
}