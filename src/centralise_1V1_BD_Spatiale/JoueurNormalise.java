package centralise_1V1_BD_Spatiale;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class JoueurNormalise {
	public static void main(String[] arg) {
		ArrayList<Float> elos = new ArrayList<Float>();
		ArrayList<Float> latencies = new ArrayList<Float>();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		String totalData = "";
		FileReader fr = null;
		BufferedReader br;
		String lu;
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(3000);
		JoueurItf j;

		// récupération des propriétés des joueurs à partir du fichier
		// joueur_proprietes.csv
		try {
			fr = new FileReader(
					"../joueurs_proprietes_1V1_acctId=userId.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br = new BufferedReader(fr);

		// modif pour la normalisation
		new RepartitionPingsLatence(br, ids, elos, latencies);
		// br = rpl.returnMeTheValues();
		// fin de la modif

		while (!ids.isEmpty()) {
			j = new JoueurImpl(ids.get(0).intValue(), elos.get(0).intValue(),
					latencies.get(0).intValue());
			ids.remove(0);
			elos.remove(0);
			latencies.remove(0);
			ThreadJoueur tj = new ThreadJoueur(j);
			scheduler.execute(tj);
		}
	}
}