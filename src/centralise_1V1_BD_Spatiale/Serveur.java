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
	    	st.executeUpdate("DROP TABLE IF EXISTS Joueurs");
	    	st.executeUpdate("CREATE TABLE Joueurs(summonerId INTEGER, duration INTEGER, PRIMARY KEY(summonerId))");
	    	System.out.println("create executé");
	    	st.executeUpdate("SELECT AddGeometryColumn ('Joueurs', 'geom', 4326, 'POINT', 'XY')");
	    	st.executeUpdate("CREATE INDEX index_summonerId ON Joueurs(summonerId)");
	    	st.executeUpdate("CreateSpatialIndex(Joueurs, geom)");
	    	System.out.println("après addgeometry...");	
	    } catch (ClassNotFoundException e1) {
	    	e1.printStackTrace();    
	    } catch(SQLException e) {
	        System.err.println(e.getMessage());
	    }
	
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
					System.out.println("fermeture de la socket c'est pour ca qu'après cest null!!!!!!");
					scom.close();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
