package centralise_1V1_utilitaire;

public class ThreadJoueur extends Thread {
	private JoueurItf j;
	
	public ThreadJoueur (JoueurItf j) {
		this.j = j;
	}
	
	public void run() {
		j.match();
	}
}
