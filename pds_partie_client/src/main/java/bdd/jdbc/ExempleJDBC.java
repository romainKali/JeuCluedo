package bdd.jdbc;

import java.sql.*;

public class ExempleJDBC {
    // Dans cet exemple, la connection et la requete sont des variables globales
    // qui seront refermées seulement tout à la fin
    // C'est pas forcément comme cela qu'il faut procéder dans une application réelle. Ici
    // c'est juste pour montrer comment ça marche...
    private static Connection connection;
    private static Statement statement;

    public static void main(String[] args) {
        connecter();
        System.out.println("Base connectée");
        System.out.println("Liste des clients dont le nom contient 'CHAN'");
        System.out.println("---------------------------------------------");
        afficherListeClients(rechercherListeClientsParNom("CHAN"));
        int cle = inserer("DELOIN", "Alain");
        System.out.println();

        System.out.println("Liste des clients après l'insertion");
        System.out.println("---------------------------------------------");
        afficherListeClients(rechercherListeClientsParNom(""));
        supprimer(cle);
        System.out.println();

        System.out.println("Liste des clients après la suupression");
        System.out.println("---------------------------------------------");
        afficherListeClients(rechercherListeClientsParNom(""));
        deconnecter();
    }

    public static void connecter() {
        connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/exemplejdbc", "tartempion", "toto");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deconnecter() {
        if (connection != null) {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ResultSet rechercherListeClientsParNom(String nom) {
        final String sql = "SELECT * FROM client WHERE nom_client LIKE '%" + nom + "%'";
        ResultSet resultat = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            resultat = statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return resultat;
    }

    public static void afficherListeClients(ResultSet liste) {
        int indiceClient = 1;
        try {
            while (liste.next()) {
                System.out.println("Client[" + indiceClient + "]");
                System.out.println("Code    : " + liste.getString("code_client"));
                System.out.println("Nom     : " + liste.getString("nom_client"));
                System.out.println("Prénom  : " + liste.getString("prenom_client"));
                System.out.println("Adresse : " + liste.getString("adr_client"));
                System.out.println("Code CB : " + liste.getString("code_cb"));
                indiceClient++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int inserer(String nom, String prenom) {
        final String sql = "INSERT INTO client (nom_client, prenom_client) VALUES ('"+nom+"','"+prenom+"')";
        Statement statement = null;

        try {
            statement = connection.createStatement();
            int n = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            if (n != 1)
                throw new SQLException("L'insertion s'est mal passée car n vaut "+n);

            // Ici, on est sûr qu'un seul enregistrement a été inséré, donc une seule clé à récupérer
            ResultSet listeCles = statement.getGeneratedKeys();
            listeCles.next();
            int cle = listeCles.getInt(1);

            return cle;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void supprimer(int cle) {
        final String sql = "DELETE FROM client WHERE code_client='"+cle+"'";

        try {
            statement = connection.createStatement();
            int n = statement.executeUpdate(sql);
            if (n != 1)
                throw new SQLException("La suppression s'est mal passée car n vaut "+n);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
