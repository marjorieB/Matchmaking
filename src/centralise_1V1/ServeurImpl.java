package centralise_1V1;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.ListIterator;

public class ServeurImpl implements ServeurItf {

	// structure contenant les joueurs
	// implémentation naïve avec une liste pour chaque critère
	private LinkedList<JoueurItf> l1; // contient les joueurs classés par summonerElo
	private LinkedList<JoueurItf> l2; // contient les joueurs classés par latence
	private LinkedList<JoueurItf> l3; // contient les joueurs classées par durée d'attente dans le cache du serveur
	private Tache tache; // tache qui se réveillera régulièrement et qui augmentera le temps d'attente des clients dans la file du serveur
						 // cette tache est également chargée de matcher les clients qui attendent depuis longtemps.
	
	public ServeurImpl() throws RemoteException {
		l1 = new LinkedList<JoueurItf>();
		l2 = new LinkedList<JoueurItf>();
		l3 = new LinkedList<JoueurItf>();
		System.out.println("création de la tâche");
		tache = new Tache(l1, l2, l3);
	}
	
	@Override
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
		
		try {
			joueur.setDuration(0);
			if (l1.isEmpty()) {
				l1.add(joueur);
				l2.add(joueur);
				l3.add(joueur);
			}
			else {
				while(it1.hasNext()) {
					tmp = it1.next();
					if (tmp.getSummonerElo() < joueur.getSummonerElo()) {
						i++;
					}
					// on récupère les joueurs de summonerElo étant égale à plus ou moins 20 unités du summonerElo du joueur passé en paramètre
					if((tmp.getSummonerElo() > (joueur.getSummonerElo() - 20)) || 
							(tmp.getSummonerElo() < (joueur.getSummonerElo() + 20))) {
						sl1.add(tmp);
					}
				}
				while (it2.hasNext()) {
					tmp = it2.next();
					if (tmp.getLatency() < joueur.getLatency()) {
						j++;
					}
					if ((tmp.getLatency() > joueur.getLatency() - 20) || 
							(tmp.getLatency() < joueur.getLatency() + 20)) {
						sl2.add(tmp);
					}					
				}
				while (it3.hasNext() && (it3.next().getDuration() < joueur.getDuration())) {
					k++;
				}
				res = intersection(sl1, sl2);
				if (res.isEmpty()) {
					l1.add(i, joueur);
					l2.add(j, joueur);
					l3.add(k, joueur);
				}
				else {
					while((tmp = choixJoueur(joueur, res)) == null) {
						System.out.println("tmp est nul");
						l1.remove(tmp);
						l2.remove(tmp);
						l3.remove(tmp);
					}	
					joueur.InfoJoueur(tmp);
					tmp.InfoJoueur(joueur);
					l1.remove(tmp);
					l2.remove(tmp);
					l3.remove(tmp);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
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
			try {
				if (j.getDuration() > max) {
					max = j.getDuration();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (JoueurItf j : res) {
			try {
				if (j.getDuration() == max) {
					tmp.add(j);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (tmp.size() == 1) {
			return tmp.getFirst();
		}
		else {
			for (JoueurItf j : tmp) {
				try {
					if (j.getSummonerElo() < min) {
						min = j.getSummonerElo();
						ret = j;
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return ret;				
		}
	}
}
