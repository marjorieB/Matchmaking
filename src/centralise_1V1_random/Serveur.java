package centralise_1V1_random;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import centralise_1V1_utilitaire.*;

public class Serveur {
	
	public static void main (String [] args) {
		ServerSocket ss;
		Socket scom;
		BufferedReader br;
		String recu;
		String [] demandes;
		LinkedList<JoueurItf> joueurs = new LinkedList<JoueurItf>();
		ThreadServer t = new ThreadServer(joueurs, args[0]);
		int nb_connexions = 0;
		t.start();
		
		try {
			ss = new ServerSocket(12345);
			while (true) {
				if (nb_connexions == 100000) {
					break;
				}
				scom = ss.accept();
				scom.setSoTimeout(0);
				br = new BufferedReader(new InputStreamReader(scom.getInputStream()));
				recu = br.readLine();
				demandes = recu.split(" ");
				if (demandes[0].equalsIgnoreCase("matchmaking")) {
					synchronized (joueurs) {
						nb_connexions++;
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
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			scom = ss.accept();
			scom.setSoTimeout(0);
			br = new BufferedReader(new InputStreamReader(scom.getInputStream()));
			recu = br.readLine();
			System.exit(0);
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
