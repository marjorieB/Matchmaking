package centralise_1V1_BD_Spatiale;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import centralise_1V1_utilitaire.*;

public class TacheServeur extends Thread {
	private LinkedList<JoueurItf> joueurs;
	private LinkedList<JoueurItf> joueurs_copie;
	private ArrayList<ResultSet> res = new ArrayList<ResultSet>();
	private HashMap<Integer, JoueurItf> map;
	private HashMap<Integer, Integer> tmp;
	private Timer timer;
	private TacheTraitement tt;
	private long tempsDeb = 0;
	private TacheInterneServeur tis;
	private Stats stats;
	private Connection conn;
	private int nb_connexions_tot = 0;
	//private NbConnexions nb_connexions;
	private int nb_matchs = 0;
	
	public TacheServeur(LinkedList<JoueurItf> liste, Connection conn, String arg) {
		joueurs = liste;
		joueurs_copie = new LinkedList<JoueurItf>();
		this.conn = conn;
		tt = new TacheTraitement(conn, res);
		tt.start();
		map = new HashMap<Integer, JoueurItf>();
		tmp = new HashMap<Integer, Integer>();
		//this.nb_connexions = nb_connexions;
		tis = new TacheInterneServeur();
		timer = new Timer();
		timer.scheduleAtFixedRate(tis, 0, 3000);
		stats = new Stats(arg, "BD_Spatiale");
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, int duration1, JoueurItf j2, int duration2) {
		DataOutputStream br1;
		DataOutputStream br2;
		
		j1.setDuration(duration1);
		j2.setDuration(duration2);
		j1.setTime2(System.currentTimeMillis());
		j2.setTime2(System.currentTimeMillis());
		
		stats.miseAJour(j1, j2);
	
		try {
			br1 = new DataOutputStream(j1.getSocket().getOutputStream());
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
			
			nb_matchs += 2;
			if (nb_matchs == 100000) {
				tis.cancel();
				stats.fin(tempsDeb);
				System.exit(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public class TacheInterneServeur extends TimerTask {	
		boolean first = true;

		@Override
		public void run() {
			//System.out.println("DéBUT TACHE");
			
			int taille = 0;
			int cpt = 0;
			
			try {
				//Statement st = conn.createStatement();
				
				// update de la colonne Duration de l'ensemble des joueurs de la base
				tt.executeUpdate("UPDATE Joueurs SET duration = duration + 1");
				//st.executeUpdate("UPDATE Joueurs SET duration = duration + 1");
				
				
				// insertion de l'ensemble des joueurs qui se sont connectés.
				synchronized (map) {
					synchronized(joueurs) {						
						while (joueurs.isEmpty() && map.isEmpty()) {
							try {
								joueurs.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						joueurs_copie = (LinkedList<JoueurItf>) joueurs.clone();
						joueurs.clear();
					}
					if (first) {
						tempsDeb = System.currentTimeMillis();
						first = false;							
					}
					//nb_connexions_tot += joueurs_copie.size();
				
					taille = joueurs_copie.size();
					while (!joueurs_copie.isEmpty()) {
						String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";
						for (int i = 0; i < 400; i++) {								
							if ((i != 399) && (cpt != taille - 1)) {
								requete += "select " + joueurs_copie.getFirst().getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs_copie.getFirst().getSummonerElo() 
										+ " " + joueurs_copie.getFirst().getLatency() + ")', 4326) UNION ALL ";
								map.put(new Integer(joueurs_copie.getFirst().getSummonerId()), joueurs_copie.getFirst());
								joueurs_copie.removeFirst();
								cpt++;
							}
							else {
								requete += "select " + joueurs_copie.getFirst().getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs_copie.getFirst().getSummonerElo() 
										+ " " + joueurs_copie.getFirst().getLatency() + ")', 4326);";
								map.put(new Integer(joueurs_copie.getFirst().getSummonerId()), joueurs_copie.getFirst());
								joueurs_copie.removeFirst();
								cpt++;
								break;
							}
						}
						tt.executeUpdate(requete);
						//st.executeUpdate(requete);
					}
					cpt = 0;
				}
				
				matchmaking();
				
				// test pour savoir si des joueurs ont attendus dans la file d'attente depuis trop longtemps
				// traitement approprié en conséquence
				//res = st.executeQuery("SELECT * FROM Joueurs WHERE duration >= 5");
				
				synchronized(res) {	
					int indice = tt.executeQuery("SELECT * FROM Joueurs WHERE duration >= 5");
					//System.out.println("indice 1 = " + indice);
					while (res.size() <= indice) {
						//System.out.println("début attente 1 size = " + res.size() + " indice = " + indice);
						res.wait();
						//System.out.println("fin attente 1");
					}
					if (res.get(indice).next()) {
						res.remove(indice);
						broad_matchmaking();
					}
					else {
						res.remove(indice);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//System.out.println("FIN TACHE");
		}
		
		
		public void matchmaking() {
			//Statement st;
			//ResultSet res = null;
			boolean flag = false;
			try {
				//st = conn.createStatement();
				synchronized(res) {	
					int indice = tt.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 " + 
							"WHERE id1<>id2 AND ST_Distance(J1.geom, J2.geom) < 20 group by id1");
					//System.out.println("indice 2 = " + indice);
					while (res.size() <= indice) {
						//System.out.println("début attente 2");
						res.wait();
						//System.out.println("fin attente 2");
					}
					while (res.get(indice).next()) {
						if ((tmp.get(new Integer(res.get(indice).getInt(1))) == null) && (tmp.get(new Integer(res.get(indice).getInt(2))) == null)) {
							tmp.put(new Integer(res.get(indice).getInt(1)), new Integer(res.get(indice).getInt(1)));
							tmp.put(new Integer(res.get(indice).getInt(2)), new Integer(res.get(indice).getInt(2)));
							EnvoiInfoJoueur(map.get(new Integer(res.get(indice).getInt(1))), res.get(indice).getInt(3), map.get(new Integer(res.get(indice).getInt(2))), res.get(indice).getInt(4));
							flag = true;
						}
					}
					res.remove(indice);
				}	
				/*res = st.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 " + 
						"WHERE id1<>id2 AND ST_Distance(J1.geom, J2.geom) < 20 group by id1"); //group by id1
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
				st.close();*/
				if (flag) {
					delete();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		public void broad_matchmaking() {
			//Statement st;
			//ResultSet res;
			boolean flag = false;
						
			try {
				synchronized(res) {		
					int indice = tt.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 " + 
							"WHERE id1<>id2 AND J1.duration >= 5 AND J2.duration >= 5 ORDER BY ST_Distance(J1.geom, J2.geom)");
					//System.out.println("indice 3 = " + indice);
					while (res.size() <= indice) {
						//System.out.println("début attente 3");
						res.wait();
						//System.out.println("fin attente 3");
					}
					while (res.get(indice).next()) {
						if ((tmp.get(new Integer(res.get(indice).getInt(1))) == null) && (tmp.get(new Integer(res.get(indice).getInt(2))) == null)) {
							tmp.put(new Integer(res.get(indice).getInt(1)), new Integer(res.get(indice).getInt(1)));
							tmp.put(new Integer(res.get(indice).getInt(2)), new Integer(res.get(indice).getInt(2)));
							EnvoiInfoJoueur(map.get(new Integer(res.get(indice).getInt(1))), res.get(indice).getInt(3), map.get(new Integer(res.get(indice).getInt(2))), res.get(indice).getInt(4));
							flag = true;
						}
					}
					res.remove(indice);
				}
				
				
				
			/*	st = conn.createStatement();
				res = st.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 " + 
						"WHERE id1<>id2 AND J1.duration >= 5 AND J2.duration >= 5 ORDER BY ST_Distance(J1.geom, J2.geom)");
				while (res.next()) {
					//System.out.println("id: " + res.getInt(1) + " id ref: "+ res.getInt(2) + " duration1 " + res.getInt(3) + " duration2 " + res.getInt(4));
					if ((tmp.get(new Integer(res.getInt(1))) == null) && (tmp.get(new Integer(res.getInt(2))) == null)) {
						tmp.put(new Integer(res.getInt(1)), new Integer(res.getInt(1)));
						tmp.put(new Integer(res.getInt(2)), new Integer(res.getInt(2)));
						EnvoiInfoJoueur(map.get(new Integer(res.getInt(1))), res.getInt(3), map.get(new Integer(res.getInt(2))), res.getInt(4));
						flag = true;
					}
				}
				res.close();
				res = null;
				st.close();*/
				if (flag) {
					delete();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		
				
		public void delete() {
			Statement st; 
			int j = 0;		
			
			if (tmp.size() < 1000) {
				String requete = "DELETE FROM Joueurs WHERE";
				
				synchronized (map) {
					for (Integer i: tmp.keySet()) {
						j++;
						map.remove(i);
						if (j == tmp.size()) {
							requete += " summonerId = " + tmp.get(i).intValue();
						}
						else {
							requete += " summonerId = " + tmp.get(i).intValue() + " OR";
						}
					}
					//System.out.println(requete);
					tmp.clear();
				}
				try {
					st = conn.createStatement();
					//tt.executeUpdate(requete);
					st.executeUpdate(requete);
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
			else {
				deleteSup1000();
			}
		}
		
		public void deleteSup1000() {
			LinkedList<Integer> liste = new LinkedList<Integer>();
			int cpt = 0;
			Statement st; 
			int taille = tmp.size();
		
			for (Integer i: tmp.keySet()) {
				liste.add(tmp.get(i));
			}
			synchronized(map) {
				while (!liste.isEmpty()) {
					String requete = "DELETE FROM Joueurs WHERE";
					for (int i = 0; i < 500; i++) {
						
						if ((i != 499) && (cpt != taille - 1)) {
							requete += " summonerId = " + liste.getFirst().intValue() + " OR";
							map.remove(liste.getFirst());
							liste.removeFirst();
							cpt++;
						}
						else {
							requete += " summonerId = " + liste.getFirst().intValue();
							map.remove(liste.getFirst());
							liste.removeFirst();
							cpt++;
							break;
						}
					}
					try {
						st = conn.createStatement(); 
						tt.executeUpdate(requete);
						st.executeUpdate(requete);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				cpt = 0;
			}
			tmp.clear();
		}	
	}
}
