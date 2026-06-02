package ihm;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Classe utilitaire gérant la configuration globale de l'application cliente.
 * <p>
 * Elle contient des paramètres statiques définissant les règles de jeu autorisées,
 * les préférences d'interface utilisateur, l'audio et les options de débogage.
 * Les valeurs par défaut peuvent être écrasées par le chargement d'un fichier de configuration XML.
 * </p>
 */
public class Config {

    // --- Paramètres des règles de jeu ---
    /** Autorise ou interdit le déplacement des pions sur le plateau. */
    public static boolean mouvement = true;
    /** Autorise ou interdit l'émission de soupçons. */
    public static boolean soupcon = true;
    /** Autorise ou interdit de porter une accusation finale. */
    public static boolean accusation = true;
    /** Autorise ou interdit le lancer de dés. */
    public static boolean des = true;

    // --- Paramètres de communication réseau ---
    /** Autorise ou interdit l'envoi de messages publics sur le chat. */
    public static boolean message_public = true;
    /** Autorise ou interdit l'envoi de messages privés entre les joueurs. */
    public static boolean message_prive = true;

    // --- Paramètres de débogage ---
    /** Active ou désactive l'affichage détaillé des trames réseaux dans la console. */
    public static boolean debug_reseau = false;

    // --- Paramètres de l'interface et de l'audio ---
    /** Volume par défaut des effets sonores (SFX) compris entre 0.0 et 100.0. */
    public static double volume_sfx = 70.0;
    /** Affiche ou masque l'horodatage des messages dans le chat du jeu. */
    public static boolean chat_timestamp = true;
    /** Active ou désactive le défilement automatique vers le bas de la console de jeu. */
    public static boolean chat_autoscroll = true;
    /** Demande ou non une confirmation visuelle avant de terminer son tour. */
    public static boolean confirm_fin_tour = true;
    /** Active ou désactive le mode sombre de l'interface (si implémenté). */
    public static boolean dark_mode = false;


    /**
     * Extrait une valeur booléenne depuis un document XML pour une balise donnée.
     * <p>
     * Si la balise est introuvable, vide, ou illisible, la valeur par défaut fournie
     * en paramètre est retournée de manière sécurisée.
     * </p>
     *
     * @param doc          Le document XML pré-analysé.
     * @param tag          Le nom de la balise XML à rechercher (ex: "mouvement").
     * @param defaultValue La valeur de secours à retourner en cas d'échec de lecture.
     * @return true ou false selon le contenu de la balise XML ou la valeur par défaut.
     */
    private static boolean getBoolean(
            Document doc,
            String tag,
            boolean defaultValue
    ) {

        try {

            var node =
                    doc.getElementsByTagName(tag)
                            .item(0);

            if (node == null)
                return defaultValue;

            String text =
                    node.getTextContent();

            if (text == null || text.isBlank())
                return defaultValue;

            return Boolean.parseBoolean(text);

        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Charge et applique la configuration depuis le fichier XML défini dans {@link InterfaceMain#CONFIG_XML}.
     * Les valeurs trouvées dans le fichier écrasent les valeurs par défaut définies statiquement.
     * En cas d'erreur de lecture du fichier XML, les paramètres conservent leurs valeurs par défaut
     * et l'erreur est affichée dans la console.
     */
    public static void load() {

        try {

            Document doc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(Config.class.getResourceAsStream(
                                    InterfaceMain.CONFIG_XML
                            )
                    );

            doc.getDocumentElement().normalize();

            mouvement = getBoolean(
                    doc,
                    "mouvement",
                    mouvement
            );

            soupcon = getBoolean(
                    doc,
                    "soupcon",
                    soupcon
            );

            accusation = getBoolean(
                    doc,
                    "accusation",
                    accusation
            ) ;

            des = getBoolean(
                    doc,
                    "des",
                    des
            );

            message_public = getBoolean(
                    doc,
                    "message_public",
                    message_public
            );

            message_prive = getBoolean(
                    doc,
                    "message_prive",
                    message_prive
            );

            debug_reseau = getBoolean(
                    doc,
                    "debug_reseau",
                    debug_reseau
            );

            System.out.println("Configuration chargée.");

        } catch (Exception e) {
            System.out.println(
                    "Erreur chargement config.xml"
            );

            e.printStackTrace();
        }
    }
}