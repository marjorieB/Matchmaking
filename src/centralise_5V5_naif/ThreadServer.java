package centralise_5V5_naif;

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
	private LinkedList<JoueurItf> [][] matriceJoueurs;
	private int arg1;
	private Stats stats;
	private FileWriter fw;
	private int nb_matchs = 0;
	private long tempsDeb = 0;
	private int nb_connexions = 0;
	private TacheConnexions tc;
	private TacheInterneServeur tis;
	private Timer timer;
	
	public ThreadServer(LinkedList<JoueurItf> liste, String arg, String arg1) {
		this.arg1 = Integer.parseInt(arg1);
		matriceJoueurs = new LinkedList[(3000/this.arg1)][(3000/this.arg1)];
		for (int i = 0; i < matriceJoueurs.length; i++) {
			for (int j = 0; j < matriceJoueurs[0].length; j++) {
				matriceJoueurs[i][j] = new LinkedList<JoueurItf>();
			}
		}
		joueurs = liste;
		try {
			fw = new FileWriter("nb_connexions_par_seconde_naif_5V5_criteres_" + arg1 + "_" + arg + ".csv");
			fw.write("nb_connexions_par_seconde\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tc = new TacheConnexions();
		tis = new TacheInterneServeur();
		timer = new Timer();
		timer.scheduleAtFixedRate(tc, 0, 1000);
		timer.scheduleAtFixedRate(tis, 0, 5000);
		this.stats = new Stats(arg, "naif_5V5_criteres_" + arg1 + "_");
	}
	
	public void run () {
		boolean first = true;
		int x = 0, y = 0;
		while (true) {
			synchronized(joueurs) {
				while (joueurs.size() >= 1) {
					if (first) {
						tempsDeb = System.currentTimeMillis();
						first = false;
					}
					nb_connexions += 1;
					x = joueurs.getFirst().getSummonerElo() / arg1;
					y = joueurs.getFirst().getLatency() / arg1;
					matriceJoueurs[x][y].add(joueurs.removeFirst());
					synchronized(matriceJoueurs) {
						if (matriceJoueurs[x][y].size() == 10) {
							formeEquipe(x, y);
						}
					}
				}
			}
			
		}
	}
	
	public void formeEquipe (int x, int y) {
		int sum1 = 0;
		int sum2 = 0;
		HashMap<Integer, JoueurItf> team1 = new HashMap<Integer, JoueurItf>();
		HashMap<Integer, JoueurItf> team2 = new HashMap<Integer, JoueurItf>();
		JoueurItf e1 = null;
		JoueurItf e2 = null;
		
		for (int i = 0; i < matriceJoueurs[x][y].size(); i++) {
			double min = Double.MAX_VALUE;
			if (!team1.containsKey(matriceJoueurs[x][y].get(i).getSummonerId()) && 
					!team2.containsKey(matriceJoueurs[x][y].get(i).getSummonerId())) {
				e1 = matriceJoueurs[x][y].get(i);
			}
			else {
				continue;
			}
			double distance = 0;
			for (int j = 1; j < matriceJoueurs[x][y].size(); j++) {
				if (!team1.containsKey(matriceJoueurs[x][y].get(j).getSummonerId()) && 
						!team2.containsKey(matriceJoueurs[x][y].get(j).getSummonerId())) {
					if ((!matriceJoueurs[x][y].get(j).equals(e1)) && (distance = distance(matriceJoueurs[x][y].get(j).getSummonerElo(), matriceJoueurs[x][y].get(j).getLatency(), 
							e1.getSummonerElo(), e1.getLatency())) < min) {
						min = distance;
						e2 = matriceJoueurs[x][y].get(j);
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
		matriceJoueurs[x][y].clear();
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
				nb_connexions = 0;
			}
		}
	}
	
	public class TacheInterneServeur extends TimerTask {
		LinkedList<Point> aMatcher = new LinkedList<Point>();
		
		@Override
		public void run() {
			synchronized(matriceJoueurs) {
				for (int i = 0; i < matriceJoueurs.length; i++) {
					for (int j = 0; j < matriceJoueurs[0].length; j++) {
						for (JoueurItf joueur : matriceJoueurs[i][j]) {
							joueur.setDuration(joueur.getDuration() + 1);
							if (joueur.getDuration() >= 2) {
								aMatcher.add(new Point(i, j));
							}
						}
					}
				}
				if (!aMatcher.isEmpty()) {
					for (Point pt : aMatcher) {
						chercherJoueurs(pt);
					}
				}
				aMatcher.clear();
			}
		}
	}
	
	public void chercherJoueurs(Point pt) {
		int m = 0;
		
		while ((matriceJoueurs[pt.getX()][pt.getY()].size() < 10) && m < ((3000 / arg1) - 1)) {
			m++;
			for (int k = -m; k <= m; k++) {
				for (int l = -m; l <= m; l++) {
					if (((pt.getX() + k) < matriceJoueurs.length) && ((pt.getX() + k) >= 0)
						&& ((pt.getY() + l) < matriceJoueurs[0].length) && ((pt.getY() + l) >= 0)
						&& ((l != 0) || (k != 0))) {
						while ((matriceJoueurs[pt.getX() + k][pt.getY() + l].size() != 0) &&
								(matriceJoueurs[pt.getX()][pt.getY()].size() != 10)) {
							matriceJoueurs[pt.getX()][pt.getY()].add(matriceJoueurs[pt.getX() + k][pt.getY() + l].removeFirst());
						}
						if (matriceJoueurs[pt.getX()][pt.getY()].size() == 10) {
							break;
						}
					}
				}
				if (matriceJoueurs[pt.getX()][pt.getY()].size() == 10) {
					break;
				}
			}
			if (matriceJoueurs[pt.getX()][pt.getY()].size() == 10) {
				break;
			}
		}
		if (matriceJoueurs[pt.getX()][pt.getY()].size() == 10) {
			formeEquipe(pt.getX(), pt.getY());
		}	
	}
}
