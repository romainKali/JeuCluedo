package ihm;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import bdd.DaoCarnet;
import bdd.DatabaseManager;
import enums.Carte;
import enums.EArme;
import enums.ELieu;
import enums.EPersonnage;
import enums.Proposition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import reseau.ThreadConsole;
import reseau.experts.Expert;
import reseau.experts.chat.ExpertMessagePrivate;
import reseau.experts.chat.ExpertMessagePublic;
import reseau.experts.erreur.ExpertErreur;
import reseau.experts.erreur.ExpertErreurDeconnexion;
import reseau.experts.erreur.ExpertErreurGST;
import reseau.experts.erreur.ExpertErreurParam;
import reseau.experts.general.ExpertConnexion;
import reseau.experts.general.ExpertDemarrer;
import reseau.experts.general.ExpertDeplacer;
import reseau.experts.notifications.ExpertDefaite;
import reseau.experts.notifications.ExpertElim;
import reseau.experts.notifications.ExpertFinTour;
import reseau.experts.notifications.ExpertVictoire;
import reseau.experts.personnel.ExpertCartes;
import reseau.experts.personnel.ExpertLancerDes;
import reseau.experts.personnel.ExpertPostConnexion;
import reseau.experts.soupcon.ExpertCarteRefutante;
import reseau.experts.soupcon.ExpertInformationRefutation;
import reseau.experts.soupcon.ExpertSoupconARefuter;
import reseau.experts.soupcon.ExpertSoupconNonRefute;
import reseau.experts.soupcon.ExpertSoupconRefuted;
import reseau.experts.soupcon.ExpertSoupconner;

/**
 * Classe principale de l'application cliente Cluedo.
 * <p>
 * Cette classe orchestre le démarrage de l'interface graphique (JavaFX),
 * la gestion de la connexion réseau, le traitement des messages serveurs via une chaîne
 * d'experts (Pattern Chain of Responsibility) et la synchronisation de l'état du jeu.
 * </p>
 */
public class InterfaceMain extends Application {

	/// --- Paramètres de connexion ---
	public static final String SERVEUR = "localhost";
	public static final int PORT = 4567;

	public static final String BDD_USER = "root";
	public static final String BDD_PASS = "";

	/// --- Ressources graphiques et configuration ---
	public static final String PLATEAU_CLUEDO = "/PlateauCluedo.png";
	public static final String CONFIG_XML = "/ihm/config.xml";
	public static final int LTAILLE_FENETRE = 1400;
	public static final int HTAILLE_FENETRE = 1000;

	/// --- Constantes des Pions ---
	public static final String PION_MOUTARDE = "/pions/Col_Moutarde.png";
	public static final String PION_ROSE = "/pions/Mlle_Rose.png";
	public static final String PION_LEBLANC = "/pions/Mme_Leblanc.png";
	public static final String PION_OLIVE = "/pions/Rev_Olive.png";
	public static final String PION_PERVENCHE = "/pions/Mme_Pervenche.png";
	public static final String PION_VIOLET = "/pions/Prof_Violet.png";

	/** Map associant chaque personnage à l'image de son pion sur le plateau. */
	public static final Map<EPersonnage, Image> PION_MAP = new EnumMap<>(EPersonnage.class);

	/**
	 * Initialise la Map liant les personnages (EPersonnage) à leurs images de pions respectives.
	 */
	private void initPionMap() {
		PION_MAP.put(EPersonnage.Colonel_Moutarde,   new Image(getClass().getResourceAsStream(PION_MOUTARDE)));
		PION_MAP.put(EPersonnage.Mademoiselle_Rose,  new Image(getClass().getResourceAsStream(PION_ROSE)));
		PION_MAP.put(EPersonnage.Madame_Leblanc,     new Image(getClass().getResourceAsStream(PION_LEBLANC)));
		PION_MAP.put(EPersonnage.Reverend_Olive,     new Image(getClass().getResourceAsStream(PION_OLIVE)));
		PION_MAP.put(EPersonnage.Madame_Pervenche,   new Image(getClass().getResourceAsStream(PION_PERVENCHE)));
		PION_MAP.put(EPersonnage.Professeur_Violet,  new Image(getClass().getResourceAsStream(PION_VIOLET)));
	}

