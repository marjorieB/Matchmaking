package centralise_1V1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JoueurItf extends Remote {
	public void jouer(JoueurItf j) throws RemoteException;
	public void InfoJoueur(JoueurItf j) throws RemoteException;
	public int getSummonerElo() throws RemoteException;
	public void setSummonerElo(int elo) throws RemoteException;
	public int getDuration() throws RemoteException;
	public void setDuration(int duration) throws RemoteException;
	public int getLatency() throws RemoteException;
	public void setLatency(int latency) throws RemoteException;
}

