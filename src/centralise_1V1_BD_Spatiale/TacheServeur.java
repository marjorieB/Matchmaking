package centralise_1V1_BD_Spatiale;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import centralise_1V1_utilitaire.*;

public class TacheServeur extends Thread {
	private LinkedList<JoueurItf> joueurs;
	private HashMap<Integer, JoueurItf> map;
	private HashMap<Integer, Integer> tmp;
	private Timer timer;
	private long tempsDeb = 0;
	private TacheInterneServeur tis;
	private Stats stats;
	private Connection conn;
	private int nb_connexions_tot = 0;
	private NbConnexions nb_connexions;
	private int nb_matchs = 0;

	public TacheServeur(LinkedList<JoueurItf> liste, Connection conn, NbConnexions nb_connexions, String arg) {
		joueurs = liste;
		this.conn = conn;
		map = new HashMap<Integer, JoueurItf>();
		tmp = new HashMap<Integer, Integer>();
		this.nb_connexions = nb_connexions;
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

		// System.out.println("j1 = " + j1.getSummonerId() + " j2 = " +
		// j2.getSummonerId());
		stats.miseAJour(j1, j2);

		try {
			// System.out.println("j'envoie a " + j1.getSummonerId());
			br1 = new DataOutputStream(j1.getSocket().getOutputStream());
			// System.out.println("j'envoi a " + j2.getSummonerId());
			br2 = new DataOutputStream(j2.getSocket().getOutputStream());

			/*
			 * on envoi au joueur le temps qu'il a attendu dans la file du
			 * serveur suivi du summonerElo, de la latence et de la durée dans
			 * la file d'attente de son adversaire
			 */
			br1.writeBytes("infoJoueur " + duration1 + " " + j2.getSummonerId()
					+ " " + j2.getSummonerElo() + " " + j2.getLatency() + " "
					+ duration2 + "\n");
			br2.writeBytes("InfoJoueur " + duration2 + " " + j1.getSummonerId()
					+ " " + j1.getSummonerElo() + " " + j1.getLatency() + " "
					+ duration1 + "\n");
			j1.getSocket().close();
			j2.getSocket().close();

			nb_matchs += 2;
			/*
			 * if (nb_connexions_tot%2 == 0) { 
			 * 		if (nb_connexions_tot == nb_matchs) { 
			 * 			tis.cancel(); 
			 * 			tc.cancel(); 
			 * 			stats.fin(tempsDeb);
			 * 		 } 
			 * }
			 * else { 
			 * 		if ((nb_connexions_tot - 1) == nb_matchs) { 
			 * 			tis.cancel();
			 * 			tc.cancel(); 
			 * 			stats.fin(tempsDeb); 
			 * 		 } 
			 * }
			 */
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
			ResultSet res;
			int taille = 0;
			int cpt = 0;

			try {
				Statement st = conn.createStatement();
				// String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";

				// update de la colonne Duration de l'ensemble des joueurs de la base
				st.executeUpdate("UPDATE Joueurs SET duration = duration + 1");

				// insertion de l'ensemble des joueurs qui se sont connectés.
				synchronized (joueurs) {
					synchronized (map) {
						while (joueurs.isEmpty() && map.isEmpty()) {
							try {
								joueurs.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (first) {
							tempsDeb = System.currentTimeMillis();
							first = false;
						}
						nb_connexions_tot += joueurs.size();
						/*
						 * if (nb_connexions_tot == 100000) { 
						 * 		tc.cancel(); 
						 * }
						 */
						taille = joueurs.size();
						while (!joueurs.isEmpty()) {
							String requete = "INSERT INTO Joueurs (summonerId, duration, geom)";
							for (int i = 0; i < 400; i++) {
								if ((i != 399) && (cpt != taille - 1)) {
									requete += "select "
											+ joueurs.getFirst()
													.getSummonerId()
											+ ", 1, "
											+ "ST_GeomFromText('POINT("
											+ joueurs.getFirst()
													.getSummonerElo() + " "
											+ joueurs.getFirst().getLatency()
											+ ")', 4326) UNION ALL ";
									map.put(new Integer(joueurs.getFirst().getSummonerId()), joueurs.getFirst());
									joueurs.removeFirst();
									cpt++;
								} else {
									requete += "select "
											+ joueurs.getFirst().getSummonerId() + ", 1, "
											+ "ST_GeomFromText('POINT("
											+ joueurs.getFirst().getSummonerElo() + " "
											+ joueurs.getFirst().getLatency() + ")', 4326);";
									map.put(new Integer(joueurs.getFirst()
											.getSummonerId()), joueurs
											.getFirst());
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

				// test pour savoir si des joueurs ont attendus dans la file
				// d'attente depuis trop longtemps
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
				res = st.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 "
						+ "WHERE id1<>id2 AND ST_Distance(J1.geom, J2.geom) < 20 group by id1"); // group
																									// by
																									// id1
				while (res.next()) {
					// System.out.println("id: " + res.getInt(1) + " id ref: "+
					// res.getInt(2));
					if ((tmp.get(new Integer(res.getInt(1))) == null)
							&& (tmp.get(new Integer(res.getInt(2))) == null)) {
						tmp.put(new Integer(res.getInt(1)),
								new Integer(res.getInt(1)));
						tmp.put(new Integer(res.getInt(2)),
								new Integer(res.getInt(2)));
						EnvoiInfoJoueur(map.get(new Integer(res.getInt(1))),
								res.getInt(3),
								map.get(new Integer(res.getInt(2))),
								res.getInt(4));
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
				res = st.executeQuery("SELECT J1.summonerId id1, J2.summonerId id2, J1.duration, J2.duration FROM Joueurs J1, Joueurs J2 "
						+ "WHERE id1<>id2 AND J1.duration >= 5 AND J2.duration >= 5 ORDER BY ST_Distance(J1.geom, J2.geom)");
				while (res.next()) {
					// System.out.println("id: " + res.getInt(1) + " id ref: "+
					// res.getInt(2) + " duration1 " + res.getInt(3) +
					// " duration2 " + res.getInt(4));
					if ((tmp.get(new Integer(res.getInt(1))) == null)
							&& (tmp.get(new Integer(res.getInt(2))) == null)) {
						tmp.put(new Integer(res.getInt(1)),
								new Integer(res.getInt(1)));
						tmp.put(new Integer(res.getInt(2)),
								new Integer(res.getInt(2)));
						EnvoiInfoJoueur(map.get(new Integer(res.getInt(1))),
								res.getInt(3),
								map.get(new Integer(res.getInt(2))),
								res.getInt(4));
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
					for (Integer i : tmp.keySet()) {
						j++;
						map.remove(i);
						if (j == tmp.size()) {
							requete += " summonerId = " + tmp.get(i).intValue();
						} else {
							requete += " summonerId = " + tmp.get(i).intValue() + " OR";
						}
					}
					// System.out.println(requete);
					tmp.clear();
				}
				try {
					st = conn.createStatement();
					st.executeUpdate(requete);
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			} else {
				deleteSup1000();
			}
		}

		public void deleteSup1000() {
			LinkedList<Integer> liste = new LinkedList<Integer>();
			int cpt = 0;
			Statement st;
			int taille = tmp.size();

			for (Integer i : tmp.keySet()) {
				liste.add(tmp.get(i));
			}
			synchronized (map) {
				while (!liste.isEmpty()) {
					String requete = "DELETE FROM Joueurs WHERE";
					for (int i = 0; i < 500; i++) {

						if ((i != 499) && (cpt != taille - 1)) {
							requete += " summonerId = "
									+ liste.getFirst().intValue() + " OR";
							map.remove(liste.getFirst());
							liste.removeFirst();
							cpt++;
						} else {
							requete += " summonerId = "
									+ liste.getFirst().intValue();
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