	/// --- Constantes des Cartes ---
	public static final String P_CARTE_MOUTARDE = "/cartes_Personnage/Col_Moutarde.png";
	public static final String P_CARTE_ROSE = "/cartes_Personnage/Mlle_Rose.png";
	public static final String P_CARTE_LEBLANC = "/cartes_Personnage/Mme_Leblanc.png";
	public static final String P_CARTE_OLIVE = "/cartes_Personnage/Rev_Olive.png";
	public static final String P_CARTE_PERVENCHE = "/cartes_Personnage/Mme_Pervenche.png";
	public static final String P_CARTE_VIOLET = "/cartes_Personnage/Prof_Violet.png";

	public static final String L_CARTE_BIBLIOTHEQUE = "/cartes_Lieu/Bibliotheque.png";
	public static final String L_CARTE_BUREAU = "/cartes_Lieu/Bureau.png";
	public static final String L_CARTE_CUISINE = "/cartes_Lieu/Cuisine.png";
	public static final String L_CARTE_HALL = "/cartes_Lieu/Hall.png";
	public static final String L_CARTE_SALLE_BAL = "/cartes_Lieu/Salle_Bal.png";
	public static final String L_CARTE_SALLE_BILLARD = "/cartes_Lieu/Salle_Billard.png";
	public static final String L_CARTE_SALLE_MANGER = "/cartes_Lieu/Salle_Manger.png";
	public static final String L_CARTE_SALON = "/cartes_Lieu/Salon.png";
	public static final String L_CARTE_VERANDA = "/cartes_Lieu/Veranda.png";

	public static final String A_CARTE_CHANDELIER = "/cartes_Arme/Chandelier.png";
	public static final String A_CARTE_CLE_ANGLAISE = "/cartes_Arme/Cle_Anglaise.png";
	public static final String A_CARTE_CORDE = "/cartes_Arme/Corde.png";
	public static final String A_CARTE_MATRAQUE = "/cartes_Arme/Matraque.png";
	public static final String A_CARTE_POIGNARD = "/cartes_Arme/Poignard.png";
	public static final String A_CARTE_REVOLVER = "/cartes_Arme/Revolver.png";

	public final static Map<EPersonnage, Image> PERSON_MAP = new EnumMap<>(EPersonnage.class);
	public final static Map<EArme, Image> ARME_MAP = new EnumMap<>(EArme.class);
	public final static Map<ELieu, Image> LIEU_MAP = new EnumMap<>(ELieu.class);

	private MediaPlayer musiqueAmbiance;

	/**
	 * Initialise les Maps associant les éléments du jeu (Personnages, Armes, Lieux)
	 * à leurs images de cartes respectives.
	 */
	private void initMap() {
		// Mapping Characters (EPersonnage)
		PERSON_MAP.put(EPersonnage.Mademoiselle_Rose, new Image(getClass().getResourceAsStream(P_CARTE_ROSE)));
		PERSON_MAP.put(EPersonnage.Colonel_Moutarde, new Image(getClass().getResourceAsStream(P_CARTE_MOUTARDE)));
		PERSON_MAP.put(EPersonnage.Madame_Leblanc, new Image(getClass().getResourceAsStream(P_CARTE_LEBLANC)));
		PERSON_MAP.put(EPersonnage.Reverend_Olive, new Image(getClass().getResourceAsStream(P_CARTE_OLIVE)));
		PERSON_MAP.put(EPersonnage.Madame_Pervenche, new Image(getClass().getResourceAsStream(P_CARTE_PERVENCHE)));
		PERSON_MAP.put(EPersonnage.Professeur_Violet, new Image(getClass().getResourceAsStream(P_CARTE_VIOLET)));

		// Mapping Weapons (EArme)
		ARME_MAP.put(EArme.Poignard, new Image(getClass().getResourceAsStream(A_CARTE_POIGNARD)));
		ARME_MAP.put(EArme.Revolver, new Image(getClass().getResourceAsStream(A_CARTE_REVOLVER)));
		ARME_MAP.put(EArme.Chandelier, new Image(getClass().getResourceAsStream(A_CARTE_CHANDELIER)));
		ARME_MAP.put(EArme.Corde, new Image(getClass().getResourceAsStream(A_CARTE_CORDE)));
		ARME_MAP.put(EArme.Cle_anglaise, new Image(getClass().getResourceAsStream(A_CARTE_CLE_ANGLAISE)));
		ARME_MAP.put(EArme.Matraque, new Image(getClass().getResourceAsStream(A_CARTE_MATRAQUE)));

		// Mapping Locations (ELieu)
		LIEU_MAP.put(ELieu.Bureau, new Image(getClass().getResourceAsStream(L_CARTE_BUREAU)));
		LIEU_MAP.put(ELieu.Bibliotheque, new Image(getClass().getResourceAsStream(L_CARTE_BIBLIOTHEQUE)));
		LIEU_MAP.put(ELieu.Salle_de_billard, new Image(getClass().getResourceAsStream(L_CARTE_SALLE_BILLARD)));
		LIEU_MAP.put(ELieu.Veranda, new Image(getClass().getResourceAsStream(L_CARTE_VERANDA)));
		LIEU_MAP.put(ELieu.Salle_de_bal, new Image(getClass().getResourceAsStream(L_CARTE_SALLE_BAL)));
		LIEU_MAP.put(ELieu.Hall, new Image(getClass().getResourceAsStream(L_CARTE_HALL)));
		LIEU_MAP.put(ELieu.Salon, new Image(getClass().getResourceAsStream(L_CARTE_SALON)));
		LIEU_MAP.put(ELieu.Salle_a_manger, new Image(getClass().getResourceAsStream(L_CARTE_SALLE_MANGER)));
		LIEU_MAP.put(ELieu.Cuisine, new Image(getClass().getResourceAsStream(L_CARTE_CUISINE)));
	}

