package ihm;

import enums.EPersonnage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class LoginController {
	
	@FXML
	private TextField nomField;

    @FXML
    private ListView<EPersonnage> personList;
	
	@FXML
	private void initialize() {
        personList.setItems(FXCollections.observableArrayList(EPersonnage.values()));
        personList.getSelectionModel().selectFirst();
        Platform.runLater(() -> nomField.requestFocus());
	}

	public String getNom() {
		return nomField.getText();
	}

	public EPersonnage getPersonnage() {
		EPersonnage p = personList.getSelectionModel().getSelectedItem();
        if (p == null) return EPersonnage.Colonel_Moutarde;
        else return p;
	}
	
}