package ihm;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import enums.Carte;
import enums.EArme;
import enums.ELieu;
import enums.EPersonnage;
import ihm.main_controllers.AccuseController;
import ihm.main_controllers.PrivateMessageController;
import ihm.main_controllers.PublicMessageController;
import ihm.main_controllers.RefuterController;
import ihm.main_controllers.SoupconController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Contrôleur principal de l'interface graphique JavaFX (main.fxml).
 * <p>
 * Gère l'affichage du plateau de jeu, la console de chat/événements, les logs d'erreurs,
 * ainsi que les interactions de l'utilisateur (déplacements, lancers de dés, soupçons,
 * accusations et messages). Il communique directement avec l'instance de {@link InterfaceMain}.
 * </p>
 */
public class ControllerMain {
	// tous les champs sont publiques pour les tests commandés par S.

	/** Conteneur racine de l'interface principale. */
	@FXML
	public BorderPane root;

	/** Label affichant le statut actuel du joueur (ex: "A vous !", "En attente"). */
	@FXML
	public Label status; // TODO !

	/** Label affichant le pseudonyme du joueur dont c'est actuellement le tour. */
	@FXML
	public Label joueurCourant; // TODO !

	/** Lecteur média pour la musique d'ambiance du jeu. */
	private MediaPlayer musiqueAmbiance;

	/** Boutons de l'interface permettant d'exécuter les actions de jeu. */
	@FXML
	public Button demarrer,
			bdd,
			refuter,
			lancedes,
			soupcon,
			accuse,
			fintour,
			parameteres;


	/** Panneau défilant contenant l'historique des erreurs. */
	@FXML
	public ScrollPane capsulErreur;
	/** Zone de texte riche pour l'affichage dynamique des erreurs. */
	@FXML
	public TextFlow affichagErreur;


	/** Panneau défilant contenant la console d'événements du jeu. */
	@FXML
	public ScrollPane capsulJeu;
	/** Zone de texte riche pour l'affichage des actions, notifications et messages du chat. */
	@FXML
	public TextFlow consoleJeu;


	/** Conteneur du plateau de jeu visuel. */
	@FXML
	public BorderPane plateau;

	/** Grille représentant les cases du plateau pour le positionnement des pions. */
	public GridPane lespions;

	/** Zone d'affichage horizontale contenant les cartes que le joueur possède en main. */
	@FXML
	public HBox zoneCartes;


	/**
	 * Initialise le contrôleur après le chargement du fichier FXML.
	 * Configure les ScrollPanes, prépare la grille de déplacement (GridPane) pour le plateau,
	 * gère les clics pour le déplacement des pions et lance la musique d'ambiance.
	 */
	@FXML
	public void initialize() {

		Platform.runLater(() -> capsulErreur.setVvalue(1.0));
		Platform.runLater(() -> capsulJeu.setVvalue(1.0));

		plateau.setMinSize(750, 750);
		plateau.setMaxSize(750, 750);

		initGrid();
		
		plateau.setCenter(lespions);

		BorderPane.setAlignment(lespions, javafx.geometry.Pos.TOP_LEFT);
		BorderPane.setMargin(lespions, new javafx.geometry.Insets(34, 0, 0, 37));

		initPionMap();
		AudioManager.jouerMusiqueAmbiance();
	}