	/// --- Constantes et utilitaires Audio ---
	public final static String SON_AMBIANCE = "/sons/musique_Ambiance.mp3";
	public final static String SON_LANCER_DES = "/sons/Lancer_De.mp3";
	public final static String SON_PORTE = "/sons/Door_Open.mp3";
	public final static String SON_NOTIFICATION = "/sons/Ding.mp3";
	public final static String SON_REFUTER = "/sons/Glissement_De_Papier.mp3";

	public final static Media MEDIA_AMBIANCE = initMediaEach(SON_AMBIANCE);

	public static final AudioClip SFX_LANCER_DES = loadAudioClip(SON_LANCER_DES);
	public static final AudioClip SFX_PORTE = loadAudioClip(SON_PORTE);
	public static final AudioClip SFX_NOTIFICATION = loadAudioClip(SON_NOTIFICATION);
	public static final AudioClip SFX_REFUTER = loadAudioClip(SON_REFUTER);

	private static Media initMediaEach(String PATH) {
		try {
			Platform.startup(() -> {});
		} catch (IllegalStateException e) {}

		URL cheminSon = AudioManager.class.getResource(PATH);
		if (cheminSon != null)
			return new Media(cheminSon.toExternalForm());
		else
			System.err.println("Fichier son introuvable !" + PATH);
		return null;
	}

	private static AudioClip loadAudioClip(String path) {
		URL cheminSon = AudioManager.class.getResource(path);

		if (cheminSon != null) {
			return new AudioClip(cheminSon.toExternalForm());
		} else {
			System.err.println("Fichier son introuvable !" + path);
			return null;
		}
	}

	/// --- Expressions Régulières (Regex) du protocole réseau ---
	// Personnels
	public static final String REGEX_CONNEXION_REUSSI_C = "^@CONNEXION_REUSSIE$";
	public static final String REGEX_LANCER_DES_REUSSI  = "^@LANCER_DES_REUSSI \\d+$";
	public static final String REGEX_POSTCON = "^@INFOS_AUTRESJOUEURS( [\\p{Alnum}_]+ P\\d+)*$";
	public static final String REGEX_VOS_CARTES = "^@VOS_CARTES( [PAL]\\d+)+$";

	// Générales
	public static final String REGEX_PARTIE_COMMENCEE = "^@PARTIE_COMMENCEE [\\p{Alnum}_]+( [\\p{Alnum}_]+ \\d+ \\d+)+$";
	public static final String REGEX_CONNEXION_REUSSI_G = "^@CONNEXION [\\p{Alnum}_]+ P\\d+$";
	public static final String REGEX_DEPLACEMENT_REUSSI = "^@DEPLACEMENT_REUSSI ([\\p{Alnum}_]+) (\\d+) (\\d+)$";
	public static final String REGEX_DECONNEXION = "^@DECONNEXION [\\p{Alnum}_]+$";

