package centralise_5V5_utilitaire;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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
	
	public void miseAJour(HashMap<Integer, JoueurItf> team1, HashMap<Integer, JoueurItf> team2) {
		int i = 0;
		
		try {
			for (Integer id : team1.keySet()) {
				fw.write(team1.get(id).getSummonerId()+ "," + team1.get(id).getSummonerElo()+ "," + 
						team1.get(id).getLatency()+ "," + team1.get(id).getDuration()+ "," +
						(team1.get(id).getTime2() - team1.get(id).getTime1()) + ",");
			}
			for (Integer id : team2.keySet()) {
				i++;
				if (i != 5) {
					fw.write(team2.get(id).getSummonerId()+ "," + team2.get(id).getSummonerElo()+ "," + 
							team2.get(id).getLatency()+ "," + team2.get(id).getDuration()+ "," +
							(team2.get(id).getTime2() - team2.get(id).getTime1()) + ",");
				}
				else {
					fw.write(team2.get(id).getSummonerId()+ "," + team2.get(id).getSummonerElo()+ "," + 
							team2.get(id).getLatency()+ "," + team2.get(id).getDuration()+ "," +
							(team2.get(id).getTime2() - team2.get(id).getTime1()) + "\n");
				}
			}
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
