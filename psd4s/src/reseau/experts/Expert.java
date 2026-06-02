package reseau.experts;

import exception.ExpertException;
import reseau.ConnexionUtilisateur;

/**
 * Classe abstraite représentant un maillon de la chaîne de responsabilité.
 * <p>
 * Chaque expert est spécialisé dans le traitement d'un type particulier
 * de message réseau. Lorsqu'un expert ne sait pas traiter un message,
 * il le transmet à l'expert suivant de la chaîne.
 * </p>
 */
public abstract class Expert {
    /**
     * Expert suivant dans la chaîne de responsabilité.
     */
    private Expert suivant;


    /**
     * Retourne l'expert suivant dans la chaîne.
     *
     * @return l'expert suivant
     */
    public Expert getSuivant() {
        return suivant;
    }

    /**
     * Définit l'expert suivant dans la chaîne de responsabilité.
     *
     * @param suivant le nouvel expert suivant
     * @throws IllegalArgumentException si l'expert passé en paramètre vaut null
     */
    public void setSuivant(Expert suivant) {
        if (suivant == null){
            throw new IllegalArgumentException("On ne peut initialiser le maillon suivant avec un pointeur null !");
        }
        this.suivant = suivant;
    }

    /**
     * Tente de traiter le message reçu.
     * <p>
     * Si l'expert courant sait traiter le message, il exécute le traitement
     * correspondant. Sinon, le message est transmis à l'expert suivant de la
     * chaîne. Si aucun expert ne peut traiter le message, une exception est
     * levée.
     * </p>
     *
     * @param message message reçu du client
     * @param utilisateur utilisateur ayant envoyé le message
     * @throws IllegalArgumentException si le message ou l'utilisateur vaut null
     * @throws ExpertException si aucun expert de la chaîne ne peut traiter le message
     */
    public void traiter(String message, ConnexionUtilisateur utilisateur) {
        if (message == null || utilisateur == null){
            throw new IllegalArgumentException("Un des éléments permettant de traiter la requête est un pointeur null! (éléments = type de requete, utilisateur)");
        }


        if (saitTraiter(message)) {
            executerTraitement(message, utilisateur);
        }
        else if (getSuivant() != null){
            getSuivant().traiter(message, utilisateur);
        }
        else {
            throw new ExpertException("Aucun des maillons de la chaîne de responsabilité n'a été en mesure de traiter le message recu");
        }
    }

    /**
     * Indique si l'expert est capable de traiter le message fourni.
     *
     * @param message message à analyser
     * @return true si l'expert sait traiter le message, false sinon
     */
    public abstract boolean saitTraiter(String message);

    /**
     * Exécute le traitement associé au message.
     *
     * @param message message à traiter
     * @param utilisateur utilisateur ayant envoyé le message
     */
    public abstract void executerTraitement(String message, ConnexionUtilisateur utilisateur);
}
