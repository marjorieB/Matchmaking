package centralise_1V1_naif;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import externalRessources.RepartitionPingsLatence;
<<<<<<< HEAD
=======

>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70

public class Joueur {
	public static void main(String[] arg) {
		ArrayList<Float> elos=new ArrayList<Float>();
		ArrayList<Float> latencies=new ArrayList<Float>();
		ArrayList<Integer> ids=new ArrayList<Integer>();
		String totalData="";
		FileReader fr = null;
		BufferedReader br;
		String lu;
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(500);
		JoueurItf j;


			// récupération des propriétés des joueurs à partir du ficheir
			// joueur_proprietes.csv
			try {
				fr = new FileReader("../joueurs_proprietes_1V1_summonerId=summonerId.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			br = new BufferedReader(fr);
<<<<<<< HEAD

			// modif pour la normalisation
			new RepartitionPingsLatence(br, ids, elos, latencies);
			//br = rpl.returnMeTheValues();
			// fin de la modif

			while (!ids.isEmpty()) {
				j = new JoueurImpl(ids.get(0).intValue(), elos.get(0).intValue(), latencies.get(0).intValue());
				ids.remove(0);
				elos.remove(0);
				latencies.remove(0);
				ThreadJoueur tj = new ThreadJoueur(j);
				scheduler.execute(tj);
			}
			
		/*	while ((lu = br.readLine()) != null) {
=======
			
			//modif pour la normalisation
			RepartitionPingsLatence rpl=new RepartitionPingsLatence(br);
			br=rpl.returnMeTheValues();
			//fin de la modif
			
			while ((lu = br.readLine()) != null) {
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
				proprietes = lu.split(",");
				summonerId = Integer.parseInt(proprietes[0]);
				summonerElo = Integer.parseInt(proprietes[1]);
				latency = (int) Double.parseDouble(proprietes[2]);
				// création des joueurs et lancement des joueurs
				j = new JoueurImpl(summonerId, summonerElo, latency);
				ThreadJoueur tj = new ThreadJoueur(j);
				scheduler.execute(tj);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
