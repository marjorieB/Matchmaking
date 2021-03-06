package centralise_1V1_BD_Spatiale;


import java.io.BufferedReader;

import centralise_1V1_utilitaire.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Timer;

import org.sqlite.SQLiteConfig;

public class Serveur {
	
	public static void main (String [] args) {
		ServerSocket ss;
		Socket scom;
		BufferedReader br;
		String recu;
		String [] demandes;
		SQLiteConfig config;
		Statement st;
		NbConnexions nb_connexions = new NbConnexions();
		LinkedList<JoueurItf> joueurs = new LinkedList<JoueurItf>();
		Connection conn = null;
		TacheConnexions tc = new TacheConnexions(nb_connexions, args[0]);
		tc.start();
		
	    try {
	    	Class.forName("org.sqlite.JDBC");
	    	config = new SQLiteConfig();
	    	config.enableLoadExtension(true);

	    	conn = DriverManager.getConnection("jdbc:sqlite:bd_spatiale.db" ,config.toProperties());
	    	
	    	st = conn.createStatement();
	    	st.execute("SELECT load_extension('../libspatialite.so.3.0.0')");
	    	//t.execute("SELECT load_extension('/usr/lib/libspatialite.so')");

	    	
	    	st.execute("SELECT InitSpatialMetadata()");
	    	st.execute("INSERT OR IGNORE INTO spatial_ref_sys (srid, auth_name, auth_srid, ref_sys_name, proj4text)" +
	    			" VALUES (4326, 'moi', 4326, 'WGS84', '+proh=longlat +ellps=WGS84 +datum=WGS84 +no_defs')");
	    	st.executeUpdate("DROP TABLE IF EXISTS Joueurs");
	    	st.executeUpdate("CREATE TABLE Joueurs(summonerId INTEGER, duration INTEGER, PRIMARY KEY(summonerId))");
	    	st.executeUpdate("SELECT AddGeometryColumn('Joueurs', 'geom', 4326, 'POINT', 2)");
	    	st.executeUpdate("CREATE INDEX index_summonerId ON Joueurs(summonerId)");
	    	st.executeUpdate("SELECT CreateSpatialIndex('Joueurs', 'geom')");
	    	/*st.executeUpdate("CREATE TRIGGER trigger_geom AFTER INSERT ON Joueurs\n" + 
	    			"BEGIN\nDECLARE res integer;\n select summonerId into res from Joueurs" + 
	    			" where NEW.SummonerId <> summonerId and ST_Distance(geom, New.geom) < 20 ORDER BY" +
	    			"ST_Distance(geom, New.geom) LIMIT 1;\n IF (res IS NOT NULL) THEN\n" +
	    			"UPDATE Joueurs SET summonerIdRef = NEW.summonerId WHERE summonerId=res;\n" +
	    			"UPDATE Joueurs SET NEW.summonerIdRef = res WHERE summonerId=NEW.summonerIdtrigger;\n" +
	    			"END IF;\n END;\n");
	    	System.out.println("après création du trigger");	*/	
					
	    } catch (ClassNotFoundException e1) {
	    	e1.printStackTrace();    
	    } catch(SQLException e) {
	        System.err.println(e.getMessage());
	    }
	
	    TacheServeur t = new TacheServeur(joueurs, conn, args[0]);	    
	    
		try {
			ss = new ServerSocket(12345);
			while (true) {
				if (nb_connexions.getNb_connexions() == 100000) {
					break;
				}
				scom = ss.accept();
				scom.setSoTimeout(0);
				br = new BufferedReader(new InputStreamReader(scom.getInputStream()));
				recu = br.readLine();
				//System.out.println(recu);
				demandes = recu.split(" ");
				if (demandes[0].equalsIgnoreCase("matchmaking")) {
					synchronized (joueurs) {
						nb_connexions.setNb_connexions(nb_connexions.getNb_connexions() + 1);
						JoueurItf joueur = new JoueurImpl(Integer.parseInt(demandes[1]), Integer.parseInt(demandes[2]), Integer.parseInt(demandes[3]), scom, System.currentTimeMillis());
						joueur.setDuration(0);
						joueurs.add(joueur);
						joueurs.notify();
					}
				}
				else {
					//erreur
					System.out.println("close de la socket");
					scom.close();
				}
			}
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
