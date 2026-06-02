package ihm;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Gestionnaire utilitaire pour la lecture des fichiers audio de l'application.
 * <p>
 * Cette classe centralise le lancement et l'arrêt de la musique d'ambiance (via {@link MediaPlayer})
 * ainsi que la lecture des différents effets sonores ponctuels (via {@link AudioClip}), en prenant
 * en compte les paramètres de volume configurés par l'utilisateur.
 * </p>
 */
public class AudioManager {

    /** Lecteur multimédia dédié exclusivement à la musique de fond (ambiance). */
    private static MediaPlayer musiqueAmbiancePlayer;

    /**
     * Démarre ou redémarre la musique d'ambiance en boucle infinie.
     * Si une musique est déjà en cours de lecture, elle est d'abord arrêtée
     * pour éviter la superposition des pistes.
     */
    public static void jouerMusiqueAmbiance() {
        // Prevent creating multiple overlapping background tracks
        if (musiqueAmbiancePlayer != null) {
            musiqueAmbiancePlayer.stop();
        }

        // Grab the Media from your Main class (as we set up in question 1)
        Media media = InterfaceMain.MEDIA_AMBIANCE;

        if (media != null) {
            musiqueAmbiancePlayer = new MediaPlayer(media);
            musiqueAmbiancePlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musiqueAmbiancePlayer.play();
        } else {
            System.err.println("Impossible de jouer la musique : Media non initialisé.");
        }
    }

    /**
     * Arrête la lecture de la musique d'ambiance si celle-ci est en cours.
     */
    public static void arreterMusiqueAmbiance() {
        if (musiqueAmbiancePlayer != null) {
            musiqueAmbiancePlayer.stop();
        }
    }

    /**
     * Récupère l'instance actuelle du lecteur de musique d'ambiance.
     * Utile pour ajuster dynamiquement le volume depuis les paramètres.
     *
     * @return Le {@link MediaPlayer} gérant la musique de fond.
     */
    public static MediaPlayer getAmbiancePlayer() {
        return musiqueAmbiancePlayer;
    }

    /**
     * Joue l'effet sonore correspondant à un lancer de dés.
     */
    public static void joueurSonDes() {
        playSon(InterfaceMain.SFX_LANCER_DES);
    }

    /**
     * Joue l'effet sonore d'une notification (ex: fin de tour ou réception d'un message).
     */
    public static void jouerSonNotification() {
        playSon(InterfaceMain.SFX_NOTIFICATION);
    }

    /**
     * Joue l'effet sonore lié à la réfutation d'un soupçon (ex: glissement d'une carte/papier).
     */
    public static void jouerSonRefuter() {
        playSon(InterfaceMain.SFX_REFUTER);
    }

    /**
     * Joue l'effet sonore d'une ouverture de porte (ex: démarrage de la partie).
     */
    public static void jouerSonPorte() {
        playSon(InterfaceMain.SFX_PORTE);
    }

    /**
     * Méthode utilitaire privée chargée de jouer un effet sonore (AudioClip) spécifique.
     * Elle applique automatiquement le volume configuré par l'utilisateur dans la classe {@link Config}.
     *
     * @param sfx L'objet {@link AudioClip} à lire.
     */
    private static void playSon(AudioClip sfx) {
        if (sfx == null)
            System.err.println("Peut pas jouer le son car il est null");
        else
            sfx.play(Config.volume_sfx / 100.0);
    }
}