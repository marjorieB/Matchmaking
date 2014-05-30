package centralise_1V1_naif;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Joueur {
	public static void main (String [] arg) {
		FileReader fr;
		BufferedReader br;
		String lu;
		String proprietes[];
		int summonerElo;
		int latency;
		JoueurItf j; 
		
		try {
			// récupération des propriétés des joueurs à partir du ficheir joueur_proprietes.csv			
			fr = new FileReader("../../M1_SAR/stage_M1/joueurs_proprietes.csv");
			br = new BufferedReader(fr);
			int i = 0;
			while ((lu = br.readLine()) != null && i < 200) {
				proprietes = lu.split(",");
				summonerElo = Integer.parseInt(proprietes[1]);
				latency = (int)Double.parseDouble(proprietes[2]);
				// création des joueurs et lancement des joueurs
				j = new JoueurImpl(summonerElo, latency);
				ThreadJoueur tj = new ThreadJoueur(j);
				tj.start();
				i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
}
