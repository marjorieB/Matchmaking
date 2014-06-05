package centralise_1V1_BD_Spatiale;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class ThreadServer extends Thread {
	private LinkedList<JoueurItf> joueurs;
	private HashMap<Integer, JoueurItf> map;
	private HashMap<Integer, Integer> tmp;
	private Timer timer;
	private Connection conn;
	
	public ThreadServer(LinkedList<JoueurItf> liste, Connection conn) {
		joueurs = liste;
		map = new HashMap<Integer, JoueurItf>();
		tmp = new HashMap<Integer, Integer>();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TacheServeur(), 0, 5000);
		this.conn = conn;
	}
	
	public void run () {
		while(true) {
			
		}
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, int duration1, JoueurItf j2, int duration2) {
		DataOutputStream br1;
		DataOutputStream br2;

		
		try {
			System.out.println("j'envoie a " + j1.getSummonerId());
			br1 = new DataOutputStream(j1.getSocket().getOutputStream());
			System.out.println("j'envoi a " + j2.getSummonerId());
			br2 = new DataOutputStream(j2.getSocket().getOutputStream());
			
			/* on envoi au joueur le temps qu'il a attendu dans la file du serveur
			 * suivi du summonerElo, de la latence et de la durée dans la file d'attente
			 * de son adversaire 
			 */
			br1.writeBytes("infoJoueur " + duration1 + " " + j2.getSummonerId() + " " + j2.getSummonerElo()
					+ " " + j2.getLatency() + " " + duration2 + "\n");
			br2.writeBytes("InfoJoueur "  + duration2 + " " + j1.getSummonerId() + " " + j1.getSummonerElo()
					+ " " + j1.getLatency() + " " + duration1 + "\n");
			j1.getSocket().close();
			j2.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public class TacheServeur extends TimerTask {		
		@Override
		public void run() {
			boolean broad_matchmaking = false;
			try {
				Statement st = conn.createStatement();
				String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";
				
				// update de la colonne Duration de l'ensemble des joueurs de la base
				st.executeUpdate("UPDATE Joueurs SET duration = duration + 1");
				// insertion de l'ensemble des joueurs qui se sont connectés.
				
				synchronized(joueurs) {
					synchronized (map) {
						while (joueurs.isEmpty()) {
							try {
								joueurs.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						for (int i = 0; i < joueurs.size(); i++) {
							if (i != joueurs.size() - 1) {
								requete += "select " + joueurs.get(i).getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs.get(i).getSummonerElo() 
										+ " " + joueurs.get(i).getLatency() + ")', 4326) UNION ALL ";
							}
							else {
								requete += "select " + joueurs.get(i).getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs.get(i).getSummonerElo() 
										+ " " + joueurs.get(i).getLatency() + ")', 4326);";
							}
							System.out.println(joueurs.get(i).getSummonerId() + " vient d'être inséré dans la map");
							map.put(new Integer(joueurs.get(i).getSummonerId()), joueurs.get(i));
						}
						System.out.println("insertion de " + joueurs.size());

					}	

					joueurs.removeAll(joueurs);
				}
				st.executeUpdate(requete);
				matchmaking();	
				
				// test pour savoir si des joueurs ont attendus dans la file d'attente depuis trop longtemps
				// traitement approprié en conséquence
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		public void matchmaking() {
			Statement st;
			ResultSet res = null;
			boolean flag = false;
			try {
				st = conn.createStatement();
				res = st.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 " + 
						"WHERE id1<>id2 AND ST_Distance(J1.geom, J2.geom) < 20 GROUP BY id1");
				while (res.next()) {
					System.out.println("id: " + res.getInt(1) + " id ref: "+ res.getInt(2));
					if ((tmp.get(new Integer(res.getInt(1))) == null) && (tmp.get(new Integer(res.getInt(2))) == null)) {
						tmp.put(new Integer(res.getInt(1)), new Integer(res.getInt(1)));
						tmp.put(new Integer(res.getInt(2)), new Integer(res.getInt(2)));
						EnvoiInfoJoueur(map.get(new Integer(res.getInt(1))), res.getInt(3), map.get(new Integer(res.getInt(2))), res.getInt(4));
						flag = true;
					}
				}
				res.close();
				res = null;
				if (flag) {
					delete();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		public void delete() {
			Statement st; 
			int j = 0;
			String requete = "DELETE FROM Joueurs WHERE";
			
			synchronized (map) {
				for (Integer i: tmp.keySet()) {
					j++;
					map.remove(i);
					if (j == tmp.size()) {
						requete += " summonerId = " + tmp.get(i).intValue();
					}
					else {
						requete += " summonerId = " + tmp.get(i).intValue() + " AND";
					}
				}
				System.out.println(requete);
				tmp.clear();
			}
			try {
				st = conn.createStatement();
				st.executeUpdate(requete);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
}
