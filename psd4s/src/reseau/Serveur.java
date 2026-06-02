package reseau;

import java.io.IOException;
import java.util.ArrayList;

import exception.DefaiteException;
import exception.DemarragePartieException;
import exception.EliminationJoueurException;
import exception.JoueurNePouvantRefuterException;
import exception.ReglesException;
import exception.ResultatPartieException;
import exception.SoupconNonRefutableException;
import metier.Jeu.Hypothese;
import metier.Jeu.Joueur;
import metier.Jeu.Superviseur;
import metier.cluedo.carte.Carte;
import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;
import metier.cluedo.plateau.CaseCluedo;

/**
 * Représente le serveur du jeu Cluedo.
 *
 * Cette classe assure le lien entre la couche réseau et la couche métier.
 * Elle centralise les connexions des utilisateurs, reçoit les requêtes
 * transmises par les experts du protocole et invoque les traitements
 * appropriés du {@link Superviseur}.
 *
 * Le serveur est également responsable de la diffusion des informations
 * aux différents joueurs afin de maintenir une vision cohérente de l'état
 * de la partie sur l'ensemble des clients connectés.
 *
 * Les règles du jeu ne sont pas implémentées dans cette classe mais dans
 * la classe {@link Superviseur}.
 */
public class Serveur {
    private int port;
    private ArrayList<ConnexionUtilisateur> utilisateurs = new ArrayList<>();

    private Superviseur superviseur;


    /**
     * Créer un serveur écoutant sur le port spécifié.
     *
     * Le serveur initialise la couche métier puis démarre le thread
     * chargé d'accepter les connexions entrantes.
     *
     * @param port port d'écoute du serveur
     */
    public Serveur(int port) {
        this.port = port;
        this.superviseur = Superviseur.getInstance();

        initCensure();

        new ThreadAcceptConnexion(this);
    }

    /**
     * Constructeur utilisé exclusivement pour les tests d'intégration.
     *
     * Aucun thread réseau n'est démarré.
     */
    public Serveur() {
        this.port = 0;
        this.superviseur = Superviseur.getInstance();

        initCensure();
    }


    public Superviseur getSuperviseur() {
        return this.superviseur;
    }

    public int getPort() {
        return port;
    }

    public int size() {
        return utilisateurs.size();
    }

    public boolean add(ConnexionUtilisateur utilisateur) {
        return utilisateurs.add(utilisateur);
    }

    public boolean remove(ConnexionUtilisateur utilisateur) throws ServeurException {
        if (utilisateur == null)
            throw new ServeurException("La connexion utilisateur vaut null");

        return utilisateurs.remove(utilisateur);
    }
    
    
    
    
  //// --------------- CARTE FUNCTIONS ----------------------------------------------  

    /**
     * Convertit une carte Cluedo vers sa représentation protocolaire.
     *
     * Exemples :
     * P0, A3, L5.
     *
     * @param c carte à convertir
     * @return représentation textuelle de la carte
     */
    public static String carteToString(Carte c) {
    	switch (c) {
    	case EPersonnage p : return "P" + p.ordinal();
    	case EArme a : return "A" + a.ordinal();
    	case ELieu l : return "L" + l.ordinal();
    	default : return null;
    	}
    }

	private static final EPersonnage[] CACHE_PERSONNAGES = EPersonnage.values();
	private static final EArme[] CACHE_ARMES = EArme.values();
	private static final ELieu[] CACHE_LIEUX = ELieu.values();

    /**
     * Convertit une représentation protocolaire vers une carte Cluedo.
     *
     * Exemples :
     * P0, A3, L5.
     *
     * @param nom chaîne représentant une carte
     * @return la carte correspondante ou null si la chaîne est invalide
     */
    public static Carte stringToCarte(String nom) {
        if (nom == null) return null;

        try {
            switch (nom.charAt(0)) {
                case 'P':
                    return CACHE_PERSONNAGES[Integer.parseInt(nom.substring(1))];

                case 'A':
                    return CACHE_ARMES[Integer.parseInt(nom.substring(1))];

                case 'L':
                    return CACHE_LIEUX[Integer.parseInt(nom.substring(1))];
            }
        }
        catch (ArrayIndexOutOfBoundsException |
               NumberFormatException e) {
            return null;
        }



        return null;
    }





