package centralise_1V1_BD_Spatiale;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;

import centralise_1V1_utilitaire.NbConnexions;

public class TacheConnexions extends TimerTask {
	private NbConnexions nb;
	private FileWriter fw;
	
	
	public TacheConnexions(NbConnexions nb, String arg) {
		this.nb = nb;
		try {
			fw = new FileWriter("nb_connexions_par_seconde_BD_spatiale" + arg + ".csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		synchronized(nb) {
			try {
				fw.write(nb.getNb_connexions() + "\n");
				fw.flush();
				nb.setNb_connexions(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}