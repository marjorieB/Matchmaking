package centralise_1V1_random;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import centralise_1V1_utilitaire.*;



public class ThreadServer extends Thread {
	private LinkedList<JoueurItf> joueurs;
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
			fw = new FileWriter("nb_connexions_par_seconde_random"+ arg + ".csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tc = new TacheConnexions();
		timer = new Timer();
		timer.scheduleAtFixedRate(tc, 0, 1000);
		this.stats = new Stats(arg, "random");
	}
	
	public void run () {
		boolean first = true;
		while (true) {
			synchronized(joueurs) {
				while (joueurs.size() >= 2) {
					if (first) {
						tempsDeb = System.currentTimeMillis();
						first = false;
					}
					nb_connexions += 2;
					/*if (nb_connexions == 100000) {
						tc.cancel();
					}*/
					EnvoiInfoJoueur(joueurs.remove(), joueurs.remove());
				}
			}
		}
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, JoueurItf j2) {
		DataOutputStream dos1;
		DataOutputStream dos2;

		j1.setTime2(System.currentTimeMillis());
		j2.setTime2(System.currentTimeMillis());
		
		stats.miseAJour(j1, j2);
		
		try {
			dos1 = new DataOutputStream(j1.getSocket().getOutputStream());
			dos2 = new DataOutputStream(j2.getSocket().getOutputStream());
			
			/* on envoi au joueur le temps qu'il a attendu dans la file du serveur
			 * suivi du summonerElo, de la latence et de la durée dans la file d'attente
			 * de son adversaire 
			 */
			dos1.writeBytes("infoJoueur " + j1.getDuration() + " " + j2.getSummonerElo()
					+ " " + j2.getLatency() + " " + j2.getDuration() + "\n");
			dos2.writeBytes("InfoJoueur "  + j2.getDuration() + " " + j1.getSummonerElo()
					+ " " + j1.getLatency() + " " + j1.getDuration() + "\n");
			nb_matchs += 2;
			if (nb_matchs == 100000) { // a ajuster en fonction du nombre de joueurs dans le fichier csv
				tc.cancel();
				stats.fin(tempsDeb);
				System.exit(0);
			}
			/*if (nb_connexions%2 == 0) {
				if (nb_connexions == nb_matchs) {
					stats.afficher_stats();
				}
			}
			else {
				if ((nb_connexions - 1) == nb_matchs) {
					stats.afficher_stats();
				}
			}*/
			j1.getSocket().close();
			j2.getSocket().close();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("nombre de connexions " + nb_connexions);
				nb_connexions = 0;
			}
		}
		
	}
}
