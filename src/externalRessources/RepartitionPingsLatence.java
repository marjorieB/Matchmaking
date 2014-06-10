package externalRessources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

public class RepartitionPingsLatence {
	ArrayList<Float> elos;
	ArrayList<Float> latencies;
	ArrayList<Integer> ids;
	String totalData="";
	
	public RepartitionPingsLatence(BufferedReader br) throws NumberFormatException, IOException {
		//first let's loop on adding the stats
		String lu;
		String proprietes[];
		int summonerId;
		int summonerElo;
		int latency;
		
		
		while ((lu = br.readLine()) != null) {
			proprietes = lu.split(",");
			summonerId = Integer.parseInt(proprietes[0]);
			summonerElo = Integer.parseInt(proprietes[1]);
			latency = (int)Double.parseDouble(proprietes[2]); // 11 permet de considèrer la latence au même titre que le elo lorsque l'on considérera la distance
			ids.add(summonerElo);
			elos.add((float) summonerElo);
			latencies.add((float) latency);
		}
		
		// now get the max of both Elo & Latency
		Float maxElo=Collections.max(elos);
		Float maxLat=Collections.max(latencies);
		//now let's divide all by this max value
		for (Float f : elos) elos.set(elos.indexOf(f), f/maxElo);
		for (Float f : latencies) latencies.set(latencies.indexOf(f), f/maxLat);
		//now let's watch how it looks
		//System.out.println("went here");
		//we are now ready
	}

	
	public BufferedReader returnMeTheValues() {
		if (totalData.length()<10) {
			for (int i=0;i<ids.size();i++) {
				int id=ids.get(i);
				float elo=elos.get(i);
				float latence=latencies.get(i);
				totalData+=id+","+elo+","+latence+"\n";
			}
			//System.out.println("I Was here");
			return new BufferedReader(new StringReader(totalData));
		}
		else {
			return new BufferedReader(new StringReader(totalData));
		}
	}
}
