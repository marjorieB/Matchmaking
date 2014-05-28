package centralise_1V1_naif;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class JoueurImpl implements JoueurItf {
	
	private boolean dejaContacte = false;
	private int latency;
	private int premadeSize; // pour l'instant cette information n'est pas prise en compte,
							// dans un premier temps en considère des combats 1v1
	private int summonerElo;
	private int duration;
	private Socket s;

	
	public JoueurImpl(int summonerElo, int latency) {
		this.summonerElo = summonerElo;
		this.latency = latency;
	}

	public JoueurImpl (int summonerElo, int latency, Socket s) {
		this.summonerElo = summonerElo;
		this.latency = latency;
		this.s = s;
	}
	
	@Override
	public void jouer(int duration, int summonerEloAdversaire, int latencyAdversaire, int durationAdversaire) {
		
		if (!dejaContacte) {
			dejaContacte = true;
			System.out.println("joueur: elo = " + this.getSummonerElo() + " latence = " + this.getLatency() +
					" durée = " + duration + " contre joueur: elo = " + summonerEloAdversaire +
					" latence = " + latencyAdversaire + " durée = " + durationAdversaire);
		}
	}

	public void match() {
		Socket s;
		OutputStream os;
		DataOutputStream dos;
		BufferedReader br;
		String recu;
		String [] tab;
		
		try {
			// attente de l'application pendant un temps random pour avoir des séquences d'exécution différentes
			Thread.sleep((int)(Math.random()) * 5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			s = new Socket("localhost", 12345);
			os = s.getOutputStream();
			dos = new DataOutputStream(os);
			//demande de matchmaking au serveur
			dos.writeBytes("matchmaking " + summonerElo + " " + latency);
			System.out.println("demande de matchmaking envoyée au serveur");
			
			// réception de la réponse du serveur.
			for (int i = 0; i < 2; i++) {
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				recu = br.readLine();
				tab = recu.split(" ");
				if (tab[0].equalsIgnoreCase("infoJoueur")) {
					jouer(Integer.parseInt(tab[1]), Integer.parseInt(tab[2]),
							Integer.parseInt(tab[3]), Integer.parseInt(tab[4]));
				}
				/*else if (tab[0].equalsIgnoreCase("jouer")) {
					//jouer();
				}*/
				else {
					System.out.println("commande inconnue");
				}
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int getSummonerElo() {
		return summonerElo;
	}

	@Override
	public void setSummonerElo(int elo) {
		summonerElo = elo;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;		
	}

	@Override
	public int getLatency() {
		return latency;
	}

	@Override
	public void setLatency(int latency) {
		this.latency = latency;
	}

	public Socket getSocket() {
		return s;
	}

	public void setSocket(Socket s) {
		this.s = s;
	}
}
