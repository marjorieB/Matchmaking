package externalRessources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

public class RepartitionPingsLatence {
<<<<<<< HEAD
	ArrayList<Float> elos=new ArrayList<Float>();
	ArrayList<Float> latencies=new ArrayList<Float>();
	ArrayList<Integer> ids=new ArrayList<Integer>();
	String totalData="";
	
	public RepartitionPingsLatence(BufferedReader br, ArrayList<Integer> ids, ArrayList<Float> elos, ArrayList<Float> latencies) {
=======
	ArrayList<Float> elos=new ArrayList<>();
	ArrayList<Float> latencies=new ArrayList<>();
	ArrayList<Integer> ids=new ArrayList<>();
	String totalData="";
	
	public RepartitionPingsLatence(BufferedReader br) throws NumberFormatException, IOException {
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
		//first let's loop on adding the stats
		String lu;
		String proprietes[];
		int summonerId;
		int summonerElo;
		int latency;
		
		
<<<<<<< HEAD
		try {
			while ((lu = br.readLine()) != null) {
				proprietes = lu.split(",");
				summonerId = Integer.parseInt(proprietes[0]);
				summonerElo = Integer.parseInt(proprietes[1]);
				latency = (int)Double.parseDouble(proprietes[2]);
				ids.add(summonerId);
				elos.add((float) summonerElo);
				latencies.add((float) latency);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
=======
		while ((lu = br.readLine()) != null) {
			proprietes = lu.split(",");
			summonerId = Integer.parseInt(proprietes[0]);
			summonerElo = Integer.parseInt(proprietes[1]);
			latency = (int)Double.parseDouble(proprietes[2]); // 11 permet de considèrer la latence au même titre que le elo lorsque l'on considérera la distance
			ids.add(summonerElo);
			elos.add((float) summonerElo);
			latencies.add((float) latency);
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
		}
		
		// now get the max of both Elo & Latency
		Float maxElo=Collections.max(elos);
		Float maxLat=Collections.max(latencies);
		//now let's divide all by this max value
		for (Float f : elos) elos.set(elos.indexOf(f), (f/maxElo)*1000);
		for (Float f : latencies) latencies.set(latencies.indexOf(f), (f/maxLat)*1000);
		//now let's watch how it looks
<<<<<<< HEAD
		System.out.println("went here");
=======
		//System.out.println("went here");
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
		//we are now ready 
	}

	
<<<<<<< HEAD
	/*public BufferedReader returnMeTheValues() {
=======
	public BufferedReader returnMeTheValues() {
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
		if (totalData.length()<10) {
			for (int i=0;i<ids.size();i++) {
				int id=ids.get(i);
				int elo=(int)(elos.get(i).floatValue());
				float latence=latencies.get(i);
				totalData+=id+","+elo+","+latence+"\n";
			}
<<<<<<< HEAD
			System.out.println("I Was here");
=======
			//System.out.println("I Was here");
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
			return new BufferedReader(new StringReader(totalData));
		}
		else {
			return new BufferedReader(new StringReader(totalData));
		}
<<<<<<< HEAD
	}*/
=======
	}
>>>>>>> 8e1b47e247eeaf19bffe02a7f5a2406904b7ec70
}
