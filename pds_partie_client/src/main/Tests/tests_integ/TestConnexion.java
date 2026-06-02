package tests_integ;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import enums.EPersonnage;
import ihm.InterfaceMain;
import javafx.application.Platform;


public class TestConnexion {
	InterfaceMain main;
	FakeThreadReseau reseau;
	
	

	public void initAll() {
		Platform.startup(() -> {});
		
		main = new InterfaceMain();
		reseau = new FakeThreadReseau(main);
		
		main.setReseau(reseau);
	}
	
	@Test
	public void TestConnexion() {
		main.connexion("Alice", EPersonnage.Colonel_Moutarde);
		
		assertEquals( reseau.getMessageReceived() , "@CONNEXION Alice P1" );
	}
	
	@Test
	public void TestDeconnexion() {
		main.deconnexion();
		assertEquals("@DECONNEXION", reseau.getMessageReceived());
	}
}