	// Soupçons
	public static final String REGEX_SOUPCON_EMIS = "^@SOUPCON_EMIS [\\p{Alnum}_]+ P\\d+ A\\d+ L\\d+$";
	public static final String REGEX_SOUPCON_REFUTE = "^@SOUPCON_REFUTE [\\p{Alnum}_]+$";
	public static final String REGEX_SOUPCON_A_REFUTER = "^@SOUPCON_A_REFUTER .+$";
	public static final String REGEX_CARTE_REFUTANT_SOUPCON = "^@CARTE_REFUTANT_SOUPCON [PAL]\\d+ [\\p{Alnum}_]+$";
	public static final String REGEX_SOUPCON_NON_REFUTE = "^@SOUPCON_NON_REFUTE [\\p{Alnum}_]+$";

	// Chat
	public static final String REGEX_PUBLIC_FROM  = "^@PUBLIC_FROM [\\p{Alnum}_]+ .+$";
	public static final String REGEX_MP_FROM      = "^@MP_FROM [\\p{Alnum}_]+ .+$";

	// Notifications
	public static final String REGEX_FIN_TOUR = "^@FIN_TOUR [\\p{Alnum}_]+$";
	public static final String REGEX_JOUEUR_ELIM = "^@JOUEUR_ELIM .+$";
	public static final String REGEX_VICTOIRE = "^@VICTOIRE [\\p{Alnum}_]+$";
	public static final String REGEX_DEFAITE  = "^@DEFAITE .+$";

	// Erreurs
	public static final String REGEX_ERREUR       = "^@ERREUR .+$";
	public static final String REGEX_ERREUR_PARAM = "^@ERREUR_PARAM .+$";
	public static final String REGEX_ERREUR_DECONNEXION = "^@ERREUR_DECONNEXION .+$";
	public static final String REGEX_ERREUR_GST_PARTIE = "^@ERREUR_GST_PARTIE$";

	/// --- Chemins FXML ---
	public static final String LOGIN_FXML = "/ihm/fxmls/login.fxml";
	public static final String MAIN_FXML = "/ihm/fxmls/main.fxml";
	public static final String BDD_FXML = "/ihm/fxmls/bdd.fxml";
	public static final String SOUPCON_FXML = "/ihm/fxmls/soupcon.fxml";
	public static final String ACCUSE_FXML = "/ihm/fxmls/accuse.fxml";
	public static final String REFUTER_FXML = "/ihm/fxmls/refuter.fxml";
	public static final String PUBLIC_M_FXML = "/ihm/fxmls/publicmessage.fxml";
	public static final String PRIVATE_M_FXML = "/ihm/fxmls/privatemessage.fxml";
	public static final String PARAMETRE_FXML = "/ihm/fxmls/parametre.fxml";

	/// --- Champs d'état du client ---
	private ThreadConsole reseau;
	private ControllerMain controller;
	private DatabaseManager dbManager;

	private String nom;
	private EPersonnage personnage;
	private boolean estElimine = false;

	private Proposition soupcon = null;
	private Boolean refutage = null;
	private String emetteurSoupcon = null;

	private Expert chaineExperts;

	private ArrayList<Carte> lescartes = new ArrayList<Carte>();

	private Map<String, EPersonnage> pseudoToPersonnage = new HashMap<>();
	private Map<EPersonnage, String> personnageToPseudo = new EnumMap<>(EPersonnage.class);

	// Caches pour optimisation de la conversion de cartes
	private static final EPersonnage[] CACHE_PERSONNAGES = EPersonnage.values();
	private static final EArme[] CACHE_ARMES = EArme.values();
	private static final ELieu[] CACHE_LIEUX = ELieu.values();

	/**
	 * Convertit une carte (Personnage, Arme, Lieu) en une chaîne de caractères
	 * lisible par le protocole réseau (ex: P0, A1, L2).
	 * * @param c La carte à convertir.
	 * @return La représentation textuelle de la carte, ou "null" si la carte est inexistante.
	 */
	public static String carteToString(Carte c) {
		if (c == null) {
			return "null";
		}

		switch (c) {
			case EPersonnage p : return "P" + p.ordinal();
			case EArme a : return "A" + a.ordinal();
			case ELieu l : return "L" + l.ordinal();
			default : return null;
		}
	}

	/**
	 * Convertit une chaîne de caractères issue du protocole réseau (ex: "P0", "A1")
	 * en objet Carte correspondant.
	 * * @param nom La chaîne de caractères à parser.
	 * @return L'objet Carte correspondant, ou null en cas d'erreur/format invalide.
	 */
	public static Carte stringToCarte(String nom) {
		if (nom == null) return null;

		switch(nom.charAt(0)) {
			case 'P' : return CACHE_PERSONNAGES[Integer.parseInt(nom.substring(1))];
			case 'A' : return CACHE_ARMES[Integer.parseInt(nom.substring(1))];
			case 'L' : return CACHE_LIEUX[Integer.parseInt(nom.substring(1))];
		}
		return null;
	}

