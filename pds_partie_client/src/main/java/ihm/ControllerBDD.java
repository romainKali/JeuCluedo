package ihm;

import java.util.List;

import bdd.DaoCarnet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Contrôleur JavaFX gérant l'affichage du carnet de détective (base de données).
 * <p>
 * Cette classe est responsable de la création et du remplissage de la grille (GridPane)
 * représentant l'état des connaissances du joueur local sur les cartes possédées ou non
 * par les différents joueurs de la partie. Elle communique avec la base de données via
 * la classe {@link bdd.DaoCarnet}.
 * </p>
 */
public class ControllerBDD {

    /** Conteneur principal de la vue du carnet de détective. */
    @FXML
    private BorderPane root;

    /** Grille affichant le tableau croisé (Joueurs en colonnes, Cartes en lignes). */
    @FXML
    private GridPane tableau;

    /** Bouton permettant de fermer la fenêtre du carnet. */
    @FXML
    private Button closeButton;

    /**
     * Méthode appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * Elle s'assure que la grille est vierge avant tout remplissage de données.
     */
    @FXML
    private void initialize() {
        tableau.getChildren().clear();
    }

    /**
     * Récupère les données depuis la base de données et construit dynamiquement
     * l'affichage du carnet de détective.
     * <p>
     * Place les noms des joueurs en en-tête (colonnes), les noms des cartes à gauche (lignes),
     * et remplit les intersections avec l'état de possession de la carte
     * (✔ = possède, ✖ = ne possède pas, ? = incertain).
     * </p>
     *
     * @param pseudoLocal Le pseudonyme du joueur local qui consulte son carnet.
     */
    public void initData(String pseudoLocal) {
        List<String> nomsJoueurs = DaoCarnet.getTousLesJoueurs();
        List<String> nomsCartes = DaoCarnet.getToutesLesCartes();

        // Ajout d'une vérification : si la BDD est vide ou hors ligne
        if (nomsJoueurs == null || nomsJoueurs.isEmpty() || nomsCartes == null || nomsCartes.isEmpty()) {
            Label erreur = new Label("Aucune donnée trouvée.\nVérifiez que WAMP/XAMPP est lancé et que la BDD 'psd4s' contient des cartes/joueurs.");
            erreur.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
            tableau.add(erreur, 0, 0);
            return;
        }

        int nbJoueurs = nomsJoueurs.size();
        int nbCartes = nomsCartes.size();

        // Amélioration visuelle de la grille
        tableau.setHgap(15);
        tableau.setVgap(5);
        tableau.setGridLinesVisible(true); // Affiche les lignes du carnet

        // Noms des joueurs en haut
        for (int i = 0; i < nbJoueurs; i++) {
            Label labelJoueur = new Label(nomsJoueurs.get(i));
            labelJoueur.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
            tableau.add(labelJoueur, i + 1, 0);
        }

        // Noms des cartes à gauche
        for (int j = 0; j < nbCartes; j++) {
            Label labelCarte = new Label(nomsCartes.get(j));
            labelCarte.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
            tableau.add(labelCarte, 0, j + 1);
        }

        for (int i = 0; i < nbJoueurs; i++) {
            for (int j = 0; j < nbCartes; j++) {

                Integer possede = DaoCarnet.etatPossessionCarte(pseudoLocal, nomsJoueurs.get(i), nomsCartes.get(j));

                if (possede != null) {
                    if (possede == 1) { // 1 = Possède
                        Label tick = new Label("✔");
                        tick.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 5px;");
                        tableau.add(tick, i + 1, j + 1);
                    } else if (possede == 0) { // 0 = Ne possède pas
                        Label cross = new Label("✖");
                        cross.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 5px;");
                        tableau.add(cross, i + 1, j + 1);
                    } else if (possede == 2) {
                        Label unknown = new Label("?");
                        unknown.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 5px;");
                        tableau.add(unknown, i + 1, j + 1);
                    }
                }
            }
        }
    }

    /**
     * Action associée au bouton de fermeture.
     * Ferme la fenêtre modale contenant le carnet de détective.
     *
     * @param e L'événement d'action (clic sur le bouton).
     */
    @FXML
    private void close(ActionEvent e) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}