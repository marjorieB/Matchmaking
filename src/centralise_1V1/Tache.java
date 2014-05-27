package centralise_1V1;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Tache {

	private Timer timer;
	private LinkedList<JoueurItf> l1; //contient les joueurs classés par summonerElo
	private LinkedList<JoueurItf> l2; // contient les joueurs classés par latence
	private LinkedList<JoueurItf> l3; // contient les joueurs classées par durée d'attente dans le cache du serveur
	
	public Tache (LinkedList<JoueurItf> l1, LinkedList<JoueurItf> l2, LinkedList<JoueurItf> l3) {
		this.l1 = l1;
		this.l2 = l2;
		this.l3 = l3;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TacheServeur(), 0, 5000);
	}
	
	public class TacheServeur extends TimerTask {
		boolean broad_matchmaking = false;
		
		@Override
		public synchronized void run() {
			System.out.println("tache dans run liste de taille " + l3.size());
			for (int i = 0; i < l3.size(); i++) {
				JoueurItf j = l3.get(i);
				try {
					j.setDuration(j.getDuration() + 1);
					if (j.getDuration() > 5) {
						broad_matchmaking = true;
					}
				} catch (RemoteException e) {
					// le joueur n'a pas pu être contacté
					l3.remove(j);
				}
			}
			if (broad_matchmaking) {
				broad_matchmaking();
				System.out.println("appel à broad_matchmaking");
				broad_matchmaking = false;
			}
			
		}
		
		public synchronized void broad_matchmaking () {
			int i = 0;
			int min;
			JoueurItf ret = null;
			JoueurItf joueur;
			LinkedList<JoueurItf> toMatch = new LinkedList<JoueurItf>();
			
			while (toMatch.size() <= 1) {
			
				for (JoueurItf j : l3) {
					try {
						if (j.getDuration() > (5 - i)) {
							toMatch.add(j);
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				i++;
			}
			while (toMatch.size() > 1) {
				min = Integer.MAX_VALUE;
				joueur = toMatch.getFirst();
				try {
					for (JoueurItf j : toMatch) {
						if (j != joueur) {
							if (Math.abs(j.getSummonerElo() - joueur.getSummonerElo()) < min) {
								min = Math.abs(j.getSummonerElo() - joueur.getSummonerElo());
								ret = j;
							}	
						}
					}
					joueur.InfoJoueur(ret);
					ret.InfoJoueur(joueur);
					toMatch.remove(joueur);
					toMatch.remove(ret);
					l1.remove(joueur);
					l1.remove(ret);
					l2.remove(joueur);
					l2.remove(ret);
					l3.remove(joueur);
					l3.remove(ret);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!toMatch.isEmpty()) {
				// il reste normallement uniquement un joueur
				joueur = toMatch.getFirst();
				min = Integer.MAX_VALUE;
				for (JoueurItf j: l1) {
					if (j != joueur) {
						try {
							if (Math.abs(j.getSummonerElo() - joueur.getSummonerElo()) < min) {
								min = Math.abs(j.getSummonerElo() - joueur.getSummonerElo());
								ret = j;
							}
							joueur.InfoJoueur(ret);
							ret.InfoJoueur(joueur);
							toMatch.remove(joueur);
							toMatch.remove(ret);
							l1.remove(joueur);
							l1.remove(ret);
							l2.remove(joueur);
							l2.remove(ret);
							l3.remove(joueur);
							l3.remove(ret);							
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
