package centralise_1V1;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Serveur {
	public static void main (String [] args) {
		try {
			// création et enregistrement du serveur
			ServeurItf s = new ServeurImpl();
			UnicastRemoteObject.exportObject(s, 0);
			Registry registry = LocateRegistry.getRegistry("localhost");
			
			registry.rebind("serveur", s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