    /**
     * Recherche un utilisateur à partir de son pseudo.
     *
     * @param pseudo pseudo recherché
     * @return la connexion correspondante
     * @throws ServeurException si aucun utilisateur ne possède ce pseudo
     */
    public ConnexionUtilisateur get(String pseudo) throws ServeurException {
        for (ConnexionUtilisateur utilisateur : utilisateurs) {
            if (pseudo.equals(utilisateur.getPseudo()))
                return utilisateur;
        }

        throw new ServeurException("L'utilisateur "+pseudo+" n'existe pas...");
    }

    /**
     * Diffuse un message public à tous les utilisateurs connectés.
     *
     * Les mots censurés sont automatiquement remplacés avant diffusion.
     *
     * @param emetteur utilisateur à l'origine du message
     * @param message contenu du message
     */
    public void messagePublic(ConnexionUtilisateur emetteur, String message) {
        // On remplace les mots tabous du message
        for (String mot : motsCensures) {
            message = message.replace(mot, censure);
        }

        // Et on parcours la liste des utilisateurs pour leur envoyer le message à chacun (sauf à soi-même)
        for (ConnexionUtilisateur utilisateur : utilisateurs) {
            if (utilisateur.equals(emetteur))
                continue;

            utilisateur.envoyerMessagePublic(emetteur, message);
        }
    }

    /**
     * Envoie un message privé à un utilisateur précis.
     *
     * @param emetteur utilisateur à l'origine du message
     * @param pseudoDestination pseudo du destinataire
     * @param message contenu du message
     * @throws ServeurException si le destinataire n'existe pas
     */
    public void messagePrive(ConnexionUtilisateur emetteur, String pseudoDestination, String message) throws ServeurException {
        ConnexionUtilisateur dest = get(pseudoDestination);

        dest.envoyerMessagePrive(emetteur, message);
    }

    /**
     * Envoie un message à tous les joueurs actuellement connectés.
     *
     * @param message message à diffuser
     */
    public void envoyerMessageAToutLeMonde(String message) {
        if (message == null || message.isEmpty()){
            throw new IllegalArgumentException("On ne peut envoyer un pointeur null comme message à tout le monde !");
        }
        for (ConnexionUtilisateur utilisateur : utilisateurs) {
            if (utilisateur.getJoueur() != null){
                utilisateur.getThreadConnexion().envoyerMessage(message);
            }
        }
    }

    /**
     * Envoie un message à tous les joueurs sauf à celui indiqué.
     *
     * @param utilisateur joueur exclu de la diffusion
     * @param message message à transmettre
     */
    public void envoyerMessageAuxAutresJoueurs(ConnexionUtilisateur utilisateur, String message) {
        if (message == null || message.isEmpty()){
            throw new IllegalArgumentException("Le message à envoyer ne peut être vide");
        }
        if (utilisateur == null || !utilisateurs.contains(utilisateur)){
            throw new IllegalArgumentException("L'utilisateur est incorrect");
        }

        for (ConnexionUtilisateur user : utilisateurs){
            if (!user.equals(utilisateur) && user.getJoueur() != null){
                user.getThreadConnexion().envoyerMessage(message);
            }
        }
    }



    // ******* Partie "métier" *****
    // La censure est appliquée uniquement sur les messages publiques, pas dans les messages privés
    // Par exemple, ici on ne veut pas parler de politique, donc on censure les noms des principales
    // personnalités politique française (choisissez les mots tabous que vous voulez)

    public ArrayList<String> motsCensures = new ArrayList<>();
    public String censure = "@#$?!";

    private void initCensure() {
        motsCensures.add("BAYROU");
        motsCensures.add("MACRON");
        motsCensures.add("LE PEN");
        motsCensures.add("MELENCHON");
        motsCensures.add("RETAILLEAU");
        motsCensures.add("FAURE");
        motsCensures.add("TONDELLIER");
        motsCensures.add("ROUSSEL");
        motsCensures.add("ADAM");
        // rajoutez qui vous voulez, même le nom de vos profs ;-)
    }