	/**
	 * Action déclenchée par le bouton de démarrage.
	 * Envoie la requête au serveur pour commencer la partie et joue un effet sonore.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void demarrage(ActionEvent e) {
		leMain.demarrer();
		AudioManager.jouerSonPorte();
	}

	/**
	 * Action déclenchée par le bouton de lancer de dés.
	 * Demande un lancer de dés au serveur et joue un effet sonore.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void lancerDes(ActionEvent e) {
		leMain.lancerDesSend();
		AudioManager.joueurSonDes();
	}

	/**
	 * Action déclenchée par le bouton de fin de tour.
	 * Prévient le serveur que le joueur courant termine ses actions.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void mettreFinAuTour(ActionEvent e) {
		leMain.mettreFinAuTour();
		AudioManager.jouerSonNotification();
	}

	/**
	 * Action pour envoyer un message public à tous les joueurs.
	 * Ouvre une boîte de dialogue de saisie et transmet le message au gestionnaire réseau.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void messagePublic(ActionEvent e) {
		PublicMessageController controller = dialoguePublicMessage();

		if (controller == null) {
			printErrorLine("Message Publique echoué", null);
			return;
		}

		leMain.publicMessageSend(controller.getText());
		AudioManager.jouerSonNotification();
	}

	/**
	 * Action pour envoyer un message privé à un joueur spécifique.
	 * Ouvre une boîte de dialogue permettant de choisir le destinataire et rédiger le message.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void messagePrivate(ActionEvent e) {
		PrivateMessageController controller = dialoguePrivateMessage();

		if (controller == null) {
			printErrorLine("Message Privé echoué", null);
			return;
		}

		leMain.privateMessageSend(controller.getReceiver(), controller.getText());
		AudioManager.jouerSonNotification();
	}


	/**
	 * Action déclenchée pour réfuter un soupçon en cours.
	 * Vérifie si le joueur a l'obligation de réfuter, ouvre la boîte de sélection de carte
	 * et notifie le serveur de la carte utilisée.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void refuterSoupcon(ActionEvent e) {
		if (leMain.getSoupcon() == null) {
			printErrorLine("Pas de soupcon a refuter?", null);
		}
		//else if (!leMain.peutRefuter()) {

		else if (leMain.getRefutage() == null) {
			printErrorLine("Vous n'avez pas a refuter le soupcon ?", null);
		}
		else if (leMain.getRefutage()) {
			printErrorLine("Vous avez deja refuté le soupcon !", null);
		}
		else {
			RefuterController controller = dialogueRefuter();

			if (controller == null) {
				printErrorLine("Refutage echoué, VOUS ETES TOUJOURS DANS L'OBLIGATION DE REFUTER LE SOUPCON !", null);
				return;
			}

			leMain.refuterSoupcon(controller.getCarte());
			AudioManager.jouerSonRefuter();
		}

	}

	/**
	 * Ouvre le formulaire permettant d'émettre un soupçon (personnage et arme).
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void soupconner(ActionEvent e) {
		SoupconController controller = dialogueSoupcon();

		if (controller == null) {
			printErrorLine("Soupcon echoué", null);
			return;
		}

		leMain.soupconnerSend(controller.getPersonnage(), controller.getArme());
	}

	/**
	 * Ouvre le formulaire permettant de porter une accusation finale.
	 * * @param e L'événement d'action.
	 */
	@FXML
	public void accuser(ActionEvent e) {
		AccuseController controller = dialogueAccuse();

		if (controller == null) {
			printErrorLine("Accusation echoué", null);
			return;
		}

		leMain.accuserSend(controller.getPersonnage() , controller.getArme() , controller.getLieu());
	}

