package centralise_1V1_naif;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Serveur {
	
	public static void main (String [] args) {
		ServerSocket ss;
		Socket scom;
		BufferedReader br;
		String recu;
		String [] demandes;
		JoueurItf j;
		LinkedList<JoueurItf> joueurs = new LinkedList<JoueurItf>();
	
		ThreadServer t = new ThreadServer(joueurs);
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
						joueurs.add(new JoueurImpl(Integer.parseInt(demandes[1]), Integer.parseInt(demandes[2]), scom));
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