    /**
     * Traite une demande de connexion provenant d'un client.
     *
     * Cette méthode vérifie que le personnage demandé est valide et que
     * l'utilisateur n'est pas déjà connecté. Un nouvel objet {@link Joueur}
     * est ensuite créé puis enregistré auprès du {@link Superviseur}.
     *
     * En cas de succès :
     * <ul>
     *     <li>le joueur est associé à la connexion ;</li>
     *     <li>un message de confirmation est envoyé au client ;</li>
     *     <li>la liste des autres joueurs déjà connectés est transmise ;</li>
     *     <li>les autres joueurs sont informés de cette nouvelle connexion.</li>
     * </ul>
     *
     * En cas d'erreur, un message d'erreur explicite est envoyé au client.
     *
     * @param utilisateur connexion à authentifier
     * @param pseudo pseudo choisi par le joueur
     * @param perso personnage sélectionné par le joueur
     */
    public void traiterConnexion(ConnexionUtilisateur utilisateur, String pseudo, EPersonnage perso) {
        System.out.println("LE SERVEUR TRAITE LA CONNEXION DE : " + pseudo);

        if (utilisateur.getJoueur() != null){
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous êtes déjà connecté !");
            return;
        }


        if (perso == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR_PARAM personnage que vous avez envoyé est incorrect");
            return;
        }

        try {
            Joueur joueur = new Joueur(pseudo, perso);
            getSuperviseur().ajouterJoueur(joueur);
            utilisateur.setJoueur(joueur);
            utilisateur.setPseudo(pseudo);
            utilisateur.getThreadConnexion().envoyerMessage("@CONNEXION_REUSSIE");

            StringBuilder msg =  new StringBuilder("@INFOS_AUTRESJOUEURS");
            for (Joueur j : getSuperviseur().getListeJoueurs()){
                if (!j.equals(joueur)){
                    msg.append(" ").append(j.getNom()).append(" ").append(carteToString(j.getPersonnage()));
                }
            }
            utilisateur.getThreadConnexion().envoyerMessage(msg.toString());

            if (!utilisateurs.contains(utilisateur)){
                this.add(utilisateur);
            }

            envoyerMessageAuxAutresJoueurs(utilisateur, "@CONNEXION " + utilisateur.getJoueur().getNom() + " " + carteToString(utilisateur.getJoueur().getPersonnage()));
        } catch (ReglesException e){
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        }
    }

    /**
     * Traite une demande de démarrage de partie.
     *
     * Cette méthode vérifie que l'utilisateur est correctement connecté
     * puis demande au {@link Superviseur} d'initialiser la partie.
     *
     * Lorsque le démarrage réussit :
     * <ul>
     *     <li>les cartes de chaque joueur sont distribuées et envoyées ;</li>
     *     <li>l'état initial de la partie est diffusé à tous les joueurs ;</li>
     *     <li>le joueur courant est désigné.</li>
     * </ul>
     *
     * Si les conditions de démarrage ne sont pas réunies
     * (nombre insuffisant de joueurs par exemple),
     * un message d'erreur est envoyé au client.
     *
     * @param utilisateur joueur ayant demandé le démarrage
     */
    public void traiterDemarrer(ConnexionUtilisateur utilisateur) {
        if (utilisateur.getJoueur() == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour démarrer la partie");
            return;
        }
        
        try {
            utilisateur.getServeur().getSuperviseur().demarrer();
            this.envoyerMainAChaqueJoueur();
            this.envoyerEtatInitialPartie();
        } catch(DemarragePartieException e){
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        }
    }


    /**
     * Envoie à chaque joueur la liste de ses cartes.
     */
    private void envoyerMainAChaqueJoueur() {
		
    	for (ConnexionUtilisateur u : utilisateurs) {
    		if ( u.getJoueur() != null ) {
    			StringBuilder cartes = new StringBuilder("@VOS_CARTES");
    			for (Carte c : u.getJoueur().getCartes()) {
    				cartes.append(" ").append(carteToString(c));
    				//cartes.append(" ").append(c);
    			}
    			u.getThreadConnexion().envoyerMessage(cartes.toString());
    		}
    	}
		
	}

