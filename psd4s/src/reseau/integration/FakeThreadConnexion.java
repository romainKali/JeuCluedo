package reseau.integration;

import reseau.ConnexionUtilisateur;
import reseau.ThreadConnexion;

import java.util.ArrayList;

/**
 * Implémentation simulée de {@link ThreadConnexion} utilisée lors
 * des tests d'intégration.
 *
 * Cette classe remplace les communications réseau réelles par un
 * mécanisme de stockage en mémoire des messages envoyés par le serveur.
 *
 * Elle permet de vérifier le comportement du serveur sans ouvrir
 * de socket ni établir de connexion réseau réelle.
 *
 * Les messages envoyés sont conservés dans une liste afin que les
 * tests puissent vérifier leur contenu et leur ordre d'émission.
 */
public class FakeThreadConnexion extends ThreadConnexion {
    /**
     * Historique des messages envoyés par le serveur durant le test.
     */
    public ArrayList<String> messagesEnvoyesParLeServeur = new ArrayList<>();

    /**
     * Crée une fausse connexion réseau associée à un utilisateur.
     *
     * @param connexion connexion utilisateur simulée
     */
    public FakeThreadConnexion(ConnexionUtilisateur connexion) {
        super(connexion);
    }

    /**
     * Simule l'envoi d'un message au client.
     *
     * Au lieu d'écrire dans une socket, le message est simplement
     * ajouté à l'historique des messages envoyés afin d'être
     * exploité par les tests.
     *
     * @param message message envoyé par le serveur
     */
    @Override
    public void envoyerMessage(String message) {
        messagesEnvoyesParLeServeur.add(message);
    }

    /**
     * Retourne l'ensemble des messages envoyés par le serveur
     * depuis le début du test.
     *
     * @return liste des messages enregistrés
     */
    public ArrayList<String> getMessagesEnvoyesParLeServeur() {
        return messagesEnvoyesParLeServeur;
    }

    /**
     * Retourne le dernier message envoyé par le serveur.
     *
     * Cette méthode est particulièrement utile pour vérifier
     * rapidement le résultat d'une action dans un test.
     *
     * @return dernier message envoyé ou null si aucun message
     * n'a encore été enregistré
     */
    public String getDernierMessage(){
        if (messagesEnvoyesParLeServeur.isEmpty()){
            return null;
        }
        return messagesEnvoyesParLeServeur.getLast();
    }

    /**
     * Affiche dans la console l'ensemble des messages enregistrés.
     *
     * Cette méthode est principalement utilisée pour faciliter
     * le débogage des tests d'intégration.
     */
    public void afficherTousLesMessagesEnvoyesParLeServeur(){
        for (int i = 0; i < messagesEnvoyesParLeServeur.size(); i++){
            System.out.println(messagesEnvoyesParLeServeur.get(i));
        }
    }
}
