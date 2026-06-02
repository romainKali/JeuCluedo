package ihm.main_controllers;

import enums.EArme;
import enums.EPersonnage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

/**
 * Contrôleur JavaFX gérant la fenêtre de dialogue pour formuler un soupçon.
 * <p>
 * Cette interface permet au joueur de sélectionner un personnage et une arme
 * à partir de listes déroulantes afin d'émettre une hypothèse (un soupçon).
 * Contrairement à l'accusation, le lieu n'est pas sélectionné ici car il est
 * implicitement déduit de la position actuelle du pion du joueur sur le plateau.
 * </p>
 */
public class SoupconController {

    /** Liste déroulante permettant de sélectionner le personnage suspecté. */
    @FXML
    private ChoiceBox<EPersonnage> personnageChoiceBox;

    /** Liste déroulante permettant de sélectionner l'arme suspectée. */
    @FXML
    private ChoiceBox<EArme> armeChoiceBox;

    /**
     * Méthode appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * Elle peuple les listes déroulantes avec l'ensemble des valeurs possibles pour
     * les personnages et les armes, et sélectionne le premier élément par défaut.
     */
    @FXML
    public void initialize() {
        personnageChoiceBox.setItems(FXCollections.observableArrayList(EPersonnage.values()));
        armeChoiceBox.setItems(FXCollections.observableArrayList(EArme.values()));

        personnageChoiceBox.getSelectionModel().selectFirst();
        armeChoiceBox.getSelectionModel().selectFirst();
    }

    /**
     * Récupère l'arme actuellement sélectionnée dans la liste déroulante.
     *
     * @return L'objet {@link EArme} sélectionné.
     */
    public EArme getArme() {
        return armeChoiceBox.getValue();
    }

    /**
     * Récupère le personnage actuellement sélectionné dans la liste déroulante.
     *
     * @return L'objet {@link EPersonnage} sélectionné.
     */
    public EPersonnage getPersonnage() {
        return personnageChoiceBox.getValue();
    }
}