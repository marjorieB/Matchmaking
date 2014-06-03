package centralise_1V1_BD_Spatiale;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
	private Timer timer;
	private Connection conn;
	
	public ThreadServer(LinkedList<JoueurItf> liste, Connection conn) {
		joueurs = liste;
		map = new HashMap<Integer, JoueurItf>();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TacheServeur(), 0, 3000);
		this.conn = conn;
	}
	
	public void run () {
		while (true) {
			synchronized(joueurs) {
				while (joueurs.isEmpty()) {
					try {
						joueurs.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				map.put(new Integer(joueurs.getFirst().getSummonerId()), joueurs.getFirst());
				matchmaking(joueurs.getFirst());
				joueurs.removeFirst();				
			}
		}
	}
	
	public void matchmaking (JoueurItf joueur) {
		ResultSet res;
		boolean trouve = false;
		int summonerId = 0;
		try {
			Statement st = conn.createStatement();
			
			// récupération dans un resultSet des points les plus proches
			res = st.executeQuery("select summonerId, x(geom), y(geom) from Joueurs where ST_Distance(geom, MakePoint(" +
					joueur.getSummonerElo() + ", " + joueur.getLatency() + ", 4326)) ORDER BY ST_Distance(geom, " + 
					"MakePoint(" + joueur.getSummonerElo() + ", " + joueur.getLatency() + ", 4326)) LIMIT 1");
			
			if (res.next()) {
				if ((res.getInt(2) > (joueur.getSummonerElo() - 20)) && (res.getInt(2) < (joueur.getSummonerElo() + 20)) 
						&& (res.getInt(3) > (joueur.getLatency() - 20)) && (res.getInt(3) < (joueur.getLatency() + 20))) {
					trouve = true;		
					summonerId = res.getInt(1);
				}
			}
			if (trouve) {
				// suppression du joueur de la base qui match avec le joueur passé en paramètre
				st.executeUpdate("DELETE FROM Joueurs where summonerId = " + summonerId);
				EnvoiInfoJoueur(joueur, map.get(new Integer(summonerId)));
			}
			else {
				// insertion du joueur dans la base
				st.executeUpdate("INSERT INTO Joueurs(summonerId, duration, geom) VALUES (" + joueur.getSummonerId() + 
						", " + joueur.getDuration() +  " , ST_GeomFromText('POINT(" + joueur.getSummonerElo() + " " + joueur.getLatency() + 
						" " + ")', 4326))");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, JoueurItf j2) {
		DataOutputStream br1;
		DataOutputStream br2;

		
		try {
			br1 = new DataOutputStream(j1.getSocket().getOutputStream());
			br2 = new DataOutputStream(j2.getSocket().getOutputStream());
			
			/* on envoi au joueur le temps qu'il a attendu dans la file du serveur
			 * suivi du summonerElo, de la latence et de la durée dans la file d'attente
			 * de son adversaire 
			 */
			br1.writeBytes("infoJoueur " + j1.getDuration() + " " + j2.getSummonerElo()
					+ " " + j2.getLatency() + " " + j2.getDuration() + "\n");
			br2.writeBytes("InfoJoueur "  + j2.getDuration() + " " + j1.getSummonerElo()
					+ " " + j1.getLatency() + " " + j1.getDuration() + "\n");
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
				ResultSet res;
	
				synchronized(joueurs) {
					for (int i = 0; i < joueurs.size(); i++) {
						JoueurItf j = joueurs.get(i);
						j.setDuration(j.getDuration() + 1);
					}
				}
				//incrémenter le temps de l'ensemble des joueurs de la base
				st.executeUpdate("UPDATE Joueurs SET duration = duration + 1");
				res = st.executeQuery("select 1 from Joueurs where duration > 5 LIMIT 1");
				if (res.next()) {
					broad_matchmaking = true;
				}
							
				
				if (broad_matchmaking) {
					broad_matchmaking = false;
					broad_matchmaking();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void broad_matchmaking () {
			ResultSet res;
			ResultSet res1;
			ResultSet res2;
			boolean continu = true;
			boolean trouve = false;
			boolean reste_joueur = false;
			int summonerId1 = 0;
			int summonerId2 = 0;
			int summonerElo;
			int min;
			Statement st;
			Statement st1;
			try {
				st = conn.createStatement();
				st1 = conn.createStatement();
			
				while (continu) {
					res = st.executeQuery("select summonerId, x(geom), y(geom) from Joueurs where duration > " + 5);
					res1 = res;
					if (res.next()) {
						min = Integer.MAX_VALUE;
						summonerId1 = res.getInt(1);
						summonerElo = res.getInt(2);
						while(res1.next()) {
							reste_joueur = true;
							if (summonerId1 != res1.getInt(1)) {
								if (Math.abs(summonerElo - res1.getInt(2)) < min) {
									trouve = true;
									min = Math.abs(summonerElo - res1.getInt(2));
									summonerId2 = res1.getInt(1);
								}
							}
						}
						if (trouve) {
							st.executeUpdate("DELETE FROM Joueurs where summonerId = " + summonerId1);
							st.executeUpdate("DELETE FROM Joueurs where summonerId = " + summonerId2);
							EnvoiInfoJoueur(map.get(new Integer(summonerId2)), map.get(new Integer(summonerId1)));
							reste_joueur = false;
						}
						else {
							continu = false;
						}
					}
					else {
						continu = false;
					}
				}
				if (reste_joueur) {
					res = st.executeQuery("select summonerId, x(geom), y(geom) from Joueurs where duration > " + 5);
					res.next();
					// il reste un tuple à matcher, le nombre de tuples dans le ResultSet n'était pas pair.
					res2 = st1.executeQuery("select summonerId from Joueurs where ST_Distance(GEOMETRY MakePoint(" +
							res.getInt(2) + ", " + res.getInt(3) + ")) ORDER BY ST_Distance(GEOMETRY, " + 
							"MakePoint(" + res.getInt(2) + ", " + res.getInt(3) + "LIMIT 1");
					if (res2.next()) {
						st.executeUpdate("DELETE FROM Joueurs where summonerId = " + res.getInt(1));
						st.executeUpdate("DELETE FROM Joueurs where summonerId = " + res2.getInt(1));
						EnvoiInfoJoueur(map.get(new Integer(res.getInt(1))), map.get(new Integer(res.getInt(1))));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
	}
}
