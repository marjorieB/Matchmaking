package centralise_1V1_naif;

import java.net.Socket;


public interface JoueurItf {
	public void jouer(int duration, int summonerEloAdversaire, int latencyAdversaire, int durationAdversaire);
	public void match();
	public int getSummonerElo();
	public void setSummonerElo(int elo);
	public int getDuration();
	public void setDuration(int duration);
	public int getLatency();
	public void setLatency(int latency);
	public Socket getSocket();
	public void setSocket(Socket s);
}
