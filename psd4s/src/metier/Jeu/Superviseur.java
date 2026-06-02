package metier.Jeu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import exception.*;
import metier.cluedo.carte.*;
import metier.cluedo.plateau.*;

/**
 * Représente le superviseur d'une partie de Cluedo.
 * <p>
 * Cette classe centralise l'ensemble de la logique métier du jeu :
 * gestion des joueurs, démarrage de la partie, distribution des cartes,
 * gestion des tours, déplacements, soupçons, accusations
 * et détection de la fin de partie.
 * </p>
 * <p>
 * Le superviseur garantit également le respect des règles
 * en vérifiant la validité des actions réalisées par les joueurs
 * et en levant les exceptions adaptées en cas d'erreur.
 * </p>
 * <p>
 * Cette classe suit le patron de conception Singleton afin
 * d'assurer l'existence d'une unique instance de gestion de partie.
 * </p>
 */
public class Superviseur {

    // ==============================================
    // CONSTANTES
    // ==============================================

    private static final int NB_MIN_JOUEURS = 3;
    private static final int NB_MAX_JOUEURS = 6;

    // ==============================================
    // INSTANCE UNIQUE
    // ==============================================

    private static Superviseur instance;

    // ==============================================
    // ATTRIBUTS DE LA PARTIE
    // ==============================================

    private ArrayList<Joueur> listeJoueurs = new ArrayList<>();
    private PlateauCluedo plateau;
    private boolean partieCommencee = false;
    private Hypothese enigme;
    private int indexJoueurCourant;
    private int nbDeplacementsJoueurCourant;
    private boolean peutLancerLesDesJoueurCourant = false;
    private Hypothese soupconEnCours;
    private int indexJoueurDevantRefuterSoupcon = -1;


    // ==============================================
    // CONSTRUCTEUR ET SINGLETON
    // ==============================================

    public Superviseur() {
        try {
            this.plateau = new PlateauCluedo();
        } catch (PlateauCluedoException e) {
            throw new RuntimeException("Erreur lors de la création du plateau", e);
        }
    }

    /**
     * Retourne l'unique instance de la classe Superviseur.
     * <p>
     * Cette méthode applique le patron Singleton : si aucune instance
     * n'existe encore, elle est créée, sinon l'instance existante est renvoyée.
     * </p>
     *
     * @return l'unique instance de Superviseur
     */
    public static Superviseur getInstance() {
        if (instance == null) {
            instance = new Superviseur();
        }
        return instance;
    }

    // ==============================================
    // METHODES PUBLIQUES : GESTION DES JOUEURS
    // ==============================================

    /**
     * Ajoute un joueur à la liste des participants de la partie.
     * <p>
     * Le joueur est ajouté uniquement si la partie n'a pas encore commencé,
     * que la limite maximale de joueurs n'est pas atteinte,
     * et qu'aucun autre joueur ne possède déjà le même nom
     * ou le même personnage.
     * </p>
     * <p>
     * Lors de son ajout, le joueur est initialement marqué comme éliminé.
     * Son état sera mis à jour lors du démarrage de la partie.
     * </p>
     *
     * @param joueur le joueur à ajouter à la partie
     * @throws ReglesException si la partie a déjà commencé, si le nombre maximal
     *                         de joueurs est atteint, ou si le joueur est déjà présent
     */
    public void ajouterJoueur(Joueur joueur) throws ReglesException {
        verificationsAjouterJoueur(joueur);

        joueur.setEstElimine(true);
        listeJoueurs.add(joueur);
    }

    private void verificationsAjouterJoueur(Joueur joueur) throws ReglesException {
        if (partieCommencee) {
            throw new DemarragePartieException("Vous ne pouvez pas rejoindre la partie car celle-ci a déjà commencée");
        }

        if (joueur == null) {
            throw new IllegalArgumentException("Un joueur ne peut être représenté par un pointeur null");
        }

        if (listeJoueurs.size() == NB_MAX_JOUEURS) {
            throw new JoueurException("Limite de listeJoueurs atteint");
        }

        for (Joueur j : listeJoueurs) {
            if (j.getPersonnage().equals(joueur.getPersonnage()) && j.getNom().equals(joueur.getNom())){
                throw new JoueurException("Votre pseudo et votre personnage sont déjà utilisés");
            }
            if (j.getPersonnage().equals(joueur.getPersonnage())) {
                throw new JoueurException("Le personnage que vous avez choisi est déjà utilisé");
            }
            if (j.getNom().equals(joueur.getNom())) {
                throw new JoueurException("Le pseudo que vous avez choisi est déjà utilisé");
            }
        }
    }


