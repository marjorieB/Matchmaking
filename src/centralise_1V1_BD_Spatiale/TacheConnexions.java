package centralise_1V1_BD_Spatiale;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import centralise_1V1_utilitaire.NbConnexions;


public class TacheConnexions extends Thread {
	
	private NbConnexions nb_connexions;
	private FileWriter fw;
	
	public TacheConnexions (NbConnexions nb_connexions, String arg) {
		this.nb_connexions = nb_connexions;
		try {
			fw = new FileWriter("nb_connexions_par_seconde_BD_spatiale" + arg + ".csv");
			fw.write("nb_connexions_par_seconde\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TacheInterneConnexions tc = new TacheInterneConnexions();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(tc, 0, 1000);
	}
	
	public class TacheInterneConnexions extends TimerTask {
		
		@Override
		public void run() {
			synchronized(nb_connexions) {
				try {
					fw.write(nb_connexions.getNb_connexions() + "\n");
					fw.flush();
					nb_connexions.setNb_connexions(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