	/**
	 * Récupère la liste des pseudos des joueurs actuellement dans la partie.
	 * * @return Une liste contenant les pseudos des joueurs.
	 */
	public ArrayList<String> getListeJoueurs() {
		return new ArrayList<>(pseudoToPersonnage.keySet());
	}

	// --- Getters & Setters ---

	public String getNom() { return nom; }
	public void setNom(String moiLocal) { this.nom = moiLocal; }
	public ControllerMain getController() { return controller; }
	public Proposition getSoupcon() { return soupcon; }

	/** @return true si le joueur possède l'arme actuellement soupçonnée. */
	public boolean hasArmeSoupconnée() { return lescartes.contains(soupcon.getArme()); }
	/** @return true si le joueur possède le lieu actuellement soupçonné. */
	public boolean hasLieuSoupconnée() { return lescartes.contains(soupcon.getLieu()); }
	/** @return true si le joueur possède le personnage actuellement soupçonné. */
	public boolean hasPersonnageSoupconnée() { return lescartes.contains(soupcon.getPersonnage()); }

	/** Réinitialise les variables liées aux soupçons pour un nouveau tour. */
	public void resetSoupconEtRefutage() {
		soupcon = null;
		refutage = null;
		emetteurSoupcon = null;
	}

	public void setRefutageTrue() { refutage = true; }
	public boolean peutRefuter() { return refutage != null && !refutage; }
	public Boolean getRefutage() { return refutage; }

	/**
	 * Initialise la Chaîne de Responsabilité (Chain of Responsibility) composée d'experts.
	 * Chaque expert est chargé d'intercepter et de traiter une expression régulière (Regex)
	 * spécifique reçue du serveur.
	 */
	private void initExperts() {
		// 1. Création des instances d'experts (suivant l'ordre des REGEX)

		// Personnels
		Expert expLancerDes = new ExpertLancerDes();
		Expert expPostConnexion = new ExpertPostConnexion();
		Expert expCartes = new ExpertCartes();

		// Générales
		Expert expDemarrer = new ExpertDemarrer();
		Expert expConnexion = new ExpertConnexion();
		Expert expDeplacer = new ExpertDeplacer();

		// Soupçons
		Expert expSoupconner = new ExpertSoupconner();
		Expert expRefuterSoupcon = new ExpertSoupconRefuted();
		Expert expSoupconARefuter = new ExpertSoupconARefuter();
		Expert expCarteRefutante = new ExpertCarteRefutante();
		Expert expInformationSoupcon = new ExpertInformationRefutation();
		Expert expSoupconNonRefute = new ExpertSoupconNonRefute();

		// Chat
		Expert expPublic = new ExpertMessagePublic();
		Expert expPrivate = new ExpertMessagePrivate();

		// Notifications
		Expert expFinTour = new ExpertFinTour();
		Expert expElim = new ExpertElim();
		Expert expVictoire = new ExpertVictoire();
		Expert expDefaite = new ExpertDefaite();

		// Erreurs
		Expert expErreur = new ExpertErreur();
		Expert expErreurParam = new ExpertErreurParam();
		Expert expErreurDeconnexion = new ExpertErreurDeconnexion();
		Expert expErreurGST = new ExpertErreurGST();

		// 2. Maillage de la chaîne (Chaînage séquentiel)
		expLancerDes.setSuivant(expPostConnexion);
		expPostConnexion.setSuivant(expDemarrer);

		expDemarrer.setSuivant(expConnexion);
		expConnexion.setSuivant(expDeplacer);
		expDeplacer.setSuivant(expSoupconner);

		expSoupconner.setSuivant(expRefuterSoupcon);
		expRefuterSoupcon.setSuivant(expSoupconARefuter);
		expSoupconARefuter.setSuivant(expCarteRefutante);
		expCarteRefutante.setSuivant(expPublic);

		expPublic.setSuivant(expPrivate);
		expPrivate.setSuivant(expFinTour);

		expFinTour.setSuivant(expElim);
		expElim.setSuivant(expVictoire);
		expVictoire.setSuivant(expDefaite);
		expDefaite.setSuivant(expErreur);

		expErreurGST.setSuivant(expErreurParam);
		expErreurParam.setSuivant(expErreurDeconnexion);
		expErreurDeconnexion.setSuivant(expErreur);

		expErreur.setSuivant(expCartes);

		expCartes.setSuivant(expInformationSoupcon);
		expInformationSoupcon.setSuivant(expSoupconNonRefute);

		// 3. Stockage du premier maillon
		chaineExperts = expLancerDes;
	}