    public boolean estUnParticipant(Joueur joueur) {
        if (joueur == null) {
            throw new IllegalArgumentException("Un pointeur null ne peut appartenir à la liste de joueurs");
        }

        return listeJoueurs.contains(joueur);
    }

    // ==============================================
    // METHODES PUBLIQUES : DEMARRAGE DE LA PARTIE
    // ==============================================

    /**
     * Démarre la partie de Cluedo.
     * <p>
     * Cette méthode initialise l'énigme, distribue les cartes
     * entre les joueurs, place chaque joueur sur sa position de départ
     * et prépare le premier tour de jeu.
     * </p>
     * <p>
     * À l'issue de l'appel, la partie est considérée comme commencée
     * et le joueur courant est autorisé à lancer les dés.
     * </p>
     *
     * @throws DemarragePartieException si la partie est déjà démarrée
     *                                  ou si le nombre minimum de joueurs
     *                                  n'est pas atteint
     */
    public void demarrer() throws DemarragePartieException {
        verificationDemarrer();

        ArrayList<Carte> cartesADistribuer = initialiserEnigmeEtConstruireCartes();
        distribuerCartesAuxJoueurs(cartesADistribuer);

        initialiserEtatETPositionJoueurs();

        this.soupconEnCours = null;
        this.peutLancerLesDesJoueurCourant = true;
        this.partieCommencee = true;
    }

    // ==============================================
    // METHODES PRIVEES : INITIALISATION DE LA PARTIE
    // ==============================================

    private void verificationDemarrer() throws DemarragePartieException {
        if (partieCommencee) {
            throw new DemarragePartieException("La partie est déjà démarrée. Le redémarrage est impossible.");
        }

        if (listeJoueurs.size() < NB_MIN_JOUEURS) {
            throw new DemarragePartieException("Il faut au moins 3 joueurs pour démarrer la partie.");
        }
    }

    private ArrayList<Carte> initialiserEnigmeEtConstruireCartes() {
        ArrayList<EPersonnage> persos = new ArrayList<>(List.of(EPersonnage.values()));
        ArrayList<EArme> armes = new ArrayList<>(List.of(EArme.values()));
        ArrayList<ELieu> lieux = new ArrayList<>(List.of(ELieu.values()));

        Collections.shuffle(persos);
        Collections.shuffle(armes);
        Collections.shuffle(lieux);

        this.enigme = new Hypothese(
                lieux.remove(0),
                persos.remove(0),
                armes.remove(0)
        );

        ArrayList<Carte> toutesLesCartes = new ArrayList<>();
        toutesLesCartes.addAll(persos);
        toutesLesCartes.addAll(armes);
        toutesLesCartes.addAll(lieux);

        Collections.shuffle(toutesLesCartes);

        return toutesLesCartes;
    }

    private void distribuerCartesAuxJoueurs(ArrayList<Carte> cartesADistribuer) {
        int indexJoueur = 0;

        for (Carte carte : cartesADistribuer) {
            listeJoueurs.get(indexJoueur).ajouterCarte(carte);
            indexJoueur = (indexJoueur + 1) % listeJoueurs.size();
        }

        /**
         * Le premier joueur est le premier, dans l'ordre d'enregistrement,
         * parmi ceux qui ont reçu le moins de cartes.
         */
        this.indexJoueurCourant = cartesADistribuer.size() % listeJoueurs.size();
    }

    private ArrayList<CaseCluedo> recupererPositionsDepart() throws DemarragePartieException {
        ArrayList<CaseCluedo> listePositionDepart = new ArrayList<>();

        try {
            listePositionDepart.add(plateau.getCase(0, 16));
            listePositionDepart.add(plateau.getCase(5, 0));
            listePositionDepart.add(plateau.getCase(7, 23));
            listePositionDepart.add(plateau.getCase(18, 0));
            listePositionDepart.add(plateau.getCase(24, 9));
            listePositionDepart.add(plateau.getCase(24, 14));
        } catch (PlateauCluedoException e) {
            throw new DemarragePartieException(
                    "Une des cases du plateau n'a pas pu être récupérée."
            );
        }

        return listePositionDepart;
    }

