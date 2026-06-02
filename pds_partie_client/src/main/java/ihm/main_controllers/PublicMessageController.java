package ihm.main_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Contrôleur JavaFX gérant la fenêtre de dialogue pour l'envoi de messages publics.
 * <p>
 * Cette interface simple permet à un joueur de rédiger un message destiné
 * à être diffusé à l'ensemble des joueurs présents dans la partie via le chat global.
 * </p>
 */
public class PublicMessageController {

	/** Champ de saisie de texte où le joueur rédige son message public. */
	@FXML
	private TextField messageField;

	/**
	 * Récupère le texte du message actuellement saisi par l'utilisateur.
	 *
	 * @return Le contenu du message sous forme de chaîne de caractères.
	 */
	public String getText() {
		return messageField.getText();
	}

}