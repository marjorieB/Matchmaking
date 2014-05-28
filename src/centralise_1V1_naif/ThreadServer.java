package centralise_1V1_naif;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;



public class ThreadServer extends Thread {
	private LinkedList<JoueurItf> joueurs;
	private LinkedList<JoueurItf> l1;
	private LinkedList<JoueurItf> l2;
	private LinkedList<JoueurItf> l3;
	private Timer timer;
	
	public ThreadServer(LinkedList<JoueurItf> liste) {
		joueurs = liste;
		l1 = new LinkedList<JoueurItf>();
		l2 = new LinkedList<JoueurItf>();
		l3 = new LinkedList<JoueurItf>();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TacheServeur(), 0, 5000);
	}
	
	public void run () {
		while (true) {
			synchronized(joueurs) {
				while (joueurs.isEmpty()) {
					try {
						joueurs.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				matchmaking(joueurs.getFirst());
				joueurs.removeFirst();				
			}
		}
	}
	
	public void matchmaking (JoueurItf joueur) {
		int i = 0;
		int j = 0;
		int k = 0;
		JoueurItf tmp;
		LinkedList<JoueurItf> sl1 = new LinkedList<JoueurItf>();
		LinkedList<JoueurItf> sl2 = new LinkedList<JoueurItf>();
		LinkedList<JoueurItf> res = new LinkedList<JoueurItf>();
		ListIterator<JoueurItf> it1 = l1.listIterator();
		ListIterator<JoueurItf> it2 = l2.listIterator();
		ListIterator<JoueurItf> it3 = l3.listIterator();
		
		joueur.setDuration(0);
		if (l1.isEmpty()) {
			synchronized (l1) {
				it1.add(joueur);
			}
			synchronized (l2) {
				l2.add(joueur);
			}
			synchronized (l3) {
				l3.add(joueur);
			}
		}
		else {
			synchronized (l1) {
				while(it1.hasNext()) {
					tmp = it1.next();
					if (tmp.getSummonerElo() < joueur.getSummonerElo()) {
						i++;
					}
					// on récupère les joueurs de summonerElo étant égale à plus ou moins 20 unités du summonerElo du joueur passé en paramètre
					if((tmp.getSummonerElo() > (joueur.getSummonerElo() - 20)) &&
							(tmp.getSummonerElo() < (joueur.getSummonerElo() + 20))) {
						sl1.add(tmp);
					}
				}
			}
			synchronized (l2) {
				while (it2.hasNext()) {
					tmp = it2.next();
					if (tmp.getLatency() < joueur.getLatency()) {
						j++;
					}
					if ((tmp.getLatency() > joueur.getLatency() - 20) && 
							(tmp.getLatency() < joueur.getLatency() + 20)) {
						sl2.add(tmp);
					}					
				}
			}
			synchronized (l3) {
				while (it3.hasNext() && (it3.next().getDuration() < joueur.getDuration())) {
					k++;
				}
			}
			res = intersection(sl1, sl2);
			if (res.isEmpty()) {
				synchronized (l1) {
					l1.add(i, joueur);
				}
				synchronized (l2) {
					l2.add(j, joueur);
				}
				synchronized (l3) {
					l3.add(k, joueur);
				}
			}
			else {
				while((tmp = choixJoueur(joueur, res)) == null) {
					System.out.println("tmp est nul, ce cas ne peut pas arriver!!!");
					l1.remove(tmp);
					l2.remove(tmp);
					l3.remove(tmp);
				}
				EnvoiInfoJoueur(tmp, joueur);
				/*joueur.InfoJoueur(tmp);
				tmp.InfoJoueur(joueur);*/
				synchronized(l1) {
					l1.remove(tmp);
				}
				synchronized (l2) {
					l2.remove(tmp);
				}
				synchronized (l3) {
					l3.remove(tmp);
				}
			}
		}
		
	}

	public LinkedList<JoueurItf> intersection (LinkedList<JoueurItf> sl1, LinkedList<JoueurItf> sl2) {
		LinkedList<JoueurItf> res = new LinkedList<JoueurItf>();
		
		for (JoueurItf j1 : sl1) {
			for(JoueurItf j2 : sl2) {
				if (j1 == j2) {
					res.add(j1);
				}
			}
		}
		return res;
	}
	
	public JoueurItf choixJoueur (JoueurItf joueur, LinkedList<JoueurItf> res) {
		int max = 0;
		JoueurItf ret = null;
		int min = Integer.MAX_VALUE;
		LinkedList<JoueurItf> tmp = new LinkedList<JoueurItf>();
		
		for (JoueurItf j : res) {
			if (j.getDuration() > max) {
				max = j.getDuration();
			}	
		}
		for (JoueurItf j : res) {
			if (j.getDuration() == max) {
				tmp.add(j);
			}
		}
		if (tmp.size() == 1) {
			return tmp.getFirst();
		}
		else {
			for (JoueurItf j : tmp) {
				if (j.getSummonerElo() < min) {
					min = j.getSummonerElo();
					ret = j;
				}
			}
			return ret;				
		}
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, JoueurItf j2) {
		DataOutputStream br1;
		DataOutputStream br2;

		
		try {
			br1 = new DataOutputStream(j1.getSocket().getOutputStream());
			br2 = new DataOutputStream(j2.getSocket().getOutputStream());
			
			/* on envoi au joueur le temps qu'il a attendu dans la file du serveur
			 * suivi du summonerElo, de la latence et de la durée dans la file d'attente
			 * de son adversaire 
			 */
			br1.writeBytes("infoJoueur " + j1.getDuration() + " " + j2.getSummonerElo()
					+ " " + j2.getLatency() + " " + j2.getDuration());
			br2.writeBytes("InfoJoueur "  + j2.getDuration() + " " + j1.getSummonerElo()
					+ " " + j1.getLatency() + " " + j1.getDuration());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public class TacheServeur extends TimerTask {
		boolean broad_matchmaking = false;
		
		@Override
		public void run() {
			//System.out.println("tache dans run liste de taille " + l3.size());
			synchronized (l3) {
				for (int i = 0; i < l3.size(); i++) {
					JoueurItf j = l3.get(i);
					j.setDuration(j.getDuration() + 1);
					if (j.getDuration() >= 5) {
						broad_matchmaking = true;
					}
				}
			}
			if (broad_matchmaking) {
				broad_matchmaking();
				System.out.println("appel à broad_matchmaking");
				broad_matchmaking = false;
			}
			
		}
		
		public void broad_matchmaking () {
			int i = 0;
			int min;
			JoueurItf ret = null;
			JoueurItf joueur;
			LinkedList<JoueurItf> toMatch = new LinkedList<JoueurItf>();
			
			while (toMatch.size() <= 1) {
				synchronized (l3) {
					for (JoueurItf j : l3) {
						if (j.getDuration() >= (5 - i)) {
							toMatch.add(j);
						}
					}
				}
				i++;
			}
			System.out.println("taille de toMatch = " + toMatch.size());
			while (toMatch.size() > 1) {
				min = Integer.MAX_VALUE;
				joueur = toMatch.getFirst();
				for (JoueurItf j : toMatch) {
					if (j != joueur) {
						if (Math.abs(j.getSummonerElo() - joueur.getSummonerElo()) < min) {
							min = Math.abs(j.getSummonerElo() - joueur.getSummonerElo());
							ret = j;
						}	
					}
				}
				EnvoiInfoJoueur(ret, joueur);
				/*joueur.InfoJoueur(ret);
				ret.InfoJoueur(joueur);*/
				toMatch.remove(joueur);
				toMatch.remove(ret);
				synchronized (l1) {
					l1.remove(joueur);
					l1.remove(ret);
				}
				synchronized (l2) {
					l2.remove(joueur);
					l2.remove(ret);
				}
				synchronized (l3) {
					l3.remove(joueur);
					l3.remove(ret);
				}
			}			
			if (!toMatch.isEmpty()) {
				if (!l1.isEmpty()) {
					// il reste normallement uniquement un joueur
					joueur = toMatch.getFirst();
					min = Integer.MAX_VALUE;
					synchronized (l1) {
						
						for (JoueurItf j: l1) {
							if (j != joueur) {
								if (Math.abs(j.getSummonerElo() - joueur.getSummonerElo()) < min) {
									min = Math.abs(j.getSummonerElo() - joueur.getSummonerElo());
									ret = j;
								}
								EnvoiInfoJoueur(ret, joueur);
								/*joueur.InfoJoueur(ret);
								ret.InfoJoueur(joueur);	*/				
							}
						}
						l1.remove(joueur);
						l1.remove(ret);
					}
					toMatch.remove(joueur);
					toMatch.remove(ret);
					synchronized (l2) {
						l2.remove(joueur);
						l2.remove(ret);
					}
					synchronized (l3) {
						l3.remove(joueur);
						l3.remove(ret);
					}
				}
			}
		}
	}
}