    private void initialiserEtatETPositionJoueurs() throws DemarragePartieException {
        ArrayList<CaseCluedo> listePositionDepart = recupererPositionsDepart();

        for (int i = 0; i < listeJoueurs.size(); i++) {
            try {
                listeJoueurs.get(i).setPosition(listePositionDepart.get(i));
                listeJoueurs.get(i).setPeutSoupconner(false);
                listeJoueurs.get(i).setEstElimine(false);
            } catch (JoueurException e) {
                throw new DemarragePartieException("Un des joueurs n'a pas pu être initialisé sur une case de départ.");
            }
        }
    }

    // ==============================================
    // METHODES PUBLIQUES : GESTION DU TOUR
    // ==============================================

    /**
     * Met fin au tour du joueur courant et passe au joueur suivant.
     * <p>
     * Cette méthode réinitialise le nombre de déplacements restants,
     * met à jour le joueur courant en sélectionnant le prochain joueur
     * non éliminé, puis prépare le nouveau tour.
     * </p>
     * <p>
     * Si tous les joueurs restants sont éliminés, la partie prend fin
     * et une exception de défaite est levée.
     * </p>
     *
     * @param joueurCourant le joueur qui souhaite terminer son tour
     * @throws ReglesException si la partie n'a pas commencé ou si le joueur
     *                         fourni n'est pas le joueur courant
     * @throws ResultatPartieException si la fin du tour entraîne la fin de partie
     */
    public void mettreFinAuTour(Joueur joueurCourant) throws ReglesException, ResultatPartieException {
        verificationsMettreFinAuTour(joueurCourant);

        nbDeplacementsJoueurCourant = 0;

        MAJIndexProchainJoueurCourant();
        gestionProchainTour();
    }

