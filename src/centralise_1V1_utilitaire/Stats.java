package centralise_1V1_utilitaire;

import java.io.FileWriter;
import java.io.IOException;

public class Stats {
	private FileWriter fw;
	private FileWriter fw1;
	
	public Stats (String arg, String algo) {
		try {
			fw = new FileWriter("statistiques_joueurs_" + algo + arg + ".csv");
			fw1 = new FileWriter ("Statistiques_" + algo + "_temps" + arg + ".csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void miseAJour(JoueurItf j1, JoueurItf j2) {
		try {
			fw.write(j1.getSummonerId() + "," + j1.getSummonerElo() + "," + j1.getLatency() + "," + 
					j1.getDuration() + "," + (j1.getTime2() - j1.getTime1()) + "," +
					j2.getSummonerId() + "," + j2.getSummonerElo() + "," + j2.getLatency() + "," +
					j2.getDuration() + "," + (j2.getTime2() - j2.getTime1()) + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fin(long tempsDeb) {
		long tempsFin = System.currentTimeMillis();
		try {
			fw1.write(tempsFin - tempsDeb + "\n");
			fw1.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
