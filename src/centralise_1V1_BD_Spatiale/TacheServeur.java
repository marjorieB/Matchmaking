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


public class TacheServeur {
	private LinkedList<JoueurItf> joueurs;
	private HashMap<Integer, JoueurItf> map;
	private HashMap<Integer, Integer> tmp;
	private Timer timer;
	private Statistiques stats;
	private Connection conn;
	private int nb_connexions = 0;
	private int nb_matches = 0;
	
	public TacheServeur(LinkedList<JoueurItf> liste, Connection conn) {
		joueurs = liste;
		map = new HashMap<Integer, JoueurItf>();
		tmp = new HashMap<Integer, Integer>();
		timer = new Timer();
		stats = new Statistiques();
		timer.scheduleAtFixedRate(new TacheInterneServeur(), 0, 3000);
		this.conn = conn;
	}
	
	public void EnvoiInfoJoueur(JoueurItf j1, int duration1, JoueurItf j2, int duration2) {
		DataOutputStream br1;
		DataOutputStream br2;
		
		//System.out.println("j1 = " + j1.getSummonerId() + " j2 = " + j2.getSummonerId());
				
		try {
			//System.out.println("j'envoie a " + j1.getSummonerId());
			br1 = new DataOutputStream(j1.getSocket().getOutputStream());
			//System.out.println("j'envoi a " + j2.getSummonerId());
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
			
			nb_matches += 2;
			if (nb_matches == nb_connexions) {
				stats.afficher_stats();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public class TacheInterneServeur extends TimerTask {		
		@Override
		public void run() {
		ResultSet res;
			int taille = 0;
			int cpt = 0;
			
			try {
				Statement st = conn.createStatement();
				//String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";

				
				// update de la colonne Duration de l'ensemble des joueurs de la base
				st.executeUpdate("UPDATE Joueurs SET duration = duration + 1");
				
				// insertion de l'ensemble des joueurs qui se sont connectés.
				synchronized(joueurs) {
					synchronized (map) {						
						while (joueurs.isEmpty() && map.isEmpty()) {
							try {
								joueurs.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						nb_connexions += joueurs.size();
						taille = joueurs.size();
						while (!joueurs.isEmpty()) {
							String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";
							for (int i = 0; i < 400; i++) {								
								if ((i != 399) && (cpt != taille - 1)) {
									requete += "select " + joueurs.getFirst().getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs.getFirst().getSummonerElo() 
											+ " " + joueurs.getFirst().getLatency() + ")', 4326) UNION ALL ";
									map.put(new Integer(joueurs.getFirst().getSummonerId()), joueurs.getFirst());
									joueurs.removeFirst();
									cpt++;
								}
								else {
									requete += "select " + joueurs.getFirst().getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs.getFirst().getSummonerElo() 
											+ " " + joueurs.getFirst().getLatency() + ")', 4326);";
									map.put(new Integer(joueurs.getFirst().getSummonerId()), joueurs.getFirst());
									joueurs.removeFirst();
									cpt++;
									break;
								}
							}
							st.executeUpdate(requete);
						}
						cpt = 0;
					}
				}
				matchmaking();
							
							
					/*		for (int j = 0; j < (int)((joueurs.size() / 300) + 1); j++) {
								int i = 0;
								System.out.println("joueurs.size() "+ joueurs.size() + " valeur de j = " + j + " valeur max de j = " + (int)((joueurs.size() / 500) + 1));
								String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";

								while (true) {
									System.out.println("cpt = " + cpt + " joueurs size = " + joueurs.size() + " i = " + i);
									if (cpt == joueurs.size() - 1 || i == 299) {
										System.out.println("on est la");
										requete += "select " + joueurs.get(cpt).getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs.get(cpt).getSummonerElo() 
												+ " " + joueurs.get(cpt).getLatency() + ")', 4326)";
										map.put(new Integer(joueurs.get(cpt).getSummonerId()), joueurs.get(cpt));
										i++;
										cpt += 1;
										break;
										
									}
									else {
										requete += "select " + joueurs.get(cpt).getSummonerId() + ", 1, " + "ST_GeomFromText('POINT(" + joueurs.get(cpt).getSummonerElo() 
												+ " " + joueurs.get(cpt).getLatency() + ")', 4326) UNION ALL ";
										map.put(new Integer(joueurs.get(cpt).getSummonerId()), joueurs.get(cpt));
										i++;
										cpt += 1;
									}									
								}
								System.out.println("après break");
								st.executeUpdate(requete);
								System.out.println("la requete a été exécutée");
							}
						}
						System.out.println("je suis maintenant la");
						joueurs.removeAll(joueurs);
						cpt = 0;
					}	
				}
				
				matchmaking();*/
						
				
				
				// test pour savoir si des joueurs ont attendus dans la file d'attente depuis trop longtemps
				// traitement approprié en conséquence
				res = st.executeQuery("SELECT * FROM Joueurs WHERE duration >= 5");
				if (res.next()) {
					broad_matchmaking();
				}
				
			} catch (SQLException e) {
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
						"WHERE id1<>id2 AND ST_Distance(J1.geom, J2.geom) < 20 group by id1"); //group by id1
				while (res.next()) {
					//System.out.println("id: " + res.getInt(1) + " id ref: "+ res.getInt(2));
					if ((tmp.get(new Integer(res.getInt(1))) == null) && (tmp.get(new Integer(res.getInt(2))) == null)) {
						tmp.put(new Integer(res.getInt(1)), new Integer(res.getInt(1)));
						tmp.put(new Integer(res.getInt(2)), new Integer(res.getInt(2)));
						EnvoiInfoJoueur(map.get(new Integer(res.getInt(1))), res.getInt(3), map.get(new Integer(res.getInt(2))), res.getInt(4));
						flag = true;
					}
				}
				res.close();
				res = null;
				st.close();
				if (flag) {
					delete();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		public void broad_matchmaking() {
			Statement st;
			ResultSet res;
			boolean flag = false;
			
			
			try {
				st = conn.createStatement();
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
				st.close();
				if (flag) {
					delete();
				}
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
					for (int i = 0; i < 1000; i++) {
						
						if ((i != 999) && (cpt != taille - 1)) {
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