    private void verificationsMettreFinAuTour(Joueur joueurCourant) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("La partie n'a pas commencé");
        }

        if (joueurCourant == null){
            throw new IllegalArgumentException("Un pointeur null ne peut mettre fin au tour du joueur courant");
        }

        if (joueurCourant.getEstElimine()){
            throw new ReglesException("Un joueur éliminé ne peut mettre fin à son tour");
        }

        if (!joueurCourant.equals(this.getJoueurCourant())) {
            throw new JoueurException("Seul le joueur courant est autorisé à mettre fin au tour");
        }

        if (soupconEnCours != null){
            throw new ReglesException("Le soupcon émis par le joueur courant n'a pas encore été réfuté !");
        }
    }

    private void MAJIndexProchainJoueurCourant() {
        int compteurJoueursParcourus = 0;
        do {
            if (indexJoueurCourant == listeJoueurs.size() - 1) {
                indexJoueurCourant = 0;
            } else {
                indexJoueurCourant++;
            }
            compteurJoueursParcourus++;

        } while (this.getJoueurCourant().getEstElimine() && compteurJoueursParcourus < listeJoueurs.size());
    }

    private void gestionProchainTour() throws ResultatPartieException {
        if (this.getJoueurCourant().getEstElimine()) {
            this.gestionFinPartie();
            throw new DefaiteException("Aucun joueur n'a été en mesure de déterminer l'enigme...");
        }

        this.peutLancerLesDesJoueurCourant = true;
    }


    /**
     * Élimine le joueur courant puis met fin à son tour.
     * <p>
     * Cette méthode marque le joueur courant comme éliminé,
     * puis transfère le tour au prochain joueur non éliminé.
     * </p>
     * <p>
     * Elle est notamment utilisée lorsqu'un joueur effectue
     * une accusation incorrecte.
     * </p>
     *
     * @throws ReglesException si la partie n'a pas commencé ou si une règle
     *                         empêche la fin du tour
     * @throws ResultatPartieException si cette élimination provoque la fin de partie
     */
    public void eliminerJoueurCourantETMettreFinAuTour() throws ReglesException, ResultatPartieException, EliminationJoueurException {
        if (!partieCommencee) {
            throw new DemarragePartieException("Un joueur ne peut être éliminé si la partie n'a pas commencé");
        }


        Joueur joueurElim =  this.getJoueurCourant();
        this.mettreFinAuTour(this.getJoueurCourant());
        joueurElim.setEstElimine(true);
        throw new EliminationJoueurException("le joueur " + joueurElim.getNom() + " est éliminé");
    }

    // ==============================================
    // METHODES PUBLIQUES : ACTIONS DU JOUEUR COURANT
    // ==============================================

    /**
     * Lance les deux dés pour le joueur courant.
     * <p>
     * Cette méthode génère aléatoirement la somme de deux dés à six faces
     * et attribue le résultat au nombre de déplacements restants
     * du joueur courant pour son tour.
     * </p>
     * <p>
     * Une fois les dés lancés, le joueur ne peut plus les relancer
     * durant le même tour.
     * </p>
     *
     * @param joueurCourant le joueur qui lance les dés
     * @throws ReglesException si la partie n'a pas commencé, si le joueur
     *                         n'est pas le joueur courant ou si les dés
     *                         ont déjà été lancés pendant ce tour
     */
    public void lancerDes(Joueur joueurCourant) throws ReglesException {
        verificationsLancerLesDes(joueurCourant);

        nbDeplacementsJoueurCourant = ThreadLocalRandom.current().nextInt(1, 7) + ThreadLocalRandom.current().nextInt(1, 7);
        peutLancerLesDesJoueurCourant = false;
    }

    private void verificationsLancerLesDes(Joueur joueurCourant) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("Il est impossible de lancer les dés si la partie n'a pas démarrée");
        }

        if (soupconEnCours != null){
            throw new ReglesException("Le soupcon émis par le joueur courant n'a pas encore été réfuté !");
        }

        if (joueurCourant == null) {
            throw new IllegalArgumentException("Un pointeur null n'est pas en mesure de pouvoir lancer les dés");
        }

        if (joueurCourant.getEstElimine()){
            throw new ReglesException("Un joueur éliminé ne peut lancer les dés");
        }

        if (!joueurCourant.equals(this.getJoueurCourant())) {
            throw new JoueurException("Seul le joueur courant peut lancer les dés");
        }

        if (!peutLancerLesDesJoueurCourant) {
            throw new ReglesException("Le joueur courant a déjà lancé les dés");
        }
    }


    // ==============================================
    // METHODES PUBLIQUES : SOUPCON
    // ==============================================

    /**
     * Permet au joueur courant d'émettre un soupçon.
     * <p>
     * Le soupçon est composé de la pièce dans laquelle se trouve
     * actuellement le joueur, du personnage suspecté
     * et de l'arme suspectée.
     * </p>
     * <p>
     * Une fois le soupçon émis, le joueur ne peut plus soupçonner
     * de nouveau tant qu'il n'a pas changé de pièce.
     * </p>
     *
     * @param joueurCourant le joueur qui émet le soupçon
     * @param personnage le personnage suspecté
     * @param arme l'arme suspectée
     * @throws ReglesException si les conditions nécessaires au soupçon
     *                         ne sont pas respectées
     */
    public void soupcon(Joueur joueurCourant, EPersonnage personnage, EArme arme) throws ReglesException {
        verificationsSoupcon(joueurCourant, personnage, arme);

        joueurCourant.setPeutSoupconner(false);
        soupconEnCours = new Hypothese(pieceJoueur(joueurCourant), personnage, arme);
        indexJoueurDevantRefuterSoupcon = initIndexPremierJoueurDevantRefuterSoupcon();
    }

    private int initIndexPremierJoueurDevantRefuterSoupcon() throws SoupconNonRefutableException {
        int index = indexJoueurCourant;
        for (int i = 0; i < listeJoueurs.size()-1; i++) {
            index = (index + 1) % listeJoueurs.size();

            Joueur joueurSuppose = listeJoueurs.get(index);

            if (!joueurSuppose.getEstElimine() && index != indexJoueurCourant){
                return index;
            }
        }

        /*
        * Lorsqu'il n'y a qu'un joueur un RegleException est lance dans soupcon
        * Ce SoupconNonRefutable permet seulement de rendre la fonction valide MAIS il ne sera jamais utilisé
        * */
        terminerRefutation();
        throw new SoupconNonRefutableException("Aucun joueur n'a été en mesure de réfuter le soupcon émis !");
    }

    private void prochainIndexJoueurRefutation() throws SoupconNonRefutableException{
        if (soupconEnCours == null) {
            throw new ReglesException("On ne peut déterminer qui est le prochain joueur pouvant réfuter le soupçon si le soupçon n'existe pas");
        }

        int index = indexJoueurDevantRefuterSoupcon;

        do {
            index = (index + 1) % listeJoueurs.size();

            if (index == indexJoueurCourant) {
                terminerRefutation();
                throw new SoupconNonRefutableException("Aucun joueur n'a été en mesure de réfuter le soupçon émis !");
            }

            Joueur joueurSuppose = listeJoueurs.get(index);

            if (!joueurSuppose.getEstElimine()) {
                indexJoueurDevantRefuterSoupcon = index;
                return;
            }

        } while (true);
    }

    public void refuterSoupcon(Joueur joueurDevantRefuter, Carte carte) throws  ReglesException, JoueurNePouvantRefuterException, SoupconNonRefutableException {
        if (!partieCommencee) {
            throw new DemarragePartieException("Aucune carte ne peut refuter un soupcon lorsque la partie n'a pas commencee");
        }
        if (joueurDevantRefuter == null){
            throw new IllegalArgumentException("Un pointeur null ne peut refuter un soupcon !");
        }
        if (soupconEnCours == null){
            throw new ReglesException("On ne peut refuter un soupcon qui n'existe pas !");
        }
        if (!joueurDevantRefuter.equals(getJoueurDevantRefuter())){
            throw new ReglesException("Vous n'êtes pas autoriser à refuter le soupcon en cours");
        }
        if (!joueurDevantRefuter.getCartes().contains(carte) && carte != null){
            throw new ReglesException("Vous ne pouvez refuter un soupcon avec une carte que vous ne possédez pas");
        }


        ArrayList<Carte> cartesJoueurRefutation = joueurDevantRefuter.getCartesCorrespondantAuSoupcon(soupconEnCours);

        if (cartesJoueurRefutation.isEmpty()){
            if (carte != null){
                throw new ReglesException("Vous ne possédez aucune carte permettant de réfuter ce soupçon");
            }
            prochainIndexJoueurRefutation();
            throw new JoueurNePouvantRefuterException("[" + joueurDevantRefuter.getNom() + "] n'a pas été en mesure de refuter le soupcon émis, c'est au tour de [" + listeJoueurs.get(indexJoueurDevantRefuterSoupcon).getNom() + "]");
        }
        if (carte == null){
            throw new ReglesException("Vous possédez au moins une carte permettant de réfuter le soupçon.");
        }
        if (!cartesJoueurRefutation.contains(carte)){
            throw new ReglesException("Cette carte ne correspond pas au soupçon, mais vous détenez au moins une carte valide pour le réfuter.");
        }


        terminerRefutation();
    }


    private Joueur getJoueurDevantRefuter() throws  ReglesException {
        if (indexJoueurDevantRefuterSoupcon == -1){
            throw new ReglesException("Aucun joueur n'est en mesure de refuter un soupcon qui n'existe pas !");
        }
        return listeJoueurs.get(indexJoueurDevantRefuterSoupcon);
    }


    public Joueur getPremierJoueurPouvantRefuter(Hypothese soupcon) {
        if (soupcon == null){
            throw new IllegalArgumentException("On ne peut passer un pointeur null en paramètre");
        }
        for (int i = 1; i < listeJoueurs.size(); i++) {
            int index = (indexJoueurCourant + i) % listeJoueurs.size();
            Joueur joueur = listeJoueurs.get(index);

            if (!joueur.getEstElimine() && !joueur.getCartesCorrespondantAuSoupcon(soupcon).isEmpty()) {
                return joueur;
            }
        }

        return null;
    }

    public void terminerRefutation() {
        soupconEnCours = null;
        indexJoueurDevantRefuterSoupcon = -1;
    }


    public Carte getCarteRefutantSoupcon(Hypothese soupcon) throws SoupconNonRefutableException, ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("Aucune carte ne peut refuter un soupcon lorsque la partie n'a pas commencee");
        }

        if (soupcon == null) {
            throw new IllegalArgumentException("Le soupçon ne peut pas être null");
        }

        Joueur joueur = getPremierJoueurPouvantRefuter(soupcon);

        if (joueur == null) {
            throw new SoupconNonRefutableException("Aucun des joueurs n'a été en mesure de réfuter le soupcon émis par le joueur courant");
        }

        return joueur.getCartePourRefuterSoupcon(soupcon);
    }

    private void verificationsSoupcon(Joueur joueurCourant, EPersonnage personnage, EArme arme) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("La partie n'a pas commencé, il est donc impossible de lancer un soupçon");
        }

        if (personnage == null || arme == null || joueurCourant == null) {
            throw new IllegalArgumentException("On ne peut émettre un soupçon avec un ou des pointeurs null");
        }

        if (joueurCourant.getEstElimine()){
            throw new ReglesException("Un joueur éliminé ne peut lancer un soupcon");
        }

        if (!joueurCourant.equals(this.getJoueurCourant())) {
            throw new JoueurException("Seul le joueur courant est autorisé à réaliser un soupçon");
        }

        if (soupconEnCours != null){
            throw new ReglesException("Il est impossible d'émettre un nouveau soupcon alors qu'un soupcon est déjà en cours !");
        }

        if (!estDansUnePiece(joueurCourant)) {
            throw new PositionJoueurException("Le joueur doit être dans une pièce pour émettre un soupcon");
        }

        if (!joueurCourant.getPeutSoupconner()) {
            throw new ReglesException("Le joueur n'est pas en mesure de pouvoir soupconner");
        }

        if (nbJoueursRestant() <= 1){
            throw new ReglesException("Il n'y a pas assez de joueur pour pouvoir émettre un soupcon !");
        }
    }

    // ==============================================
    // METHODES PUBLIQUES : POSITIONS ET DEPLACEMENTS
    // ==============================================

    /**
     * Déplace le joueur courant vers une case voisine du plateau.
     * <p>
     * Le déplacement consomme un point de déplacement
     * et met à jour la capacité du joueur à émettre un soupçon
     * en fonction de son changement éventuel de pièce.
     * </p>
     * <p>
     * Si le joueur entre dans une nouvelle pièce,
     * il est alors autorisé à soupçonner.
     * À l'inverse, s'il quitte une pièce, cette capacité est retirée.
     * </p>
     *
     * @param joueurCourant le joueur à déplacer
     * @param caseCluedo la case de destination
     * @throws ReglesException si le déplacement ne respecte pas
     *                         les règles du jeu
     */
    public void deplacer(Joueur joueurCourant, CaseCluedo caseCluedo) throws ReglesException {
        verificationsDeplacement(joueurCourant, caseCluedo);

        ELieu anciennePiece = getPieceOuNull(joueurCourant);

        effectuerDeplacement(joueurCourant, caseCluedo);

        ELieu nouvellePiece = getPieceOuNull(joueurCourant);

        mettreAJourCapaciteSoupcon(joueurCourant, anciennePiece, nouvellePiece);
    }

    public boolean estCeQueLaCaseAppartientAUnJoueur(CaseCluedo caseCluedo) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("La partie n'a pas commencé donc aucun des joueurs ne peut se trouver sur une case");
        }

        if (caseCluedo == null) {
            throw new IllegalArgumentException("Aucun des participants ne peuvent être positionnés sur un pointeur null");
        }

        for (Joueur joueur : listeJoueurs) {
            if (joueur.getPosition().equals(caseCluedo)) {
                return true;
            }
        }

        return false;
    }

    public Joueur donnerJoueurPresentSurLaCase(CaseCluedo caseCluedo) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("La partie n'a pas commencé donc aucun des joueurs ne peut se trouver sur une case");
        }

        if (caseCluedo == null) {
            throw new IllegalArgumentException("Aucun des participants ne peuvent être positionnés sur un pointeur null");
        }

        for (Joueur joueur : listeJoueurs) {
            if (joueur.getPosition().equals(caseCluedo)) {
                return joueur;
            }
        }

        throw new PositionJoueurException("Aucun joueur n'est associé à cette case");
    }

    public boolean estDansUnePiece(Joueur joueur) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("La partie n'a pas commencé donc aucun des joueurs ne peut se trouver dans une pièce");
        }

        if (joueur == null) {
            throw new IllegalArgumentException("Un pointeur null ne peut pas appartenir à la liste de participants");
        }

        if (!this.estUnParticipant(joueur)) {
            throw new JoueurException("Il est impossible pour le superviseur de donner la position d'un joueur qui n'appartient pas à la liste de participants");
        }

        return joueur.getPosition().getPiece() != null;
    }

    public ELieu pieceJoueur(Joueur joueur) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("La partie n'a pas commencé donc aucun des joueurs ne peut se trouver dans une pièce");
        }

        if (joueur == null) {
            throw new IllegalArgumentException("Un pointeur null ne peut pas appartenir à la liste de participants");
        }

        if (!this.estUnParticipant(joueur)) {
            throw new JoueurException("Il est impossible pour le superviseur de donner la position d'un joueur qui n'appartient pas à la liste de participants");
        }

        if (joueur.getPosition().getPiece() == null) {
            throw new PositionJoueurException("Il est impossible de donner la piece à laquelle appartient un joueur ci-celui n'est pas dans une pièce");
        }

        return joueur.getPosition().getPiece();
    }

    // ==============================================
    // METHODES PRIVEES : OUTILS DE DEPLACEMENT
    // ==============================================

    private void verificationsDeplacement(Joueur joueurCourant, CaseCluedo caseCluedo) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("Un joueur ne peut être déplacé si la partie n'a pas commencé");
        }

        if (soupconEnCours != null){
            throw new ReglesException("Le soupcon émis par le joueur courant n'a pas encore été réfuté !");
        }

        if (caseCluedo == null) {
            throw new IllegalArgumentException("Le joueur ne peut se déplacer vers un pointeur null");
        }

        if (joueurCourant.getEstElimine()){
            throw new ReglesException("Un joueur éliminé ne peut se déplacer");
        }

        if (!joueurCourant.equals(this.getJoueurCourant())) {
            throw new JoueurException("Seul le joueur courant est en mesure de pouvoir se déplacer");
        }

        if (this.getNbDeplacementsJoueurCourant() <= 0 && this.getPeutLancerLesDesJoueurCourant()){
            throw new DeplacementJoueurException("Le joueur ne peut pas se déplacer puisque vous n'avez pas lancé les dés");
        }

        if (this.getNbDeplacementsJoueurCourant() <= 0  && !this.getPeutLancerLesDesJoueurCourant()) {
            throw new DeplacementJoueurException("Le joueur n'a plus assez de déplacements");
        }

        if (!joueurCourant.getPosition().estVoisin(caseCluedo)) {
            throw new DeplacementJoueurException("Le joueur ne peut pas se déplacer vers une case qui ne lui est pas voisine");
        }

        if (this.estCeQueLaCaseAppartientAUnJoueur(caseCluedo)) {
            throw new DeplacementJoueurException("Le joueur n'a pas le droit de se déplacer vers une case déjà occupée par un autre joueur");
        }
    }

    private ELieu getPieceOuNull(Joueur joueur) {
        if (this.estDansUnePiece(joueur)) {
            return this.pieceJoueur(joueur);
        }

        return null;
    }

    private void effectuerDeplacement(Joueur joueurCourant, CaseCluedo caseCluedo) {
        joueurCourant.setPosition(caseCluedo);
        this.setNbDeplacementsJoueurCourant(this.getNbDeplacementsJoueurCourant() - 1);
    }

    private void mettreAJourCapaciteSoupcon(Joueur joueurCourant, ELieu anciennePiece, ELieu nouvellePiece) {
        if (nouvellePiece != null && nouvellePiece != anciennePiece) {
            joueurCourant.setPeutSoupconner(true);
        }

        if (anciennePiece != null && nouvellePiece == null) {
            joueurCourant.setPeutSoupconner(false);
        }
    }

    // ==============================================
    // METHODES PUBLIQUES : ACCUSATION
    // ==============================================

    /**
     * Permet au joueur courant de formuler une accusation.
     * <p>
     * L'accusation est comparée à l'énigme de la partie.
     * Si elle est correcte, la partie prend fin sur une victoire.
     * Sinon, le joueur courant est éliminé et son tour se termine.
     * </p>
     *
     * @param joueurCourant le joueur qui formule l'accusation
     * @param personnage le personnage accusé
     * @param arme l'arme accusée
     * @param lieu le lieu accusé
     * @throws ReglesException si l'accusation ne respecte pas
     *                         les règles du jeu
     * @throws ResultatPartieException si l'accusation entraîne
     *                                 une victoire ou une fin de partie
     */
    public void accuser(Joueur joueurCourant, EPersonnage personnage, EArme arme, ELieu lieu) throws ReglesException, ResultatPartieException {
        verificationsAccuser(joueurCourant, personnage, arme, lieu);

        Hypothese accusation = new Hypothese(lieu, personnage, arme);

        gestionAccusation(accusation, joueurCourant);
    }

    private void verificationsAccuser(Joueur joueurCourant, EPersonnage personnage, EArme arme, ELieu lieu) throws ReglesException {
        if (!partieCommencee) {
            throw new DemarragePartieException("On ne peut accuser un joueur lorsque la partie n'a pas commencée");
        }

        if (soupconEnCours != null){
            throw new ReglesException("Le soupcon émis par le joueur courant n'a pas encore été réfuté !");
        }

        if (joueurCourant == null){
            throw new IllegalArgumentException("Un pointeur null ne peut lancer une accusation");
        }

        if (joueurCourant.getEstElimine()){
            throw new ReglesException("Un joueur éliminé ne peut lancer une accusation");
        }

        if (!joueurCourant.equals(this.getJoueurCourant())) {
            throw new JoueurException("Seul le joueur courant peut lancer une accusation");
        }

        if (personnage == null || arme == null || lieu == null) {
            throw new IllegalArgumentException("Un pointeur null ne peut faire partie de l'accusation");
        }
    }

    private void gestionAccusation(Hypothese accusation, Joueur joueurCourant)
            throws ResultatPartieException {

        if (accusation.equals(this.getEnigme())) {
            this.gestionFinPartie();
            throw new VictoireException("Le joueur " + joueurCourant.getNom() + " a gagné la partie !");
        }

        joueurCourant.setEstElimine(true);

        if (nbJoueursRestant() == 0) {
            this.gestionFinPartie();
            throw new DefaiteException("Aucun joueur n'a été en mesure de déterminer l'enigme...");
        }

        nbDeplacementsJoueurCourant = 0;
        MAJIndexProchainJoueurCourant();
        peutLancerLesDesJoueurCourant = true;

        throw new EliminationJoueurException("le joueur " + joueurCourant.getNom() + " est éliminé");
    }

    // ==============================================
    // METHODES PUBLIQUES : FIN DE PARTIE
    // ==============================================

    /**
     * Met fin à la partie et réinitialise son état.
     * <p>
     * Cette méthode vide les cartes de chaque joueur,
     * marque tous les joueurs comme éliminés
     * et réinitialise les attributs liés au déroulement
     * de la partie.
     * </p>
     * <p>
     * Après son exécution, aucune partie n'est considérée
     * comme en cours.
     * </p>
     */
    public void gestionFinPartie() {
        if (partieCommencee) {
            for (Joueur joueur : listeJoueurs) {
                joueur.getCartes().clear();
                joueur.setEstElimine(true);
            }

            soupconEnCours = null;
            partieCommencee = false;
            indexJoueurCourant = 0;
            indexJoueurDevantRefuterSoupcon = -1;
            nbDeplacementsJoueurCourant = 0;
            peutLancerLesDesJoueurCourant = false;
            enigme = null;
        }
    }

    public void resetSuperviseur() {
        gestionFinPartie();
        listeJoueurs.clear();
    }

    // ==============================================
    // METHODES ANNEXES
    // ==============================================

    public int nbJoueursRestant(){
        int compteur = 0;
        for (Joueur joueur : listeJoueurs) {
            if (!joueur.getEstElimine()){
                compteur++;
            }
        }
        return compteur;
    }

    // ==============================================
    // ACCESSEURS
    // ==============================================

    public ArrayList<Joueur> getListeJoueurs() {
        return listeJoueurs;
    }

    public int getNbJoueurs() {
        return listeJoueurs.size();
    }

    public PlateauCluedo getPlateau() {
        return plateau;
    }

    public boolean getPartieCommencee() {
        return partieCommencee;
    }

    public void setPartieCommencee(boolean b) {
        this.partieCommencee = b;
    }

    public Hypothese getEnigme() {
        return enigme;
    }

    public void setEnigme(Hypothese enigme) {
        if (enigme == null){
            throw new IllegalArgumentException("L'enigme ne peut être un pointeur null");
        }
        this.enigme = enigme;
    }

    public int getIndexJoueurCourant() {
        return indexJoueurCourant;
    }

    public Joueur getJoueurDevantRefuterSoupcon() {
        if (indexJoueurDevantRefuterSoupcon == -1){
            return null;
        }
        return listeJoueurs.get(indexJoueurDevantRefuterSoupcon);
    }

    public Joueur getJoueurCourant() {
        return listeJoueurs.get(indexJoueurCourant);
    }

    public boolean getPeutLancerLesDesJoueurCourant() {
        return peutLancerLesDesJoueurCourant;
    }

    public void setPeutLancerLesDesJoueurCourant(boolean peutLancerLesDesJoueurCourant) {
        this.peutLancerLesDesJoueurCourant = peutLancerLesDesJoueurCourant;
    }

    public int getNbDeplacementsJoueurCourant() {
        return nbDeplacementsJoueurCourant;
    }

    public void setNbDeplacementsJoueurCourant(int nbDeplacementsJoueurCourant) {
        this.nbDeplacementsJoueurCourant = nbDeplacementsJoueurCourant;
    }

    public Hypothese getSoupconEnCours(){
        return soupconEnCours;
    }

    public int getIndexJoueurDevantRefuterSoupcon(){
        return indexJoueurDevantRefuterSoupcon;
    }

}