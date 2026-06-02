package bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ihm.InterfaceMain;

/**
 * Gestionnaire de la base de données de l'application.
 * <p>
 * Cette classe s'occupe de l'établissement de la connexion JDBC avec la base MySQL locale,
 * et fournit les méthodes nécessaires pour interagir avec les données des joueurs
 * (enregistrement et déconnexion).
 * </p>
 */
public class DatabaseManager {

    /** Objet représentant la connexion active à la base de données. */
    private Connection connection;

    /**
     * Constructeur par défaut.
     * Tente d'établir la connexion à la base de données dès l'instanciation.
     */
    public DatabaseManager() {
        connecter();
    }

    /**
     * Établit la connexion JDBC avec la base de données MySQL "psd4s".
     * Charge le driver MySQL et initialise l'objet de connexion.
     */
    private void connecter() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost/psd4s", InterfaceMain.BDD_USER, InterfaceMain.BDD_PASS);
            System.out.println("[BDD] Base connectée avec succès !");

            DaoCarnet.setConnection(this.connection);

            DaoCarnet.viderCarnet();

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[BDD] Erreur de connexion : " + e.getMessage());
        }
    }

    /**
     * Enregistre un nouveau joueur dans la base de données.
     * Utilise "INSERT IGNORE" pour éviter les doublons si le joueur existe déjà.
     * * @param pseudo Le pseudonyme du joueur à enregistrer.
     */
    public void enregistrerJoueur(String pseudo) {
        if (connection == null) return;

        String sql = "INSERT IGNORE INTO joueur (pseudo) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, pseudo);
            int lignesModifiees = pstmt.executeUpdate();

            if (lignesModifiees > 0) {
                System.out.println("[BDD] Nouveau joueur enregistré : " + pseudo);
            } else {
                System.out.println("[BDD] Le joueur " + pseudo + " est déjà dans la base.");
            }
        } catch (SQLException e) {
            System.err.println("[BDD] Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    /**
     * Supprime le joueur de la base de données locale lors de sa déconnexion.
     * * @param pseudoJoueur Le pseudonyme du joueur à supprimer de la BDD.
     */
    public void deconnecter(String pseudoJoueur) {
        if (connection != null) {
            try {
                String sql = "DELETE FROM joueur WHERE pseudo = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, pseudoJoueur);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}