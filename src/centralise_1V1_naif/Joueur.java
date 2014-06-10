package centralise_1V1_naif;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import externalRessources.RepartitionPingsLatence;


public class Joueur {
	public static void main (String [] arg) {
		FileReader fr;
		BufferedReader br;
		String lu;
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1500);
		String proprietes[];
		int summonerId;
		int summonerElo;
		int latency;
		JoueurItf j;
		
		try {
			
			// récupération des propriétés des joueurs à partir du ficheir joueur_proprietes.csv			
			fr = new FileReader("../joueurs_proprietes_1V1_acctId=userId.csv");
			br = new BufferedReader(fr);
			
			//modif pour la normalisation
			RepartitionPingsLatence rpl=new RepartitionPingsLatence(br);
			br=rpl.returnMeTheValues();
			//fin de la modif
			
			while ((lu = br.readLine()) != null) {
				proprietes = lu.split(",");
				summonerId = Integer.parseInt(proprietes[0]);
				summonerElo = Integer.parseInt(proprietes[1]);
				latency = (int)Double.parseDouble(proprietes[2]);
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
