package ihm.main_controllers;

import enums.Carte;
import enums.EArme;
import enums.ELieu;
import enums.EPersonnage;
import enums.Proposition;
import ihm.InterfaceMain;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * Contrôleur JavaFX gérant la fenêtre de dialogue pour réfuter un soupçon.
 * <p>
 * Cette interface permet au joueur confronté à un soupçon adverse de choisir
 * l'une des cartes correspondantes (Personnage, Arme ou Lieu) qu'il possède en main
 * afin d'invalider l'hypothèse. Si le joueur ne possède aucune des cartes,
 * il doit utiliser l'option de passage (bouton null).
 * </p>
 */
public class RefuterController {

	/** La carte finalement choisie par le joueur pour réfuter le soupçon (peut être null). */
	private Carte res = null;

	/** Bouton affiché et activé uniquement si le joueur ne possède aucune des cartes du soupçon. */
	@FXML
	private Button nullButton;

	/** Boutons représentant respectivement le personnage, l'arme et le lieu suspectés. */
	@FXML
	private Button personnageButton, armeButton, lieuButton;

	/** Le personnage ciblé par le soupçon en cours. */
	private EPersonnage personnage;
	/** L'arme ciblée par le soupçon en cours. */
	private EArme arme;
	/** Le lieu ciblé par le soupçon en cours. */
	private ELieu lieu;

	/**
	 * Initialise la fenêtre de réfutation avec les détails du soupçon et l'état de la main du joueur.
	 * Configure les images des cartes sur les boutons et grise (désactive) les boutons
	 * correspondant aux cartes que le joueur ne possède pas.
	 *
	 * @param soupcon           L'objet Proposition contenant le Personnage, l'Arme et le Lieu suspectés.
	 * @param possedepersonnage Vrai si le joueur possède la carte Personnage du soupçon.
	 * @param possedearme       Vrai si le joueur possède la carte Arme du soupçon.
	 * @param possedelieu       Vrai si le joueur possède la carte Lieu du soupçon.
	 */
	public void initRefuter(Proposition soupcon,
	                        boolean possedepersonnage, boolean possedearme, boolean possedelieu
	) {
		personnage = soupcon.getPersonnage();
		arme = soupcon.getArme();
		lieu = soupcon.getLieu();

		ImageView ivPersonnage = new ImageView(InterfaceMain.PERSON_MAP.get(personnage));
		ImageView ivArme = new ImageView(InterfaceMain.ARME_MAP.get(arme));
		ImageView ivLieu = new ImageView(InterfaceMain.LIEU_MAP.get(lieu));

		// Make sure they don't block clicks from hitting the button!
		configureGraphic(ivPersonnage);
		configureGraphic(ivArme);
		configureGraphic(ivLieu);

		personnageButton.setGraphic(ivPersonnage);
		armeButton.setGraphic(ivArme);
		lieuButton.setGraphic(ivLieu);

		if (!possedearme && !possedelieu && !possedepersonnage) {
			nullButton.setDisable(false);
			nullButton.setManaged(true);

			lieuButton.setDisable(true); // TODO : set images
			personnageButton.setDisable(true);
			armeButton.setDisable(true);
		}
		else {
			if (!possedearme) {
				armeButton.setDisable(true);
			}
			if (!possedelieu) {
				lieuButton.setDisable(true);
			}
			if (!possedepersonnage) {
				personnageButton.setDisable(true);
			}
		}

	}


	/**
	 * Configure les propriétés d'affichage d'une image (ImageView) pour l'intégrer dans un bouton.
	 * S'assure notamment que l'image est redimensionnée correctement et qu'elle laisse passer
	 * les événements de clic de souris vers le bouton parent.
	 *
	 * @param iv L'ImageView à configurer.
	 */
	private void configureGraphic(ImageView iv) {
		//iv.setFitWidth(100);
		iv.setFitHeight(150);
		iv.setPreserveRatio(true);
		iv.setSmooth(true);

		iv.setPickOnBounds(true);
		iv.setMouseTransparent(true);

	}


	/**
	 * Gère l'événement de clic sur l'un des boutons de sélection de carte.
	 * Associe la carte cliquée au résultat de la réfutation, puis déclenche
	 * la fermeture de la boîte de dialogue en simulant un clic sur le bouton caché "OK".
	 *
	 * @param event L'événement d'action généré par le clic.
	 */
	@FXML
	private void handleOptionClick(ActionEvent event) {
		// 1. Process your logic here...
		Button sourceObject =  (Button) event.getSource();

		if(sourceObject == lieuButton)
			res = lieu;
		else if (sourceObject == armeButton)
			res = arme;
		else if (sourceObject == personnageButton)
			res = personnage;


		// 2. Close the dialog
		Button okBtn = (Button) sourceObject.getScene().lookup("#btn-ok-cache");
		if (okBtn != null) {
			okBtn.fire();
		}
	}



	/**
	 * Récupère la carte sélectionnée par le joueur pour réfuter le soupçon.
	 *
	 * @return L'objet Carte sélectionné, ou null si aucune carte n'a été sélectionnée
	 * (ou si le joueur a utilisé le bouton null).
	 */
	public Carte getCarte() {
		return res;
	}
}