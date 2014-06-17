package centralise_1V1_random;


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
		LinkedList<JoueurItf> joueurs = new LinkedList<JoueurItf>();
		ThreadServer t = new ThreadServer(joueurs);
		t.start();
		
		try {
			ss = new ServerSocket(12345);
			while (true) {
				scom = ss.accept();
				scom.setSoTimeout(0);
				br = new BufferedReader(new InputStreamReader(scom.getInputStream()));
				recu = br.readLine();
				demandes = recu.split(" ");
				if (demandes[0].equalsIgnoreCase("matchmaking")) {
					synchronized (joueurs) {
						JoueurItf joueur = new JoueurImpl(Integer.parseInt(demandes[1]), Integer.parseInt(demandes[2]), Integer.parseInt(demandes[3]), scom, System.currentTimeMillis());
						joueur.setDuration(0);
						joueurs.add(joueur);
						joueurs.notify();
					}
				}
				else {
					// erreur
					scom.close();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
