package centralise_5V5_utilitaire;

public class ThreadJoueur extends Thread {
	private JoueurItf j;
	
	public ThreadJoueur (JoueurItf j) {
		this.j = j;
	}
	
	public void run() {
		j.match();
	}
}
