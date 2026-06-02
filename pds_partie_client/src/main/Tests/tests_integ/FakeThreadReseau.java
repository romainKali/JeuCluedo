package tests_integ;

import java.util.ArrayList;

import ihm.InterfaceMain;
import reseau.ThreadConsole;



// Classe faite que pour les tests d'integration

public class FakeThreadReseau extends ThreadConsole {

	private ArrayList<String> messages_received = new ArrayList<>();
	private ArrayList<String> messages_sent = new ArrayList<>();

	
	public FakeThreadReseau(InterfaceMain client) {
		super(client);
	}
	
    public void run() {
        while (true) {
            String message = recevoirMessage();
			if (message != null) {
			    messages_received.add(message);
			    //traiterMessage(message);
			}
			else {
				break;
			}
        }
    }
    
    public void recevoirMessage(String s) {
	    messages_received.add(s);
	    //traiterMessage(message);
    }
    
    
    public void envoyerMessage(String s) {
        messages_sent.add(s);
        //super.envoyerMessage(s);
    }
    
    public String getMessageReceived() {
    	return messages_received.getLast();
    }
    
    
	
}