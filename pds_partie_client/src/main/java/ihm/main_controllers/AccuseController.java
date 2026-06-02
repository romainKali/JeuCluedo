package ihm.main_controllers;

import enums.EArme;
import enums.ELieu;
import enums.EPersonnage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

/**
 * Contrôleur JavaFX gérant la fenêtre de dialogue pour formuler une accusation finale.
 * <p>
 * Cette interface permet au joueur de sélectionner une combinaison de trois éléments
 * (un personnage, une arme et un lieu) à partir de listes déroulantes afin d'émettre
 * son accusation pour tenter de remporter la partie.
 * </p>
 */
public class AccuseController {

    /** Liste déroulante permettant de sélectionner le personnage accusé. */
    @FXML
    private ChoiceBox<EPersonnage> personnageChoiceBox;

    /** Liste déroulante permettant de sélectionner l'arme accusée. */
    @FXML
    private ChoiceBox<EArme> armeChoiceBox;

    /** Liste déroulante permettant de sélectionner le lieu accusé. */
    @FXML
    private ChoiceBox<ELieu> lieuChoiceBox;

    /**
     * Méthode appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * Elle peuple les listes déroulantes avec l'ensemble des valeurs possibles pour
     * les personnages, les armes et les lieux, et sélectionne le premier élément par défaut.
     */
    @FXML
    public void initialize() {
        personnageChoiceBox.setItems(FXCollections.observableArrayList(EPersonnage.values()));
        armeChoiceBox.setItems(FXCollections.observableArrayList(EArme.values()));
        lieuChoiceBox.setItems(FXCollections.observableArrayList(ELieu.values()));

        personnageChoiceBox.getSelectionModel().selectFirst();
        armeChoiceBox.getSelectionModel().selectFirst();
        lieuChoiceBox.getSelectionModel().selectFirst();
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

    /**
     * Récupère le lieu actuellement sélectionné dans la liste déroulante.
     *
     * @return L'objet {@link ELieu} sélectionné.
     */
    public ELieu getLieu() {
        return lieuChoiceBox.getValue();
    }
}