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
		while(true) {
			
		}
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, JoueurItf j2) {
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
				String requete = "INSERT INTO Joueurs (summonerId, duration, summonerIdRef, geom)";
				ResultSet res;
				
				// update de la colonne Duration de l'ensemble des joueurs de la base
				st.executeUpdate("UPDATE Joueurs SET duration = duration + 1");
				// insertion de l'ensemble des joueurs qui se sont connectés.
				synchronized(joueurs) {
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
							requete += "select " + joueurs.get(i).getSummonerId() + ", 1, -1, " + "ST_GeomFromText('POINT(" + joueurs.get(i).getSummonerElo() 
									+ " " + joueurs.get(i).getLatency() + ")', 4326) UNION ALL ";
						}
						else {
							requete += "select " + joueurs.get(i).getSummonerId() + ", 1, -1, " + "ST_GeomFromText('POINT(" + joueurs.get(i).getSummonerElo() 
									+ " " + joueurs.get(i).getLatency() + ")', 4326);";
						}
						map.put(joueurs.get(i).getSummonerId(), joueurs.get(i));
					}
					System.out.println("insertion de " + joueurs.size());

					joueurs.removeAll(joueurs);
					st.executeUpdate(requete);
					matchmaking();
				}
				
				// test pour savoir si des joueurs ont attendus dans la file d'attente depuis trop longtemps
				// traitement approprié en conséquence
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		public void matchmaking() {
			Statement st;
			ResultSet res;
			try {
				st = conn.createStatement();
				res = st.executeQuery("select summonerId, summonerIdRef from Joueurs");
				while (res.next()) {
					System.out.println("id: " + res.getInt(1) + " id ref: "+ res.getInt(2));
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
}
