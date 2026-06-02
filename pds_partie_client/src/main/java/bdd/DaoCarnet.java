package bdd;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) gérant les interactions avec la base de données
 * pour le carnet de détective (table "indice").
 * <p>
 * Cette classe fournit des méthodes statiques pour insérer, mettre à jour,
 * lire et supprimer les indices (qui possède quelle carte) notés par les joueurs
 * durant une partie de Cluedo.
 * </p>
 */
public class DaoCarnet {

    /** Connexion à la base de données partagée par l'ensemble des requêtes du DAO. */
    private static Connection connection = null;

    /**
     * Initialise la connexion JDBC utilisée par ce DAO.
     * * @param conn L'objet Connection actif vers la base de données.
     */
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    /**
     * Enregistre ou met à jour un indice dans le carnet du détective.
     * Permet à un joueur de noter si un autre joueur possède ou non une carte spécifique.
     * * @param pseudoSoupconneur Le pseudonyme du joueur qui note l'indice.
     * @param pseudoProprio     Le pseudonyme du joueur ciblé par l'indice (le propriétaire supposé).
     * @param nomCarte          Le nom de la carte concernée.
     * @param possede           L'état de possession (ex: 1 pour possède, 0 pour ne possède pas, 2 pour incertain).
     */
    public static void noterIndice(String pseudoSoupconneur, String pseudoProprio, String nomCarte, int possede) {
        if (connection == null) return;
        String sql = "INSERT INTO indice (id_carte, proprio, soupconneur, possede) " +
                "VALUES ((SELECT id_carte FROM carte WHERE nom_carte = ?), " +
                "(SELECT id FROM joueur WHERE pseudo = ?), " +
                "(SELECT id FROM joueur WHERE pseudo = ?), ?) " +
                "ON DUPLICATE KEY UPDATE possede = ?";

        try {
            PreparedStatement req = connection.prepareStatement(sql);
            req.setString(1, nomCarte);
            req.setString(2, pseudoProprio);
            req.setString(3, pseudoSoupconneur);
            req.setInt(4, possede);
            req.setInt(5, possede);

            req.executeUpdate();
            req.close();
        } catch (SQLException e) {
            System.err.println("Problème lors de l'insertion de l'indice : " + e.getMessage());
        }
    }

    /**
     * Récupère l'état de possession d'une carte par un joueur, tel qu'il a été noté
     * par un autre joueur dans son carnet.
     * * @param pseudoSoupconneur Le pseudonyme du joueur qui consulte son carnet.
     * @param pseudoProprio     Le pseudonyme du joueur dont on veut vérifier la possession.
     * @param nomCarte          Le nom de la carte ciblée.
     * @return L'entier représentant l'état de possession (ex: 0, 1 ou 2), ou null si aucun indice n'est trouvé ou en cas d'erreur.
     */
    public static Integer etatPossessionCarte(String pseudoSoupconneur, String pseudoProprio, String nomCarte) {
        if (connection == null) return null;
        String sql = "SELECT i.possede FROM indice i " +
                "JOIN joueur jp ON i.proprio = jp.id " +
                "JOIN joueur js ON i.soupconneur = js.id " +
                "JOIN carte c ON i.id_carte = c.id_carte " +
                "WHERE js.pseudo = ? AND jp.pseudo = ? AND c.nom_carte = ?";

        Integer possede = null;
        try {
            PreparedStatement req = connection.prepareStatement(sql);
            req.setString(1, pseudoSoupconneur);
            req.setString(2, pseudoProprio);
            req.setString(3, nomCarte);

            ResultSet res = req.executeQuery();
            if (res.next()) {
                possede = res.getInt("possede");
            }
            res.close();
            req.close();
        } catch (SQLException e) {
            System.err.println("Problème lors de la vérification de l'indice : " + e.getMessage());
        }
        return possede;
    }

    /**
     * Récupère la liste de tous les pseudonymes des joueurs enregistrés dans la base de données.
     * * @return Une liste de chaînes de caractères contenant les pseudonymes des joueurs.
     */
    public static List<String> getTousLesJoueurs() {
        List<String> joueurs = new ArrayList<>();
        if (connection == null) return joueurs;
        String sql = "SELECT pseudo FROM joueur";

        try {
            PreparedStatement req = connection.prepareStatement(sql);
            ResultSet res = req.executeQuery();

            while (res.next()) {
                joueurs.add(res.getString("pseudo"));
            }

            res.close();
            req.close();
        } catch (SQLException e) {
            System.err.println("Problème lors de la récupération des joueurs : " + e.getMessage());
        }
        return joueurs;
    }

    /**
     * Récupère la liste de tous les noms de cartes existantes dans la base de données.
     * * @return Une liste de chaînes de caractères contenant les noms des cartes.
     */
    public static List<String> getToutesLesCartes() {
        List<String> cartes = new ArrayList<>();
        if (connection == null) return cartes;
        String sql = "SELECT nom_carte FROM carte";

        try {
            PreparedStatement req = connection.prepareStatement(sql);
            ResultSet res = req.executeQuery();

            while (res.next()) {
                cartes.add(res.getString("nom_carte"));
            }

            res.close();
            req.close();
        } catch (SQLException e) {
            System.err.println("Problème lors de la récupération des cartes : " + e.getMessage());
        }
        return cartes;
    }

    /**
     * Efface l'intégralité des indices stockés dans la base de données.
     * Cette méthode est généralement appelée lors de l'initialisation d'une nouvelle partie
     * pour remettre les carnets à zéro.
     */
    public static void viderCarnet() {
        if (connection == null) return;
        String sql = "DELETE FROM indice";

        try {
            PreparedStatement req = connection.prepareStatement(sql);
            req.executeUpdate();
            req.close();
            System.out.println("Les indices ont été vidés pour une nouvelle partie.");
        } catch (SQLException e) {
            System.err.println("Problème lors du nettoyage des indices : " + e.getMessage());
        }
    }
}