package centralise_1V1;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Joueur {
	
	public static void main (String [] args) {
		
		int summonerElo;
		int latency;
		JoueurItf j1 = null;
				
	
		try {
			
			// recherche du serveur pour effectuer un matchmaking
			Registry registry = LocateRegistry.getRegistry("localhost");
			ServeurItf s = (ServeurItf) registry.lookup("serveur");
			
			Connection connection = null;
			
			// connexion vers la base de données.
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:../../M1_SAR/stage_M1/database_new.db");
			try {
				// récupération des joueurs de la base de données
				Statement s1 = connection.createStatement();
				//Statement s2 = connection.createStatement();
				ResultSet res1 = s1.executeQuery("select * from Summoner");
				while (res1.next()) {
					summonerElo  = res1.getInt("summonerElo");
					// on ne garde que les joueurs dont le elo a une valeur significative
					if (summonerElo != -1) {
						// récupération de la latence moyenne de chaque joueur conservés
						/*ResultSet res2 = s2.executeQuery("select avg(userServerPing) from games where userId = " + res1.getInt("acctId"));
						res2.next();
						
						// création d'un nouveau joueur ayant pour critères les critères récupérés depuis la base de données 
						j1 = new JoueurImpl(res2.getInt("userServerPing"), summonerElo);*/
						latency = (int)(Math.random()*100);
						j1 = new JoueurImpl(latency, summonerElo);
						s.matchmaking(j1);		
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			
		} catch (NotBoundException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
