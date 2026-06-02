package ihm.main_controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

/**
 * Contrôleur JavaFX gérant la fenêtre de dialogue pour l'envoi de messages privés.
 * <p>
 * Cette interface permet à un joueur de rédiger un message et de sélectionner
 * un destinataire spécifique parmi les autres joueurs connectés à la partie.
 * </p>
 */
public class PrivateMessageController {

	/** Champ de saisie de texte où le joueur rédige son message. */
	@FXML
	private TextField messageField;

	/** Liste déroulante permettant de sélectionner le destinataire du message. */
	@FXML
	private ChoiceBox<String> receiverList;

	/**
	 * Récupère le texte du message actuellement saisi par l'utilisateur.
	 *
	 * @return Le contenu du message sous forme de chaîne de caractères.
	 */
	public String getText() {
		return messageField.getText();
	}

	/**
	 * Récupère le pseudonyme du destinataire sélectionné dans la liste déroulante.
	 *
	 * @return Le nom du joueur destinataire.
	 */
	public String getReceiver() {
		return receiverList.getValue();
	}

	/**
	 * Initialise la liste déroulante avec les pseudonymes des joueurs disponibles
	 * pour recevoir un message privé.
	 * <p>
	 * Le joueur local (l'expéditeur) est automatiquement exclu de cette liste
	 * afin d'empêcher l'envoi de messages à soi-même. Le champ de texte est également vidé,
	 * et le premier joueur de la liste est sélectionné par défaut si la liste n'est pas vide.
	 * </p>
	 *
	 * @param players     La liste complète des pseudonymes de tous les joueurs de la partie.
	 * @param localPlayer Le pseudonyme du joueur local utilisant cette interface.
	 */
	public void initChoices(ArrayList<String> players, String localPlayer) {
		receiverList.getItems().clear();
		messageField.clear();

		for (String player : players) {
			if (!player.equals(localPlayer)) {
				receiverList.getItems().add(player);
			}
		}

		if (!receiverList.getItems().isEmpty()) {
			receiverList.getSelectionModel().selectFirst();
		}
	}


}