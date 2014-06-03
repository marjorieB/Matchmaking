package centralise_1V1_BD_Spatiale;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

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
		LinkedList<JoueurItf> joueurs = new LinkedList<JoueurItf>();
		Connection conn = null;
			
	    try {
	    	Class.forName("org.sqlite.JDBC");
	    	config = new SQLiteConfig();
	    	config.enableLoadExtension(true);

	    	conn = DriverManager.getConnection("jdbc:sqlite:../../M1_SAR/stage_M1/bd_spatiale.db" ,config.toProperties());
	    	
	    	st = conn.createStatement();
	    	System.out.println("avant le load extension");
	    	//st.execute("SELECT load_extension('../../M1_SAR/stage_M1/spatialite-linux-i686-1.0a/VirtualShape.so')");
	    	st.execute("SELECT load_extension('/usr/lib/libspatialite.so')");

	    	System.out.println("après le load extension");
	    	st.execute("SELECT InitSpatialMetadata()");
	    	st.execute("INSERT OR IGNORE INTO spatial_ref_sys (srid, auth_name, auth_srid, ref_sys_name, proj4text)" +
	    			" VALUES (4326, 'moi', 4326, 'WGS84', '+proh=longlat +ellps=WGS84 +datum=WGS84 +no_defs')");
	    	System.out.println("après le InitSpatialMetadate");
	    	st.executeUpdate("DROP TABLE IF EXISTS Joueurs");
	    	System.out.println("après le drop");
	    	st.executeUpdate("CREATE TABLE Joueurs(summonerId INTEGER, duration INTEGER, PRIMARY KEY(summonerId))");
	    	System.out.println("create executé");
	    	st.executeUpdate("SELECT AddGeometryColumn(\'Joueurs\', \'geom\', 4326, 'POINT', 2)");
	    	System.out.println("addgeometryColumn exécuté");
	    	st.executeUpdate("CREATE INDEX index_summonerId ON Joueurs(summonerId)");
	    	System.out.println("index créé");
	    	st.executeUpdate("SELECT CreateSpatialIndex(\'Joueurs\', \'geom\')");
	    	System.out.println("index spatial créé");
	    } catch (ClassNotFoundException e1) {
	    	e1.printStackTrace();    
	    } catch(SQLException e) {
	        System.err.println(e.getMessage());
	    }
	
	    System.out.println("avant création de la thread");
		ThreadServer t = new ThreadServer(joueurs, conn);
		t.start();
		
		try {
			ss = new ServerSocket(12345);
			while (true) {
				scom = ss.accept();
				br = new BufferedReader(new InputStreamReader(scom.getInputStream()));
				recu = br.readLine();
				System.out.println(recu);
				demandes = recu.split(" ");
				if (demandes[0].equalsIgnoreCase("matchmaking")) {
					synchronized (joueurs) {
						JoueurItf joueur = new JoueurImpl(Integer.parseInt(demandes[1]), Integer.parseInt(demandes[2]), Integer.parseInt(demandes[3]), scom);
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