	/**
	 * Constructeur par défaut. Initialise la base de données, charge la configuration,
	 * initialise les experts pour le réseau et prépare les ressources graphiques.
	 */
	public InterfaceMain() {
		dbManager =  new DatabaseManager();
		Config.load();
		initExperts();
		initMap();
		initPionMap();
	}

	/**
	 * Affecte le thread réseau (ThreadConsole) à l'application cliente et le démarre.
	 * * @param c L'instance de ThreadConsole à attacher.
	 */
	public void setReseau(ThreadConsole c) {
		if (c == null)
			System.err.println("Le reseau voulu est null !");
		reseau = c;
		reseau.start();
	}

	/**
	 * Point d'entrée standard du programme.
	 * * @param args Arguments en ligne de commande.
	 */
	public static void Main(String[] args) {
		launch(args);
	}

	/**
	 * Affiche la boîte de dialogue de connexion permettant au joueur de choisir
	 * son pseudonyme et son personnage.
	 * * @return Le LoginController associé à la vue, ou null si l'action a été annulée.
	 */
	private LoginController dialogueLogin() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
			BorderPane loginRoot = loader.load();

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Login to System");
			dialog.getDialogPane().setContent(loginRoot);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.OK) {
				return loader.getController();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Affiche une alerte si la connexion échoue et propose de réessayer ou de quitter.
	 * * @return true si l'utilisateur souhaite réessayer, false sinon.
	 */
	private boolean showRetryOrExitAlert() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Login Required");
		alert.setHeaderText("No login information provided.");
		alert.setContentText("Would you like to try again or exit the application?");

		ButtonType retryButton = new ButtonType("Retry");
		ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(retryButton, exitButton);

		Optional<ButtonType> option = alert.showAndWait();

		return option.isPresent() && option.get() == retryButton;
	}

	/**
	 * Affiche une fenêtre d'alerte d'erreur générique.
	 * * @param header Titre de l'erreur.
	 * @param content Message de l'erreur.
	 */
	private void showErrorAlert(String header, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * Point d'entrée JavaFX. Gère la boucle de connexion puis lance l'interface principale.
	 * * @param primaryStage La fenêtre principale de l'application.
	 * @throws Exception Si le chargement des vues FXML échoue.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		reseau = new ThreadConsole(this);
		LoginController controller = null;
		boolean connected = false;

		while(!connected) {
			controller = dialogueLogin();

			if (controller == null ) {
				if (showRetryOrExitAlert()) continue;
				else  {
					Platform.exit();
					return;
				}
			}

			nom = controller.getNom();
			personnage = controller.getPersonnage();

			if (nom == null ||  personnage == null) {
				System.err.println("nom/personnage null");
				Platform.exit();
			}
			if (nom.isBlank()) continue;

			connexion(nom , personnage);
			String msg = reseau.recevoirMessage();

			if (msg != null &&  msg.matches(REGEX_CONNEXION_REUSSI_C)) {
				reseau.start();
				connected = true;
			}
			else if (msg == null) {
				Platform.exit();
			}
			else {
				String message = msg.substring(msg.indexOf(" ") + 1);
				if (message.equals("Votre pseudo et votre personnage sont déjà utilisés") 
					|| message.equals("Le personnage que vous avez choisi est déjà utilisé") 
					|| message.equals("Le pseudo que vous avez choisi est déjà utilisé") 
					) {
				showErrorAlert("Erreur durant le Login", message + "\nVeuillez changer vos choix, Ou quittez si vous voulez.");
			}
			else {
				showErrorAlert("Login Impossible", message);
				Platform.exit();
				return;
			}
		}
		}

		dbManager.enregistrerJoueur(nom);
		addJoueur(nom, personnage);

		FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));
		primaryStage.setScene(new Scene(loader.load(), LTAILLE_FENETRE, HTAILLE_FENETRE));
		this.controller = loader.getController();
		this.controller.setleMain(this);

		primaryStage.show();
	
	}

	/**
	 * Nettoie les ressources lors de la fermeture de l'application (BDD, Audio, Réseau).
	 */
	@Override
	public void stop() {
		DaoCarnet.viderCarnet();
		AudioManager.arreterMusiqueAmbiance();
		if (dbManager != null) {
			dbManager.deconnecter(nom);
		}
		deconnexion();
	}

	/**
	 * Réinitialise l'état du jeu (fin de partie ou erreur grave du serveur).
	 */
	private void resetJeu() {
		lescartes.clear();
		estElimine = false;
		Platform.runLater( () -> {
			controller.resetJeu();
		});
		DaoCarnet.viderCarnet();
	}

	/// --- TRAITEMENT DES EXPERTS (RÉCEPTION DES MESSAGES) ---

	/**
	 * Délègue le traitement du message reçu à la chaîne de responsabilité (Experts).
	 * * @param message Le message réseau brut envoyé par le serveur.
	 */
	public void traiterMessage(String message) {
		chaineExperts.traiter(message, this);
	}

	public void privateMessage(String emetteur, String content) {
		controller.printConsoleLine("Message Privé de : " + emetteur + ", contenu : " + content , null);
	}

	public void publicMessage(String emetteur, String content) {
		controller.printConsoleLine("Message public de : " + emetteur + ", contenu : " + content, null);
	}

	public void demarrerPartie(String s) {
		controller.setJoueurCourant(s);

		if (s.equals(nom)) controller.setStatut("C'est a vous !");
		else controller.setStatut("En attente.");

		controller.printConsoleLine("Partie demarrée !", null);
	}

	public void deplacer(String p,int x, int y) {
		Platform.runLater( () -> {
			controller.bougePion(pseudoToPersonnage.get(p),x, y);
		});
	}

	public void initPion(String p,int x, int y) {
		Platform.runLater(() -> {
			controller.initPion(pseudoToPersonnage.get(p), x, y);
		});
	}

	public void lancerdes(int a) {
		controller.printConsoleLine("Dés Lancés ! Le resultat est : " + a, null);
	}

	/**
	 * Ajoute un joueur dans les correspondances pseudo/personnage du système.
	 * * @param pseudo Le pseudonyme du joueur.
	 * @param personnage Le personnage incarné par le joueur.
	 */
	public void addJoueur(String pseudo,EPersonnage personnage) {
		pseudoToPersonnage.put(pseudo,personnage);
		personnageToPseudo.put(personnage, pseudo);
	}

	public void soupconner(String emetteur, EPersonnage personnage, EArme arme, ELieu lieu) {
		soupcon = new Proposition(lieu, personnage, arme);
		emetteurSoupcon = emetteur;
		if (emetteur.equals(nom)) controller.printConsoleLine("Votre soupcon a bien été prise en compte.", null);
		else controller.printConsoleLine("Soupcon emis par : " + emetteur +
						"Personnage : " + personnage +
						"Arme : " + arme +
						"Lieu : " + lieu
				, null);
	}

	public void defaite() {
		controller.printConsoleLine("Defaite! personne n'a reussi", Color.DARKRED);
		resetJeu();
	}

	public void victoire(String pseudo) {
		if (pseudo.equals(nom))
			controller.printConsoleLine("Felicitations ! vous avez gagné !", Color.GOLD);
		else
			controller.printConsoleLine("Dommage.... " + pseudo + " a reussi a trouver l'enigme avant vous !", Color.GREEN);
		resetJeu();
	}

	public void printerr(String message) {
		controller.printErrorLine(message, null);
	}

	public void printConsole(String msg, Color c) {
		controller.printConsoleLine(msg, c);
	}

	public void prochainTour(String pseudo_courant) {
		if (pseudo_courant.equals(nom)) {
			controller.printConsoleLine("C'est a vous !",null);
			controller.setStatut("A vous !");
		}
		else {
			controller.printConsoleLine("Cest le tour de " + pseudo_courant, null);
			if (!estElimine) controller.setStatut("En attente.");
		}
		controller.setJoueurCourant(pseudo_courant);
	}

	public void soupconARefuter() {
		controller.printConsoleLine("VOUS DEVEZ REFUTER CE SOUPCON ! MEME SI VOUS N'AVEZ PAS LES CARTES SOUPCONÉES.", Color.RED);
		refutage = false;
		Platform.runLater(() -> controller.refuterSoupcon(null));
	}

	public void traiterElimination(String pseudo) {
		if (pseudo.equals(nom)) {
			controller.printConsoleLine("Vous etes eliminé !",null);
			controller.setStatut("Éliminé");
			estElimine = true;
		}
		else
			controller.printConsoleLine(pseudo + " a été eliminé !",null);

		Platform.runLater(() -> {
			controller.supprimerPion(pseudoToPersonnage.get(pseudo));
		});
	}

	public void handleErreurGST(String message) {
		Platform.runLater( () -> {
			resetJeu();
		});
	}

	public void initCartes(String carteString ) {
		String[] cartes = carteString.split(" ");
		for(int i = 0 ; i < cartes.length ; i += 1) {
			lescartes.add(stringToCarte(cartes[i]));
		}
		controller.afficherCartes(lescartes);
	}

	public void traiterDeconnexion(String pseudo, boolean enCours) {
		personnageToPseudo.remove(pseudoToPersonnage.get(pseudo));
		pseudoToPersonnage.remove(pseudo);
		if (enCours) resetJeu();
	}

	/// --- CONTRE-TRAITEMENT (ÉMISSION DES MESSAGES) ---

	/** Envoie une demande de connexion au serveur avec le nom et le personnage choisi. */
	public void connexion(String nom, EPersonnage e) {
		reseau.envoyerMessage("@CONNEXION " + nom + " " + carteToString(e));
	}

	/** Envoie une notification de déconnexion au serveur. */
	public void deconnexion() {
		reseau.envoyerMessage("@DECONNEXION");
	}

	/** Notifie le serveur de la fin du tour courant du joueur. */
	public void mettreFinAuTour() {
		reseau.envoyerMessage("@FIN_TOUR");
	}

	/**
	 * Envoie un message privé à un joueur spécifique si la configuration l'autorise.
	 * * @param receiver Le pseudo du destinataire.
	 * @param msg Le contenu du message.
	 */
	public void privateMessageSend(String receiver,String msg) {
		if (!Config.message_prive) {
			controller.printErrorLine("Messages privés désactivés", null);
			return;
		}
		reseau.envoyerMessage("@MP_TO " + receiver + " " + msg);
	}

	/**
	 * Envoie un message public à tous les joueurs si la configuration l'autorise.
	 * * @param msg Le contenu du message.
	 */
	public void publicMessageSend(String msg) {
		if (!Config.message_public) {
			controller.printErrorLine("Messages publics désactivés", null);
			return;
		}
		reseau.envoyerMessage("@TO_ALL " + msg);
	}

	/** Démarre la partie depuis le client. */
	public void demarrer() {
		reseau.envoyerMessage("@DEMARRER");
	}

	/** Demande au serveur de lancer les dés. */
	public void lancerDesSend() {
		if (!Config.des) {
			controller.printErrorLine("Lancer de dés désactivé", null);
			return;
		}
		reseau.envoyerMessage("@LANCER_DES");
	}

	/**
	 * Demande un déplacement du pion vers les coordonnées ciblées.
	 * * @param x Coordonnée X de destination.
	 * @param y Coordonnée Y de destination.
	 */
	public void deplacerSend(int x, int y) {
		if (!Config.mouvement) {
			controller.printErrorLine("Mouvement désactivé", null);
			return;
		}
		reseau.envoyerMessage("@ALLER_VERS " + x + " " + y);
	}

	/**
	 * Émet un soupçon auprès du serveur.
	 * * @param ePersonnage Le personnage suspecté.
	 * @param arme L'arme suspectée.
	 */
	public void soupconnerSend(EPersonnage ePersonnage , EArme arme) {
		if (!Config.soupcon) {
			controller.printErrorLine("Soupçons désactivés", null);
			return;
		}
		reseau.envoyerMessage("@SOUPCONNER " + carteToString(ePersonnage) + " " + carteToString(arme));
	}

	/**
	 * Émet une accusation finale auprès du serveur.
	 * * @param personnage Le personnage accusé.
	 * @param arme L'arme accusée.
	 * @param lieu Le lieu accusé.
	 */
	public void accuserSend(EPersonnage personnage, EArme arme, ELieu lieu) {
		if (!Config.accusation) {
			controller.printErrorLine("Accusations désactivées", null);
			return;
		}
		reseau.envoyerMessage("@ACCUSER " + carteToString(personnage) + " " + carteToString(arme) + " " + carteToString(lieu));
	}

	/**
	 * Réfute un soupçon émis par un autre joueur en présentant une carte.
	 * * @param carte La carte qui permet d'invalider le soupçon.
	 */
	public void refuterSoupcon(Carte carte) {
		reseau.envoyerMessage("@INDICE " +  carteToString(carte));
		refutage = true;
	}


}