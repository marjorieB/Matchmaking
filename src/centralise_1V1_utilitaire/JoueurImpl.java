package centralise_1V1_utilitaire;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class JoueurImpl implements JoueurItf {
	
	private boolean dejaContacte = false;
	private int summonerId;
	private int latency;
	private int premadeSize; /* pour l'instant cette information n'est pas prise en compte,
							 dans un premier temps en considère des combats 1v1 */
	private int summonerElo;
	private int duration;
	private Socket s;
	private long time1 = 0;
	private long time2 = 0;

	private static int nb = 0;
	
	
	public JoueurImpl(int summonerId, int summonerElo, int latency) {
		this.summonerId = summonerId;
		this.summonerElo = summonerElo;
		this.latency = latency;
	}

	public JoueurImpl (int summonerId, int summonerElo, int latency, Socket s, long time1) {
		this.summonerId = summonerId;
		this.summonerElo = summonerElo;
		this.latency = latency;
		this.s = s;
		this.time1 = time1;
	}
	
	@Override
	public void jouer(int duration, int summonerEloAdversaire, int latencyAdversaire, int durationAdversaire) {
		if (!dejaContacte) {
			dejaContacte = true;
			/*System.out.println("joueur: elo = " + this.getSummonerElo() + " latence = " + this.getLatency() +
					" durée = " + duration + " contre joueur: elo = " + summonerEloAdversaire +
					" latence = " + latencyAdversaire + " durée = " + durationAdversaire);*/
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
			int i = (int)(Math.random() * 10000);
			// attente de l'application pendant un temps random pour avoir des séquences d'exécution différentes
			Thread.sleep(i);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			InetAddress addr = InetAddress.getByName("132.227.199.23");
 			s = new Socket(addr, 12345);
			//s = new Socket("localhost", 12345);
			s.setSoTimeout(0);
			os = s.getOutputStream();
			dos = new DataOutputStream(os);
			// demande de matchmaking au serveur
			dos.writeBytes("matchmaking " + summonerId + " " + summonerElo + " " + latency + "\n");
			/*System.out.println("joueur: summonerElo = " + summonerElo + 
					" latence = " + latency + " demande de matchmaking au serveur");*/
			
			// réception de la réponse du serveur
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			recu = br.readLine();
			tab = recu.split(" ");
			if (tab[0].equalsIgnoreCase("infoJoueur")) {
				jouer(Integer.parseInt(tab[1]), Integer.parseInt(tab[2]),
						Integer.parseInt(tab[3]), Integer.parseInt(tab[4]));
			}
			else {
				System.out.println("commande inconnue");
			}	
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public int getSummonerId() {
		return summonerId;
	}

	public void setSummonerId(int summonerId) {
		this.summonerId = summonerId;
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

	@Override
	public void setTime2(long time2) {
		this.time2 = time2; 
	}

	@Override
	public long getTime1() {
		return time1;
	}

	@Override
	public long getTime2() {
		return time2;
	}
}
