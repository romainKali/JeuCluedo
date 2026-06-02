package metier.Jeu;

import metier.cluedo.carte.Carte;
import metier.cluedo.carte.EArme;
import metier.cluedo.carte.ELieu;
import metier.cluedo.carte.EPersonnage;

public class Hypothese {
    private ELieu lieu;
    private EPersonnage personnage;
    private EArme arme;


    public Hypothese(ELieu lieu, EPersonnage personnage, EArme arme) {
        setLieu(lieu);
        setPersonnage(personnage);
        setArme(arme);
    }

    private void setLieu(ELieu lieu) {
        if (lieu == null) {
            throw new IllegalArgumentException("Le lieu ne peut être un pointeur null");
        }
        this.lieu = lieu;
    }

    public ELieu getLieu() {return lieu;}

    private void setPersonnage(EPersonnage personnage) {
        if (personnage == null) {
            throw new IllegalArgumentException("Le personnage ne peut être un pointeur null");
        }
        this.personnage = personnage;
    }

    public EPersonnage getPersonnage() {return personnage;}

    private void setArme(EArme arme) {
        if (arme == null) {
            throw new IllegalArgumentException("L'arme ne peut être un pointeur null");
        }
        this.arme = arme;
    }

    public EArme getArme() {return arme;}

    public boolean contient(Carte carte){
        return carte.equals(lieu) || carte.equals(personnage) || carte.equals(arme);
    }

    public boolean equals(Object o){
        if (o == null){return false;}
        if (o == this){return true;}
        if (o instanceof Hypothese h){
            return h.getLieu().equals(lieu) && h.getPersonnage().equals(personnage) && h.getArme() == arme;
        }
        return false;
    }
}