	/**
	 * Ouvre la fenêtre liée à la base de données (Carnet de notes du détective).
	 * * @param e L'événement d'action.
	 * @throws IOException En cas d'erreur de chargement de la vue FXML.
	 */
	@FXML
	public void ouvrirBDD(ActionEvent e) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.BDD_FXML));
		Stage stage = new Stage();
		stage.setScene(new Scene(loader.load()));

		// On donne au controller BDD le nom du joueur local
		ControllerBDD controller = loader.getController();
		controller.initData(leMain.getNom());

		stage.initModality(Modality.APPLICATION_MODAL);

		Node source = (Node) e.getSource();
		Stage parentStage = (Stage) source.getScene().getWindow();
		stage.initOwner(parentStage);

		stage.showAndWait();

	}

	/**
	 * Ouvre la fenêtre des paramètres du jeu (activation/désactivation de modules de triche/debug).
	 * * @param actionEvent L'événement d'action.
	 */
	@FXML
	public void ouvrirParametre(ActionEvent actionEvent) {
		dialogueParameteres();
	}

	///--------------------- FONCTIONS SUPPORTEURS DU FXML ------------------------------------------------
	
	
	/**
	 * Initialise la grille du plateau de jeu (GridPane).
	 * <p>
	 * Configure les dimensions minimales et maximales du conteneur selon les constantes 
	 * définies, génère dynamiquement 24 colonnes et 25 lignes avec des contraintes de taille fixes 
	 * non extensibles, et définit le fond en transparent. Attache également un écouteur de clics 
	 * sur la grille pour calculer les coordonnées de la case cliquée et envoyer une requête de 
	 * déplacement au serveur principal.
	 * </p>
	 */
	private void initGrid() {
    	lespions = new GridPane();
		lespions.setSnapToPixel(false);
		
		lespions.setMaxSize(LTAILLE_PLATEAU, HTAILLE_PLATEAU);
	    lespions.setMinSize(LTAILLE_PLATEAU, HTAILLE_PLATEAU);
	    
	    for (int i = 0; i < 24; i++) {
	    	ColumnConstraints colConst = new ColumnConstraints(LTAILLE_CASE_PLATEAU);
	        colConst.setHgrow(javafx.scene.layout.Priority.NEVER);
	        
	        RowConstraints rowConst = new RowConstraints(HTAILLE_CASE_PLATEAU); 
	        rowConst.setVgrow(javafx.scene.layout.Priority.NEVER);
	        
	        lespions.getColumnConstraints().add(colConst); 
	        lespions.getRowConstraints().add(rowConst);
	    }
	    RowConstraints rowConst = new RowConstraints(HTAILLE_CASE_PLATEAU); 
        rowConst.setVgrow(javafx.scene.layout.Priority.NEVER);
        lespions.getRowConstraints().add(rowConst);
	    

        lespions.setStyle("-fx-background-color: transparent;");
        
 	   lespions.setOnMouseClicked(event -> {
	        int x = (int) (event.getX() / LTAILLE_CASE_PLATEAU);
	        int y = (int) (event.getY() / HTAILLE_CASE_PLATEAU);
	        leMain.deplacerSend(y,x);

	    });
    }
	
	
	
	/**
	 * Affiche une boîte de dialogue pour sélectionner un personnage et une arme afin de formuler un soupçon.
	 * * @return Le contrôleur du dialogue validé, ou null en cas d'annulation.
	 */
	private SoupconController dialogueSoupcon() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.SOUPCON_FXML));
			DialogPane loginPane = loader.load();

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setDialogPane(loginPane);
			dialog.setTitle("Effectuer un Soupcon");

			ButtonType customOk = new ButtonType("Soupçonner !", ButtonData.OK_DONE);
			ButtonType customCancel = new ButtonType("Annuler", ButtonData.CANCEL_CLOSE);

			loginPane.getButtonTypes().setAll(customOk, customCancel);


			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent() && result.get() == customOk)
				return loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
			printErrorLine("Soupcon echoué", null);
		}
		return null;
	}

	/**
	 * Affiche une boîte de dialogue pour réaliser une accusation complète (Personnage, Arme, Lieu).
	 * * @return Le contrôleur du dialogue validé, ou null en cas d'annulation.
	 */
	private AccuseController dialogueAccuse() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.ACCUSE_FXML));
			DialogPane loginPane = loader.load();

			// Create the Dialog
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setDialogPane(loginPane);
			dialog.setTitle("Faire une Accusation");

			ButtonType customOk = new ButtonType("Accuser !", ButtonData.OK_DONE);
			ButtonType customCancel = new ButtonType("Annuler", ButtonData.CANCEL_CLOSE);

			loginPane.getButtonTypes().setAll(customOk, customCancel);

			// Show and wait for user action
			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent() && result.get() == customOk)
				return loader.getController();
		} catch (IOException e) {
			printErrorLine("Accusation echoué", null);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Affiche une boîte de dialogue pour la saisie d'un message textuel public.
	 * * @return Le contrôleur du dialogue contenant le texte validé, ou null en cas d'annulation.
	 */
	private PublicMessageController dialoguePublicMessage() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.PUBLIC_M_FXML));
			DialogPane loginPane = loader.load();

			// Create the Dialog
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setDialogPane(loginPane);
			dialog.setTitle("Envoyer un Message Publique");

			ButtonType customOk = new ButtonType("Envoyer Message!", ButtonData.OK_DONE);
			ButtonType customCancel = new ButtonType("Annuler", ButtonData.CANCEL_CLOSE);

			loginPane.getButtonTypes().setAll(customOk, customCancel);

			// Show and wait for user action
			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent() && result.get() == customOk)
				return loader.getController();
		} catch (IOException e) {
			printErrorLine("Message Public echoué", null);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Affiche une boîte de dialogue pour envoyer un message à un joueur en particulier.
	 * * @return Le contrôleur du dialogue contenant le destinataire et le texte, ou null en cas d'annulation.
	 */
	private PrivateMessageController dialoguePrivateMessage() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.PRIVATE_M_FXML));
			DialogPane loginPane = loader.load();

			PrivateMessageController controller = loader.getController();
			controller.initChoices(leMain.getListeJoueurs(), leMain.getNom());

			// Create the Dialog
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setDialogPane(loginPane);
			dialog.setTitle("Envoyer un Message Privé");

			ButtonType customOk = new ButtonType("Envoyer Message!", ButtonData.OK_DONE);
			ButtonType customCancel = new ButtonType("Annuler", ButtonData.CANCEL_CLOSE);

			loginPane.getButtonTypes().setAll(customOk, customCancel);

			// Show and wait for user action
			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent() && result.get() == customOk)
				return controller;
		} catch (IOException e) {
			printErrorLine("Private Message echoué", null);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Affiche l'interface permettant de choisir l'une de ses cartes pour contrer un soupçon adverse.
	 * Construit les choix disponibles selon les cartes de la main correspondant au soupçon émis.
	 * * @return Le contrôleur du dialogue avec la carte sélectionnée, ou null si échoué.
	 */
	private RefuterController dialogueRefuter() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.REFUTER_FXML));
			DialogPane refuteRoot = loader.load();
			RefuterController controller = loader.getController();

			controller.initRefuter (
					leMain.getSoupcon(),
					leMain.hasPersonnageSoupconnée(),
					leMain.hasArmeSoupconnée(),
					leMain.hasLieuSoupconnée()
			);

			Dialog<ButtonType> dialog = new Dialog<>();

			dialog.setDialogPane(refuteRoot);
			dialog.setTitle("Réfuter un Soupçon");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(root.getScene().getWindow());

			ButtonType customOk = new ButtonType("Réfuter", ButtonData.OK_DONE);
			ButtonType customCancel = new ButtonType("Annuler", ButtonData.CANCEL_CLOSE);
			refuteRoot.getButtonTypes().setAll(customOk, customCancel);


			Button okBtn = (Button) refuteRoot.lookupButton(customOk);
			if (okBtn != null) {
				okBtn.setId("btn-ok-cache");
				okBtn.setVisible(false);
				okBtn.setManaged(false);
			}

			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent() && result.get() == customOk) {
				return controller;
			}

		} catch (IOException e) {
			e.printStackTrace();
			printErrorLine("Refutage echoué", null);
		}

		return null;
	}
	
	/**
	 * Affiche la boîte de dialogue des paramètres du jeu.
	 * <p>
	 * Charge dynamiquement le fichier FXML correspondant à l'interface des paramètres, 
	 * l'intègre dans un composant de type {@link DialogPane} et l'associe à la fenêtre 
	 * principale pour éviter les clignotements visuels. Transmet ensuite la référence 
	 * de l'interface principale ({@code leMain}) à son contrôleur dédié afin de lui permettre 
	 * d'activer ou de désactiver les modules de triche/debug avant de bloquer l'affichage 
	 * en attendant l'action de l'utilisateur.
	 * </p>
	 * * @see InterfaceMain#PARAMETRE_FXML
	 * @see ihm.main_controllers.ControllerParametres
	 */
	private void dialogueParameteres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(InterfaceMain.PARAMETRE_FXML));

            // On charge le FXML en tant que DialogPane
            DialogPane parametrePane = loader.load();

            // On crée un Dialog, exactement comme pour tes autres fenêtres
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(parametrePane);
            dialog.setTitle("Paramètres");

            // On récupère le contrôleur pour lui passer l'instance de l'interface principale
            ihm.main_controllers.ControllerParametres controller = loader.getController();
            controller.setleMain(leMain);

            // On lie le dialogue à la fenêtre principale
            dialog.initOwner(root.getScene().getWindow());

            // Affiche la fenêtre sans clignotement !
            dialog.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
            printErrorLine("Impossible d'ouvrir les paramètres", null);
        }
	}

	////--------------------------------------- HORS FXML ------------------------------------------

	/** Largeur standardisée d'une case sur le plateau. */
	public final static double LTAILLE_CASE_PLATEAU = 27.45;
	/** Hauteur standardisée d'une case sur le plateau. */
	public final static double HTAILLE_CASE_PLATEAU = 26.4;

	/** Nombre de colonnes de la grille du plateau. */
	public final static int NB_COL_PLATEAU = 24;
	/** Nombre de lignes de la grille du plateau. */
	public final static int NB_LIG_PLATEAU = 25;

	/** Largeur totale calculée du plateau de jeu. */
	public final static double LTAILLE_PLATEAU = NB_COL_PLATEAU * LTAILLE_CASE_PLATEAU;
	/** Hauteur totale calculée du plateau de jeu. */
	public final static double HTAILLE_PLATEAU = NB_LIG_PLATEAU * HTAILLE_CASE_PLATEAU;

	/** Référence à l'instance principale de l'application cliente. */
	private InterfaceMain leMain;

	/** Map liant un EPersonnage à son instance graphique (ImageView) sur le plateau. */
	Map<EPersonnage, ImageView> pionMap = new EnumMap<>(EPersonnage.class);

	/**
	 * Initialise les vues des pions pour chaque personnage en chargeant les images correspondantes,
	 * puis prépare leur redimensionnement et leur alignement.
	 */
	private void initPionMap() {
		for (EPersonnage p : EPersonnage.values()) {
			ImageView iv = new ImageView(InterfaceMain.PION_MAP.get(p));

			iv.setFitWidth(LTAILLE_CASE_PLATEAU);
			iv.setFitHeight(HTAILLE_CASE_PLATEAU);
			iv.setScaleX(1.35);
			iv.setScaleY(1.35);
			iv.setPreserveRatio(true);
			iv.setSmooth(true);

			GridPane.setHalignment(iv, javafx.geometry.HPos.CENTER);
			GridPane.setValignment(iv, javafx.geometry.VPos.CENTER);



			pionMap.put(p, iv);
		}
	}

	/**
	 * Injecte la référence vers la classe principale de l'application.
	 * * @param main L'instance de {@link InterfaceMain}.
	 */
	public void setleMain(InterfaceMain main) {
		leMain = main;
	}

	/**
	 * Imprime une ligne formatée dans la console dédiée aux erreurs.
	 * * @param text Le message d'erreur à afficher.
	 * @param color La couleur du texte (par défaut Color.RED si null).
	 */
	public void printErrorLine(String text, Color color) {
		Platform.runLater(() -> {

			Text t = new Text(text);

			if (color != null) t.setFill(color);
			else t.setFill(Color.RED); // TODO : css conversion

			Text newline = new Text("\n");

			affichagErreur.getChildren().addAll(t, newline);

			capsulErreur.layout();
			capsulErreur.setVvalue(1.0);
		});
	}

	/**
	 * Imprime une ligne d'événement ou de chat dans la console principale du jeu.
	 * Gère l'horodatage des messages si l'option est activée dans la configuration.
	 * * @param text Le texte à afficher.
	 * @param color La couleur de la police (par défaut Color.WHITE si null).
	 */
	public void printConsoleLine(String text, Color color) {
		Platform.runLater(() -> {

			String textAffiche = text;
			if (Config.chat_timestamp) {
				String heure = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
				textAffiche = "[" + heure + "] " + text;
			}

			Text t = new Text(text);
			if (color != null) t.setFill(color);
			else t.setFill(Color.WHITE);

			Text newline = new Text("\n");

			consoleJeu.getChildren().addAll(t, newline);

			capsulJeu.layout(); // Updates the layout to calculate new height
			capsulJeu.setVvalue(1.0);
		});
	}

	/**
	 * Place initialement un pion sur la grille du plateau aux coordonnées spécifiées.
	 * Note: Les coordonnées X et Y sont inversées lors de l'ajout dans la grille (Row/Column).
	 * * @param personnage Le personnage associé au pion.
	 * @param x Coordonnée X de destination.
	 * @param y Coordonnée Y de destination.
	 */
	public void initPion(EPersonnage personnage ,int x, int y) {
		lespions.add(pionMap.get(personnage), y, x);


	}

	/**
	 * Retire le pion de sa position actuelle et le repositionne sur la nouvelle case.
	 * Note: Les coordonnées X et Y sont inversées pour correspondre à l'indexation de la grille.
	 * * @param personnage Le personnage associé au pion.
	 * @param x Nouvelle coordonnée X.
	 * @param y Nouvelle coordonnée Y.
	 */
	public void bougePion(EPersonnage personnage ,int x, int y) {
		lespions.getChildren().remove(pionMap.get(personnage));
		lespions.add(pionMap.get(personnage), y, x);
	}

	/**
	 * Supprime visuellement le pion du personnage du plateau de jeu (ex: en cas d'élimination).
	 * * @param personnage Le personnage à retirer.
	 */
	public void supprimerPion(EPersonnage personnage) {
		lespions.getChildren().remove(pionMap.get(personnage));
	}

	/**
	 * Réinitialise totalement l'interface de jeu (vide le plateau, les cartes et le statut)
	 * suite à une déconnexion massive ou à la fin d'une partie.
	 */
	public void resetJeu() {
		lespions.getChildren().clear();

		Platform.runLater( () -> {
			zoneCartes.getChildren().clear();
		});
		status.setText("----");
		joueurCourant.setText("----");
	}

	/**
	 * Affiche graphiquement les cartes du joueur dans la zone dédiée (HBox).
	 * Génère des ImageView dynamiques et applique un effet de survol cosmétique.
	 * * @param cartesJoueur La liste des cartes (Armes, Personnages, Lieux) détenues par le joueur.
	 */
	public void afficherCartes(List<Carte> cartesJoueur) {
		Platform.runLater(() -> {
			zoneCartes.getChildren().clear(); // On nettoie les anciennes cartes si nécessaire

			for (Carte carte : cartesJoueur) {
				ImageView iv = new ImageView();
				iv.setFitHeight(140); // Hauteur des cartes en pixels (ajustable selon vos préférences)
				iv.setPreserveRatio(true);

				// On récupère l'image depuis les instances statiques d'InterfaceMain
				if (carte instanceof EPersonnage) {
					iv.setImage(InterfaceMain.PERSON_MAP.get(carte));
				} else if (carte instanceof EArme) {
					iv.setImage(InterfaceMain.ARME_MAP.get(carte));
				} else if (carte instanceof ELieu) {
					iv.setImage(InterfaceMain.LIEU_MAP.get(carte));
				}

				// Petit effet cosmétique : la carte se soulève au passage de la souris
				iv.setOnMouseEntered(e -> iv.setTranslateY(-10));
				iv.setOnMouseExited(e -> iv.setTranslateY(0));

				zoneCartes.getChildren().add(iv);
			}
		});
	}

	/**
	 * Met à jour le label indiquant de qui c'est actuellement le tour.
	 * * @param s Le nom du joueur courant.
	 */
	public void setJoueurCourant(String s) {
		Platform.runLater( () -> {
			joueurCourant.setText(s);
		});
	}

	/**
	 * Met à jour le label de statut indiquant l'état local du joueur.
	 * * @param s Le texte de statut à afficher.
	 */
	public void setStatut(String s) {
		Platform.runLater( () -> {
			status.setText(s);
		});
	}

	/**
	 * Récupère le texte actuellement affiché dans le label de statut.
	 * * @return La chaîne de caractères du statut.
	 */
	public String getStatut() {
		return status.getText();
	}
}