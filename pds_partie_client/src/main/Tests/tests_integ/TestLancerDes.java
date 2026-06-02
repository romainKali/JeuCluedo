package tests_integ;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;

import ihm.ControllerMain;
import ihm.InterfaceMain;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;


public class TestLancerDes { // TODO gemini
	InterfaceMain main;
	FakeThreadReseau reseau;
	ControllerMain controller;
	

	public void initAll() throws IOException {
		Platform.startup(() -> {});
		
		main = new InterfaceMain();
		reseau = new FakeThreadReseau(main);
		
		main.setReseau(reseau);
		
		
		FXMLLoader loader = new FXMLLoader(
			    getClass().getResource(InterfaceMain.MAIN_FXML)
			);

		loader.load();

		controller = loader.getController();
		controller.setleMain(main);
	}
	
	public void TestSendLancerDes() {
		controller.lancedes.fire();
		
		assertEquals("@LANCER_DES" , reseau.getMessageReceived() );
	}
	
	public void TestReceiveDes() {
		
	}
}