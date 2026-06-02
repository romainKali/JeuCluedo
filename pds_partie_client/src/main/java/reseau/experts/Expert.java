package reseau.experts;

import ihm.InterfaceMain;
import reseau.ExpertException;

/**
 * Classe abstraite représentant un maillon dans la chaîne de responsabilité (Chain of Responsibility).
 * <p>
 * Ce design pattern est utilisé pour analyser et traiter de manière séquentielle les différentes
 * trames réseau reçues du serveur. Si un expert sait traiter le message, il l'exécute.
 * Sinon, il passe le message à l'expert suivant dans la chaîne.
 * </p>
 */
public abstract class Expert {

    /** Le maillon suivant dans la chaîne de responsabilité. */
    private Expert suivant;

    /**
     * Récupère l'expert suivant dans la chaîne.
     * * @return L'instance de l'expert suivant, ou null s'il n'y en a pas.
     */
    public Expert getSuivant() {
        return suivant;
    }

    /**
     * Définit l'expert suivant dans la chaîne de responsabilité.
     * * @param suivant L'expert à chaîner après celui-ci.
     * @throws IllegalArgumentException Si l'expert fourni est null.
     */
    public void setSuivant(Expert suivant) {
        if (suivant == null){
            throw new IllegalArgumentException("On ne peut initialiser le maillon suivant avec un pointeur null !");
        }
        this.suivant = suivant;
    }

    /**
     * Tente de traiter le message reçu du réseau.
     * <p>
     * Si l'expert courant sait traiter le message ({@link #saitTraiter(String)}), il exécute
     * l'action correspondante. Sinon, il délègue le traitement à l'expert suivant.
     * </p>
     * * @param message La trame reçue depuis le serveur.
     * @param main L'instance principale du client permettant d'agir sur l'IHM et l'état du jeu.
     * @throws ExpertException Si aucun expert dans la chaîne n'a pu traiter le message.
     * @throws IllegalArgumentException Si le message ou l'instance main sont null.
     */
    public void traiter(String message, InterfaceMain main) throws ExpertException {
        if (message == null || main == null){
            throw new IllegalArgumentException("Un des éléments permettant de traiter la requête est un pointeur null !");
        }

        if (saitTraiter(message)) {
            executerTraitement(message, main);
        }
        else if (getSuivant() != null){
            getSuivant().traiter(message, main);
        }
        else {
            main.printerr(message);
            throw new ExpertException("Aucun des maillons de la chaîne de responsabilité n'a été en mesure de traiter le message reçu : " + message);
        }
    }

    /**
     * Vérifie si cet expert est capable de traiter le message spécifié.
     * * @param message Le message à vérifier (souvent évalué via des expressions régulières).
     * @return {@code true} si l'expert peut traiter le message, {@code false} sinon.
     */
    public abstract boolean saitTraiter(String message);

    /**
     * Exécute le traitement spécifique associé au message.
     * * @param message Le message réseau validé.
     * @param main L'instance d'InterfaceMain pour mettre à jour la logique ou l'IHM.
     */
    public abstract void executerTraitement(String message, InterfaceMain main);
}