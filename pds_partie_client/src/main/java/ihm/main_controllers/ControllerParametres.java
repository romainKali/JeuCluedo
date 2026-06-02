package ihm.main_controllers;

import ihm.AudioManager;
import ihm.Config;
import ihm.InterfaceMain;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * Contrôleur JavaFX gérant la fenêtre des paramètres de l'application.
 * <p>
 * Cette classe permet à l'utilisateur de configurer en temps réel les options liées au son
 * (volume général, effets sonores, mode muet), à l'interface (thème sombre, défilement du chat, horodatage)
 * et aux permissions du jeu (autorisation des messages, lancers de dés, mouvements, etc.).
 * </p>
 */
public class ControllerParametres {

    /** Curseur permettant d'ajuster le volume de la musique d'ambiance. */
    @FXML private Slider sliderVolume;
    /** Étiquette affichant le pourcentage actuel du volume de la musique d'ambiance. */
    @FXML private Label labelVolumeValue;
    /** Curseur permettant d'ajuster le volume des effets sonores (SFX). */
    @FXML private Slider sliderSfx;
    /** Étiquette affichant le pourcentage actuel du volume des effets sonores. */
    @FXML private Label labelSfxValue;
    /** Case à cocher pour couper totalement le son (mode muet). */
    @FXML private CheckBox cbMuet;

    /** Case à cocher pour activer ou désactiver le mode sombre de l'interface. */
    @FXML private CheckBox cbDarkMode;
    /** Case à cocher pour activer ou désactiver le défilement automatique du chat vers le bas. */
    @FXML private CheckBox cbAutoScroll;
    /** Case à cocher pour afficher ou masquer l'horodatage des messages dans la console. */
    @FXML private CheckBox cbTimestamp;

    /** Case à cocher pour exiger ou non une confirmation avant de terminer un tour. */
    @FXML private CheckBox cbConfirmFinTour;
    /** Case à cocher pour autoriser ou interdire l'envoi de messages publics. */
    @FXML private CheckBox cbMessagePublic;
    /** Case à cocher pour autoriser ou interdire l'envoi de messages privés. */
    @FXML private CheckBox cbMessagePrive;
    /** Case à cocher pour autoriser ou interdire le lancer de dés. */
    @FXML private CheckBox cbDes;
    /** Case à cocher pour autoriser ou interdire les mouvements sur le plateau de jeu. */
    @FXML private CheckBox cbMouvement;

    /** Bouton permettant de réinitialiser l'ensemble des paramètres à leurs valeurs par défaut. */
    @FXML private Button btnResetDefault;

    /** Référence vers l'instance principale de l'application. */
    private InterfaceMain leMain;

    /**
     * Méthode appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * <p>
     * Elle configure les écouteurs d'événements (listeners) sur les curseurs et les cases à cocher
     * afin d'appliquer dynamiquement les modifications aux configurations globales (via {@link Config})
     * ou au gestionnaire audio (via {@link AudioManager}). Elle définit également l'action du bouton de réinitialisation.
     * </p>
     */
    @FXML
    public void initialize() {
        // --- SON ---
        sliderVolume.valueProperty().addListener((obs, old, val) -> {
            labelVolumeValue.setText(val.intValue() + "%");
            if (leMain != null && AudioManager.getAmbiancePlayer() != null) {
                AudioManager.getAmbiancePlayer().setVolume(val.doubleValue() / 100.0);
            }
        });

        sliderSfx.valueProperty().addListener((obs, old, val) -> {
            Config.volume_sfx = val.doubleValue();
            labelSfxValue.setText(val.intValue() + "%");
        });

        cbMuet.selectedProperty().addListener((obs, old, val) -> {
            if (leMain != null && AudioManager.getAmbiancePlayer() != null) AudioManager.getAmbiancePlayer().setMute(val);
        });
        cbAutoScroll.selectedProperty().addListener((obs, old, val) -> Config.chat_autoscroll = val);
        cbTimestamp.selectedProperty().addListener((obs, old, val) -> Config.chat_timestamp = val);

        // --- JEU ---
        cbConfirmFinTour.selectedProperty().addListener((obs, old, val) -> Config.confirm_fin_tour = val);
        cbMessagePublic.selectedProperty().addListener((obs, old, val) -> Config.message_public = val);
        cbMessagePrive.selectedProperty().addListener((obs, old, val) -> Config.message_prive = val);
        cbDes.selectedProperty().addListener((obs, old, val) -> Config.des = val);
        cbMouvement.selectedProperty().addListener((obs, old, val) -> Config.mouvement = val);

        // --- BOUTON RESET ---
        btnResetDefault.setOnAction(event -> {
            sliderVolume.setValue(70);
            sliderSfx.setValue(70);
            cbMuet.setSelected(false);
            cbDarkMode.setSelected(false);
            cbAutoScroll.setSelected(true);
            cbTimestamp.setSelected(true);
            cbConfirmFinTour.setSelected(true);
        });
    }

    /**
     * Injecte la référence vers la classe principale de l'application et met à jour
     * l'état visuel de l'interface en fonction des paramètres de configuration actuels.
     *
     * @param leMain L'instance de {@link InterfaceMain}.
     */
    public void setleMain(InterfaceMain leMain) {
        this.leMain = leMain;
        if (leMain != null && AudioManager.getAmbiancePlayer() != null) {
            sliderVolume.setValue(AudioManager.getAmbiancePlayer().getVolume() * 100.0);
            cbMuet.setSelected(AudioManager.getAmbiancePlayer().isMute());
        }
        sliderSfx.setValue(Config.volume_sfx);
        cbAutoScroll.setSelected(Config.chat_autoscroll);
        cbTimestamp.setSelected(Config.chat_timestamp);
        cbConfirmFinTour.setSelected(Config.confirm_fin_tour);
        cbMessagePublic.setSelected(Config.message_public);
        cbMessagePrive.setSelected(Config.message_prive);
        cbDes.setSelected(Config.des);
        cbMouvement.setSelected(Config.mouvement);
    }
}