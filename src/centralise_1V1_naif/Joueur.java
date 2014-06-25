package centralise_1V1_naif;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Joueur {
	public static void main(String[] arg) {
		FileReader fr;
		BufferedReader br;
		String lu;
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(800);
		String proprietes[];
		int summonerId;
		int summonerElo;
		int latency;
		JoueurItf j;

		try {

			// récupération des propriétés des joueurs à partir du ficheir
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
				scheduler.execute(tj);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}