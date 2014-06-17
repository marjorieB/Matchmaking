package centralise_1V1_random;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;



public class ThreadServer extends Thread {
	private LinkedList<JoueurItf> joueurs;
	private Statistiques stats;
	private int nb_matchs = 0;
	private int nb_connexions = 0;
	
	public ThreadServer(LinkedList<JoueurItf> liste) {
		joueurs = liste;
		this.stats = new Statistiques();
	}
	
	public void run () {
		while (true) {
			synchronized(joueurs) {
				while (joueurs.size() >= 2) {
					nb_connexions += 2;
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
			if (nb_matchs == 499840) { // a ajuster en fonction du nombre de joueurs dans le fichier csv
				stats.afficher_stats();
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
}