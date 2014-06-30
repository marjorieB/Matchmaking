package centralise_1V1_utilitaire;

public class ThreadJoueur extends Thread {
	private JoueurItf j;
	private Objet obj;
	
	public ThreadJoueur (JoueurItf j, Objet obj) {
		this.j = j;
		this.obj = obj;
	}
	
	public void run() {
		j.match();
		synchronized(obj) {
			obj.notify();
		}
	}
}
