package centralise_5V5_random;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import centralise_5V5_utilitaire.*;



public class ThreadServer extends Thread {
	private LinkedList<JoueurItf> joueurs;
	private LinkedList<JoueurItf> joueurs10 = new LinkedList<JoueurItf>();
	private Stats stats;
	private FileWriter fw;
	private int nb_matchs = 0;
	private long tempsDeb = 0;
	private int nb_connexions = 0;
	private TacheConnexions tc;
	private Timer timer;
	
	public ThreadServer(LinkedList<JoueurItf> liste, String arg) {
		joueurs = liste;
		try {
			fw = new FileWriter("nb_connexions_par_seconde_random_5V5_"+ arg + ".csv");
			fw.write("nb_connexions_par_seconde\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tc = new TacheConnexions();
		timer = new Timer();
		timer.scheduleAtFixedRate(tc, 0, 1000);
		this.stats = new Stats(arg, "random_5V5_");
	}
	
	public void run () {
		boolean first = true;
		while (true) {
			synchronized(joueurs) {
				while (joueurs.size() >= 10) {
					if (first) {
						tempsDeb = System.currentTimeMillis();
						first = false;
					}
					nb_connexions += 10;
					for (int i = 0; i < 10; i++) {
						joueurs10.add(joueurs.remove());
					}
					formeEquipe(joueurs10);
				}
			}
		}
	}
	
	public void formeEquipe (LinkedList<JoueurItf> equipes) {
		int sum1 = 0;
		int sum2 = 0;
		HashMap<Integer, JoueurItf> team1 = new HashMap<Integer, JoueurItf>();
		HashMap<Integer, JoueurItf> team2 = new HashMap<Integer, JoueurItf>();
		JoueurItf e1 = null;
		JoueurItf e2 = null;
		
		for (int i = 0; i < equipes.size(); i++) {
			double min = Double.MAX_VALUE;
			if (!team1.containsKey(equipes.get(i).getSummonerId()) && 
					!team2.containsKey(equipes.get(i).getSummonerId())) {
				e1 = equipes.get(i);
			}
			else {
				continue;
			}
			double distance = 0;
			for (int j = 1; j < equipes.size(); j++) {
				if (!team1.containsKey(equipes.get(j).getSummonerId()) && 
						!team2.containsKey(equipes.get(j).getSummonerId())) {
					if ((!equipes.get(j).equals(e1)) && (distance = distance(equipes.get(j).getSummonerElo(), equipes.get(j).getLatency(), 
							e1.getSummonerElo(), e1.getLatency())) < min) {
						min = distance;
						e2 = equipes.get(j);
					}
				}
			}
			if (((sum1 < sum2) && ((e1.getSummonerElo() + e1.getLatency()) < (e2.getSummonerElo() + e2.getLatency()))) || 
					((sum2 < sum1) && ((e2.getSummonerElo() + e2.getLatency()) < (e1.getSummonerElo() + e1.getLatency())))) {
				sum1 += e2.getSummonerElo() + e2.getLatency();
				team1.put(new Integer(e2.getSummonerId()), e2);
				sum2 += e1.getSummonerElo() + e1.getLatency();
				team2.put(new Integer(e1.getSummonerId()), e1);
			}
			else {
				sum1 += e1.getSummonerElo() + e1.getLatency();
				team1.put(new Integer(e1.getSummonerId()), e1);
				sum2 += e2.getSummonerElo() + e2.getLatency();
				team2.put(new Integer(e2.getSummonerId()), e2);
			}
		}
		joueurs10.clear();
		EnvoiInfoJoueur(team1, team2);
	}
	
	public double distance (int summonerElo1, int latence1, int summonerElo2, int latence2) {
		return Math.sqrt(Math.pow((summonerElo1 - summonerElo2), 2) + Math.pow((latence1 - latence2), 2));
	}
	
	public void EnvoiInfoJoueur(HashMap<Integer, JoueurItf> team1, HashMap<Integer, JoueurItf> team2) {
		DataOutputStream dos1[] = new DataOutputStream[5];
		DataOutputStream dos2[] = new DataOutputStream[5];
		long time = System.currentTimeMillis();
		int i = 0;
		int k = 0;
		
		try {
			for (Integer id : team1.keySet()) {
				team1.get(id).setTime2(time);
				dos1[i] = new DataOutputStream(team1.get(id).getSocket().getOutputStream());
				i++;
			}
			for (Integer id : team2.keySet()) {
				team2.get(id).setTime2(time);
				dos2[k] = new DataOutputStream(team2.get(id).getSocket().getOutputStream());
				k++;
			}

			stats.miseAJour(team1, team2);
			i = 0;
			for (Integer id : team1.keySet()) {
				dos1[i].writeBytes("infoJoueur TEAM " + team1.get(id).getSummonerId() 
						+ team1.get(id).getLatency() + team1.get(id).getDuration());
				dos2[i].writeBytes("infoJoueur ADVERSAIRES " + team1.get(id).getSummonerId() 
						+ team1.get(id).getLatency() + team1.get(id).getDuration());
				i++;
			}
			i = 0;
			for (Integer id : team2.keySet()) {
				if (i != 5) {
					dos2[i].writeBytes("infoJoueur TEAM " + team2.get(id).getSummonerId() 
							+ team2.get(id).getLatency() + team2.get(id).getDuration());
					dos1[i].writeBytes("infoJoueur ADVERSAIRES " + team2.get(id).getSummonerId() 
							+ team2.get(id).getLatency() + team2.get(id).getDuration());
				}
				else {
					dos2[i].writeBytes("infoJoueur TEAM " + team2.get(id).getSummonerId() 
							+ team2.get(id).getLatency() + team2.get(id).getDuration() + "\n");
					dos1[i].writeBytes("infoJoueur ADVERSAIRES " + team2.get(id).getSummonerId() 
							+ team2.get(id).getLatency() + team2.get(id).getDuration() + "\n");
				}
				i++;
			}
			nb_matchs += 10;
		//	System.out.println("NOMBRE DE JOUEURS MATCHES " + nb_matchs);
			if (nb_matchs == 100000) { // a ajuster en fonction du nombre de joueurs dans le fichier csv
				tc.cancel();
				stats.fin(tempsDeb);
				System.exit(0);
			}
			for (Integer id : team1.keySet()) {
				team1.get(id).getSocket().close();
			}
			for (Integer id : team2.keySet()) {
				team2.get(id).getSocket().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class TacheConnexions extends TimerTask {

		@Override
		public void run() {
			synchronized(joueurs) {
				try {
					fw.write(nb_connexions + "\n");
					fw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//System.out.println("nombre de connexions " + nb_connexions);
				nb_connexions = 0;
			}
		}
		
	}
}