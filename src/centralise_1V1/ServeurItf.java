package centralise_1V1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServeurItf extends Remote {
	public void matchmaking(JoueurItf j) throws RemoteException;
}
