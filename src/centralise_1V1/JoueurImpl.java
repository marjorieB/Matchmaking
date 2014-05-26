package centralise_1V1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class JoueurImpl extends UnicastRemoteObject implements JoueurItf {
	
	private static final long serialVersionUID = 1L;
	private boolean dejaContacte = false;
	private int latency;
	private int premadeSize; // pour l'instant cette information n'est pas prise en compte,
							// dans un premier temps en considère des combats 1v1
	private int summonerElo;
	private int duration;
	
	public JoueurImpl(int latency, int summonerElo) throws RemoteException {
		this.latency = latency;
		this.summonerElo = summonerElo;
	}

	@Override
	public void jouer(JoueurItf j) {
		
		if (!dejaContacte) {
			dejaContacte = true;
			try {
				System.out.println("joueur: elo = " + this.getSummonerElo() + " latence = " + this.getLatency() +
						" durée = " + this.getDuration() + " contre joueur: elo = " + j.getSummonerElo() +
						" latence = " + j.getLatency() + " durée = " + j.getDuration());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Override
	public void InfoJoueur(JoueurItf j) throws RemoteException {
		dejaContacte = true;
		j.jouer(this);
	}

	@Override
	public int getSummonerElo() throws RemoteException {
		return summonerElo;
	}

	@Override
	public void setSummonerElo(int elo) throws RemoteException {
		summonerElo = elo;
	}

	@Override
	public int getDuration() throws RemoteException {
		return duration;
	}

	@Override
	public void setDuration(int duration) throws RemoteException {
		this.duration = duration;		
	}

	@Override
	public int getLatency() throws RemoteException {
		return latency;
	}

	@Override
	public void setLatency(int latency) throws RemoteException {
		this.latency = latency;
	}
}