    /**
     * Diffuse l'état initial de la partie à tous les joueurs.
     */

    private void envoyerEtatInitialPartie() {
        StringBuilder msg = new StringBuilder("@PARTIE_COMMENCEE");
        msg.append(" ").append(superviseur.getJoueurCourant().getNom());
        for (Joueur joueur : getSuperviseur().getListeJoueurs()) {
            msg.append(" ").append(joueur.getNom()).append(" ").append(joueur.getX()).append(" ").append(joueur.getY());
        }
        envoyerMessageAToutLeMonde(msg.toString());
    }

    /**
     * Traite une demande de lancer de dés.
     *
     * Cette méthode délègue le calcul du résultat au
     * {@link Superviseur}. Si l'action est autorisée,
     * le nombre de déplacements accordés au joueur courant
     * est renvoyé au client.
     *
     * Les vérifications concernant le tour de jeu et les
     * règles associées sont réalisées par le Superviseur.
     *
     * @param utilisateur joueur souhaitant lancer les dés
     */
    public void traiterLancerDes(ConnexionUtilisateur utilisateur) {
        if (utilisateur.getJoueur() == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour lancer les des");
            return;
        }


        try {
            getSuperviseur().lancerDes(utilisateur.getJoueur());
            utilisateur.getThreadConnexion().envoyerMessage("@LANCER_DES_REUSSI " + getSuperviseur().getNbDeplacementsJoueurCourant());
        } catch (ReglesException e) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        }
    }

    /**
     * Traite une demande de déplacement sur le plateau.
     *
     * Les coordonnées reçues sont converties en une
     * {@link CaseCluedo}. La validité du déplacement est
     * ensuite vérifiée par le {@link Superviseur}.
     *
     * Si le déplacement est accepté, la nouvelle position
     * du joueur est diffusée à tous les clients afin de
     * synchroniser leur représentation du plateau.
     *
     * @param utilisateur joueur effectuant le déplacement
     * @param posX coordonnée X de la case cible
     * @param posY coordonnée Y de la case cible
     */
    public void traiterDeplacement(ConnexionUtilisateur utilisateur, String posX, String posY) {
        if (utilisateur.getJoueur() == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour pouvoir vous déplacer sur le plateau");
            return;
        }


        try {
            CaseCluedo caseSouhaitee = getSuperviseur().getPlateau().getCase(Integer.parseInt(posX), Integer.parseInt(posY));
            getSuperviseur().deplacer(utilisateur.getJoueur(), caseSouhaitee);
            envoyerMessageAToutLeMonde("@DEPLACEMENT_REUSSI " + utilisateur.getJoueur().getNom() + " " + posX + " " + posY);
        } catch (ReglesException e) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        }
    }

    /**
     * Traite l'émission d'un soupçon.
     *
     * Après validation des paramètres, le soupçon est transmis
     * au {@link Superviseur} qui vérifie que le joueur est
     * autorisé à effectuer cette action.
     *
     * Lorsque le soupçon est accepté :
     * <ul>
     *     <li>il est diffusé à tous les joueurs ;</li>
     *     <li>le joueur devant réfuter est déterminé ;</li>
     *     <li>une notification lui est envoyée.</li>
     * </ul>
     *
     * @param utilisateur joueur émettant le soupçon
     * @param personnageSoupcon personnage soupçonné
     * @param armeSoupcon arme soupçonnée
     */
    public void traiterSoupcon(ConnexionUtilisateur utilisateur, EPersonnage personnageSoupcon, EArme armeSoupcon) {
        if (utilisateur.getJoueur() == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour pouvoir lancer un soupcon");
            return;
        }



        if (personnageSoupcon == null || armeSoupcon == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR_PARAM le ou les noms des éléments passés dans le soupcon sont incorrects");
            return;
        }

        try {
            getSuperviseur().soupcon(utilisateur.getJoueur(), personnageSoupcon, armeSoupcon);
            Hypothese soupconEmis = getSuperviseur().getSoupconEnCours();
            envoyerMessageAToutLeMonde(
                    "@SOUPCON_EMIS " + utilisateur.getJoueur().getNom() + " " +
                           carteToString(  soupconEmis.getPersonnage()) + " " +
                           carteToString(soupconEmis.getArme()) + " " +
                       	   carteToString(soupconEmis.getLieu())
            );
            ConnexionUtilisateur userSoupcon = get(getSuperviseur().getJoueurDevantRefuterSoupcon().getNom());
            userSoupcon.getThreadConnexion().envoyerMessage("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours");
        } catch (ReglesException e) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        } catch(ServeurException e) {
            envoyerMessageAToutLeMonde("@ERREUR_GST_PARTIE");
            getSuperviseur().gestionFinPartie();
        }
    }

    /**
     * Traite une tentative de réfutation du soupçon en cours.
     *
     * La carte transmise par le client est convertie puis
     * soumise au {@link Superviseur}. Selon le résultat,
     * plusieurs situations peuvent se produire :
     *
     * <ul>
     *     <li>le soupçon est réfuté avec succès ;</li>
     *     <li>le joueur ne possède aucune carte permettant
     *     une réfutation ;</li>
     *     <li>aucun joueur n'est capable de réfuter le soupçon ;</li>
     *     <li>un autre joueur doit encore tenter une réfutation.</li>
     * </ul>
     *
     * Les messages correspondants sont automatiquement envoyés
     * aux clients concernés.
     *
     * @param utilisateur joueur effectuant la réfutation
     * @param nomDeLaCarte carte utilisée pour réfuter
     */
    public void traiterRefuterSoupcon(ConnexionUtilisateur utilisateur, String nomDeLaCarte) {
        if (utilisateur.getJoueur() == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour pouvoir refuter un soupcon");
            return;
        }


        try {
            Carte carteSoupcon = stringToCarte(nomDeLaCarte);

            getSuperviseur().refuterSoupcon(utilisateur.getJoueur(), carteSoupcon);

            ConnexionUtilisateur userCourant = get(getSuperviseur().getJoueurCourant().getNom());
            envoyerMessageAuxAutresJoueurs(userCourant, "@SOUPCON_REFUTE " + utilisateur.getJoueur().getNom());

            userCourant.getThreadConnexion().envoyerMessage("@CARTE_REFUTANT_SOUPCON " + carteToString(carteSoupcon) +  " " +  utilisateur.getJoueur().getNom());
        } catch(IllegalArgumentException e) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR_PARAM le nom de la carte que vous avez passé pour refuter le soupcon est incorrect");
        } catch (ReglesException e) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        } catch (SoupconNonRefutableException e){
            envoyerMessageAuxAutresJoueurs(utilisateur, "@SOUPCON_NON_REFUTE " + utilisateur.getJoueur().getNom());
            envoyerMessageAToutLeMonde("@INFORMATION_REFUTATION aucun des joueurs présent n'a été en mesure de réfuter le soupcon émis par " + getSuperviseur().getJoueurCourant().getNom());
        } catch (JoueurNePouvantRefuterException e) {
        	
            try {
                ConnexionUtilisateur utilDevantRefuter = get(getSuperviseur().getJoueurDevantRefuterSoupcon().getNom());
                envoyerMessageAuxAutresJoueurs(utilisateur, "@SOUPCON_NON_REFUTE " + utilisateur.getJoueur().getNom());
                utilDevantRefuter.getThreadConnexion().envoyerMessage("@SOUPCON_A_REFUTER c'est à votre tour de refuter le soupcon en cours");
            } catch (ServeurException s){
                envoyerMessageAToutLeMonde("@ERREUR_GST_PARTIE");
                getSuperviseur().gestionFinPartie();
            }
        } catch(ServeurException e){
            envoyerMessageAToutLeMonde("@ERREUR_GST_PARTIE");
            getSuperviseur().gestionFinPartie();
        }
    }

    /**
     * Met fin au tour du joueur courant.
     *
     * Le {@link Superviseur} sélectionne alors le prochain
     * joueur actif et réinitialise les informations liées
     * au tour précédent.
     *
     * Le changement de joueur est ensuite diffusé à tous
     * les clients.
     *
     * Si la fin du tour provoque la fin de la partie,
     * les informations correspondantes sont envoyées
     * à l'ensemble des joueurs.
     *
     * @param utilisateur joueur souhaitant terminer son tour
     */
    public void traiterFinTour(ConnexionUtilisateur utilisateur) {
        if (utilisateur.getJoueur() == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour pouvoir mettre fin au tour");
            return;
        }

        try {
            getSuperviseur().mettreFinAuTour(utilisateur.getJoueur());
            envoyerMessageAToutLeMonde("@FIN_TOUR " + getSuperviseur().getJoueurCourant().getNom());
        } catch (ReglesException e) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        } catch (ResultatPartieException r){
            envoyerMessageAToutLeMonde("@DEFAITE " + r.getMessage());
            getSuperviseur().gestionFinPartie();
        }
    }

    /**
     * Traite une accusation formulée par un joueur.
     *
     * L'accusation est transmise au {@link Superviseur}
     * qui la compare à l'énigme de la partie.
     *
     * Selon le résultat :
     * <ul>
     *     <li>le joueur remporte la partie ;</li>
     *     <li>le joueur est éliminé ;</li>
     *     <li>la partie se poursuit normalement.</li>
     * </ul>
     *
     * Les notifications appropriées sont ensuite diffusées
     * aux différents clients.
     *
     * @param utilisateur joueur effectuant l'accusation
     * @param personnage personnage accusé
     * @param arme arme accusée
     * @param lieu lieu accusé
     */
    public void traiterAccuser(ConnexionUtilisateur utilisateur, EPersonnage personnage, EArme arme, ELieu lieu) {
        if (utilisateur.getJoueur() == null){
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR vous devez être connecté pour pouvoir lancer une accusation");
            return;
        }

        if (personnage == null || arme == null || lieu == null) {
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR_PARAM un ou plusieurs des éléments de votre accusation sont incorrects");
            return;
        }

        try {
            getSuperviseur().accuser(utilisateur.getJoueur(), personnage, arme, lieu);
        } catch(ReglesException e){
            utilisateur.getThreadConnexion().envoyerMessage("@ERREUR " + e.getMessage());
        } catch (DefaiteException e) {
            envoyerMessageAToutLeMonde("@DEFAITE " + e.getMessage());
        } catch(ResultatPartieException r){
            envoyerMessageAToutLeMonde("@VICTOIRE " + utilisateur.getJoueur().getNom());
            getSuperviseur().gestionFinPartie();
        } catch(EliminationJoueurException t){
            envoyerMessageAToutLeMonde("@JOUEUR_ELIM " + utilisateur.getJoueur().getNom());
            envoyerMessageAToutLeMonde("@FIN_TOUR " + superviseur.getJoueurCourant().getNom());
        }
    }

    /**
     * Déconnecte un utilisateur du serveur.
     *
     * La connexion est supprimée de la liste des utilisateurs
     * actifs puis les ressources réseau associées sont libérées.
     *
     * Si une partie est en cours, celle-ci est immédiatement
     * interrompue afin de préserver la cohérence du jeu.
     * Tous les clients restants sont alors déconnectés et
     * le {@link Superviseur} est réinitialisé.
     *
     * Si aucune partie n'est en cours, seul le joueur
     * concerné est retiré de la liste des participants.
     *
     * @param utilisateur utilisateur à déconnecter
     */
    public void traiterDeconnexion(ConnexionUtilisateur utilisateur) {
        try {
            if (utilisateur.getSocket() != null) {
                utilisateur.getSocket().close();
            }

            utilisateur.getThreadConnexion().fin();

            if (getSuperviseur().getPartieCommencee()) {
                envoyerMessageAToutLeMonde("@ERREUR_DECONNEXION "
                        + utilisateur.getJoueur().getNom()
                        + " s'est déconnecté durant la partie");
                getSuperviseur().gestionFinPartie();
            }
            else if (utilisateur.getJoueur() != null) {
                envoyerMessageAuxAutresJoueurs(utilisateur,
                        "@DECONNEXION " + utilisateur.getJoueur().getNom());
            }

            getSuperviseur().getListeJoueurs().remove(utilisateur.getJoueur());
            this.remove(utilisateur);
        } catch (ServeurException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
