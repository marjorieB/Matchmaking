package centralise_1V1_utilitaire;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class Joueur {
	public static void main(String[] arg) {
		FileReader fr;
		BufferedReader br;
		String lu;
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(3000);
		String proprietes[];
		int summonerId;
		int summonerElo;
		int latency;
		JoueurItf j;
		ArrayList<ThreadJoueur> tab = new ArrayList<ThreadJoueur>();

		try {

			// récupération des propriétés des joueurs à partir du fichier
			fr = new FileReader("../joueurs_props_newDB.csv");
			br = new BufferedReader(fr);
			int i = 0;
			while ((lu = br.readLine()) != null && i < 100000) {
				i++;
				proprietes = lu.split(",");
				summonerId = Integer.parseInt(proprietes[0]);
				summonerElo = Integer.parseInt(proprietes[1]);
				latency = (int) Double.parseDouble(proprietes[2]);
				// création des joueurs et lancement des joueurs
				j = new JoueurImpl(summonerId, summonerElo, latency);
				ThreadJoueur tj = new ThreadJoueur(j);
				tab.add(tj);
				scheduler.execute(tj);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
