package enums;

public class Proposition {
	private EPersonnage personnage;
	private EArme arme;
	private ELieu lieu;
	
	
    public Proposition(ELieu lieu, EPersonnage personnage, EArme arme) {
        if (lieu == null || personnage == null || arme == null) 
        	throw new IllegalArgumentException("on ne peut pas creer avec null dawg");
    	this.lieu = lieu;
        this.personnage = personnage;
        this.arme = arme;
    }

    public EPersonnage getPersonnage() {
        return personnage;
    }

    public ELieu getLieu() {
        return lieu;
    }

    public EArme getArme() {
        return arme;
    }
    
    public boolean contient(Carte carte) {
        return carte.equals(this.personnage) || 
               carte.equals(this.lieu)       || 
               carte.equals(this.arme);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Proposition e)
        	return lieu == e.lieu && personnage == e.personnage && arme == e.arme;
        return false;
    }
}