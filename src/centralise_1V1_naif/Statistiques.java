package centralise_1V1_naif;

import java.util.ArrayList;

public class Statistiques {
	private double sommeEcartsSummonerElo;
	private double sommeEcartsLatence;
	private double sommeDistance;
	private double sommeTemps;
	private double tempsFin;
	private int nb_joueurs;
	private int nb_joueurs_duration1;
	private int nb_joueurs_duration5;
	private int nbJoueursMatches20; // nombre de joueurs matchés dont la distance est supérieure à 20 et inférieure à 50
	private int nbJoueursMatches50; //nombre de joueurs matchés dont la distance est supérieure à 50 et inférieurs à 100
	private int nbJoueursMatches100; //nombre de joueurs matchés dont la distance est supérieure à 100
	private double meilleureDistance;
	private JoueurItf[] meilleursJoueursDistance;
	private double meilleurTemps;
	private JoueurItf meilleurJoueurTemps;
	private double meilleurEcartLatence;
	private JoueurItf[] meilleursJoueursLatence;
	private double meilleurEcartSummonerElo;
	private JoueurItf[] meilleursJoueursSummonerElo;
	private double pireDistance;
	private JoueurItf[] piresJoueursDistance;
	private double pireTemps;
	private JoueurItf pireJoueurTemps;	
	private double pireEcartLatence;
	private JoueurItf[] piresJoueursLatence;
	private double pireEcartSummonerElo;
	private JoueurItf[] piresJoueursSummonerElo;
	private ArrayList<Float> tab;
	private double variance;
	
	public Statistiques() {
		sommeEcartsSummonerElo = 0;
		sommeEcartsLatence = 0;
		sommeDistance = 0;
		sommeTemps = 0;
		tempsFin = 0;
		nb_joueurs = 0;
		nb_joueurs_duration1 = 0;
		nb_joueurs_duration5 = 0;
		nbJoueursMatches20 = 0;
		nbJoueursMatches50 = 0;
		nbJoueursMatches100 = 0;
		meilleureDistance = Double.MAX_VALUE;
		meilleursJoueursDistance = new JoueurItf[2];
		meilleurTemps =  Double.MAX_VALUE;
		meilleurEcartLatence = Double.MAX_VALUE;
		meilleursJoueursLatence = new JoueurItf[2];
		meilleurEcartSummonerElo = Double.MAX_VALUE;
		meilleursJoueursSummonerElo = new JoueurItf[2];
		pireDistance = -1;
		piresJoueursDistance = new JoueurItf[2];
		pireTemps = -1;
		pireEcartLatence = -1;
		piresJoueursLatence = new JoueurItf[2];
		pireEcartSummonerElo = -1;
		piresJoueursSummonerElo = new JoueurItf[2];
		tab = new ArrayList<Float>();
		variance = 0;
	}
	
	public void afficher_stats(double tempsDeb) {
		tempsFin = System.currentTimeMillis();
		double distanceMoyenne = (sommeDistance * 2) / nb_joueurs;
		// calcul de la variance
		for (Float f: tab) {
			variance += Math.pow(f.doubleValue() - distanceMoyenne, 2);
		}
		variance = variance * 2 / nb_joueurs;
		
		System.out.println("================================== STATISTIQUES ==================================");
		
		System.out.println("statistiques effectuées sur " + nb_joueurs + " joueurs");
		System.out.println("temps total d'exécution pour matcher les " + nb_joueurs + " joueurs = " + convert(tempsFin - tempsDeb));
		System.out.println("écarts summonerElo moyen " + (sommeEcartsSummonerElo * 2) / nb_joueurs);
		System.out.println("écarts latence moyen " + (sommeEcartsLatence * 2) / nb_joueurs);
		System.out.println("distance moyenne " + (sommeDistance * 2) / nb_joueurs);
		System.out.println("variance de la distance = " + variance);
		System.out.println("écart-type de la distance = " + Math.sqrt(variance));
		System.out.println("temps moyen " + sommeTemps / nb_joueurs + " ms");
		System.out.println("ratio temps sur la distance = " + (sommeTemps * 2 / sommeDistance));
		System.out.println("nombre de joueurs matchés directement " + nb_joueurs_duration1);
		System.out.println("nombre de joueurs matchés au bout de 5 unités de temps " + nb_joueurs_duration5);
		System.out.println("nombre de joueurs dont la distance de matche est compris entre 20 et 50 = " + nbJoueursMatches20);
		System.out.println("nombre de joueurs dont la distance de matche est compris entre 50 et 100 = " + nbJoueursMatches50);
		System.out.println("nombre de joueurs dont la distance de matche est supérieure à 100 = " + nbJoueursMatches100);
		

		System.out.println("meilleure distance " + meilleureDistance + " obtenue par les joueurs : ");
		System.out.println("\t\t joueur id = " + meilleursJoueursDistance[0].getSummonerId() + " elo = " + meilleursJoueursDistance[0].getSummonerElo() + 
				" latence " + meilleursJoueursDistance[0].getLatency() + " duration = " + meilleursJoueursDistance[0].getDuration());
		System.out.println("\t\t joueur id = " + meilleursJoueursDistance[1].getSummonerId() + " elo = " + meilleursJoueursDistance[1].getSummonerElo() + 
				" latence " + meilleursJoueursDistance[1].getLatency() + " duration = " + meilleursJoueursDistance[1].getDuration());
		System.out.println("meilleur temps " + convert(meilleurTemps) + " obtenu par le joueur : ");
		System.out.println("\t\t joueur id = " + meilleurJoueurTemps.getSummonerId() + " elo = " + meilleurJoueurTemps.getSummonerElo() + 
				" latence " + meilleurJoueurTemps.getLatency() + " duration = " + meilleurJoueurTemps.getDuration() + 
				" temps début " + meilleurJoueurTemps.getTime1() + " ms temps fin " + meilleurJoueurTemps.getTime2() + " ms");
		System.out.println("meilleur écart de latence " + meilleurEcartLatence + " obtenu par les joueurs : ");
		System.out.println("\t\t joueur id = " + meilleursJoueursLatence[0].getSummonerId() + " elo = " + meilleursJoueursLatence[0].getSummonerElo() + 
				" latence " + meilleursJoueursLatence[0].getLatency() + " duration = " + meilleursJoueursLatence[0].getDuration());
		System.out.println("\t\t joueur id = " + meilleursJoueursLatence[1].getSummonerId() + " elo = " + meilleursJoueursLatence[1].getSummonerElo() + 
				" latence " + meilleursJoueursLatence[1].getLatency() + " duration = " + meilleursJoueursLatence[1].getDuration());
		System.out.println("meilleur écart summonerElo " + meilleurEcartSummonerElo + " obtenu par les joueurs : ");
		System.out.println("\t\t joueur id = " + meilleursJoueursSummonerElo[0].getSummonerId() + " elo = " + meilleursJoueursSummonerElo[0].getSummonerElo() + 
				" latence " + meilleursJoueursSummonerElo[0].getLatency() + " duration = " + meilleursJoueursSummonerElo[0].getDuration());
		System.out.println("\t\t joueur id = " + meilleursJoueursSummonerElo[1].getSummonerId() + " elo = " + meilleursJoueursSummonerElo[1].getSummonerElo() + 
				" latence " + meilleursJoueursSummonerElo[1].getLatency() + " duration = " + meilleursJoueursSummonerElo[1].getDuration());		
		
		System.out.println("pire distance " + pireDistance + " obtenu par les joueurs : ");
		System.out.println("\t\t joueur id = " + piresJoueursDistance[0].getSummonerId() + " elo = " + piresJoueursDistance[0].getSummonerElo() + 
				" latence " + piresJoueursDistance[0].getLatency() + " duration = " + piresJoueursDistance[0].getDuration());
		System.out.println("\t\t joueur id = " + piresJoueursDistance[1].getSummonerId() + " elo = " + piresJoueursDistance[1].getSummonerElo() + 
				" latence " + piresJoueursDistance[1].getLatency() + " duration = " +piresJoueursDistance[1].getDuration());
		System.out.println("pire temps " + convert(pireTemps) + " obtenu par le joueur : ");
		System.out.println("\t\t joueur id = " + pireJoueurTemps.getSummonerId() + " elo = " + pireJoueurTemps.getSummonerElo() + 
				" latence " + pireJoueurTemps.getLatency() + " duration = " + pireJoueurTemps.getDuration() + 
				" temps début " + pireJoueurTemps.getTime1() + " ms temps fin " + pireJoueurTemps.getTime2() + " ms");
		System.out.println("pire écart de latence " + pireEcartLatence + " obtenu par les joueurs : ");
		System.out.println("\t\t joueur id = " + piresJoueursLatence[0].getSummonerId() + " elo = " + piresJoueursLatence[0].getSummonerElo() + 
				" latence " + piresJoueursLatence[0].getLatency() + " duration = " + piresJoueursLatence[0].getDuration());
		System.out.println("\t\t joueur id = " + piresJoueursLatence[1].getSummonerId() + " elo = " + piresJoueursLatence[1].getSummonerElo() + 
				" latence " + piresJoueursLatence[1].getLatency() + " duration = " + piresJoueursLatence[1].getDuration());
		System.out.println("pire écart summonerElo " + pireEcartSummonerElo + " obtenu par les joueurs : ");
		System.out.println("\t\t joueur id = " + piresJoueursSummonerElo[0].getSummonerId() + " elo = " + piresJoueursSummonerElo[0].getSummonerElo() + 
				" latence " + piresJoueursSummonerElo[0].getLatency() + " duration = " + piresJoueursSummonerElo[0].getDuration());
		System.out.println("\t\t joueur id = " + piresJoueursSummonerElo[1].getSummonerId() + " elo = " + piresJoueursSummonerElo[1].getSummonerElo() + 
				" latence " + piresJoueursSummonerElo[1].getLatency() + " duration = " + piresJoueursSummonerElo[1].getDuration());
	}

	public void miseAJour (JoueurItf j1, JoueurItf j2) {
		double ecartsSummonerElo = Math.abs(j1.getSummonerElo() - j2.getSummonerElo());
		double ecartsLatence = Math.abs(j1.getLatency() - j2.getLatency());
		double distance = Math.sqrt(Math.pow((j1.getSummonerElo() - j2.getSummonerElo()), 2) + Math.pow((j1.getLatency() - j2.getLatency()), 2));
		double temps = Math.abs(j1.getTime2() - j1.getTime1() + j2.getTime2() - j2.getTime1());
		double temps1 = j1.getTime2() - j1.getTime1();
		double temps2 = j2.getTime2() - j2.getTime1();
		
		nb_joueurs += 2;
		sommeEcartsSummonerElo += ecartsSummonerElo;
		sommeEcartsLatence += ecartsLatence;
		sommeDistance += distance;
		tab.add(new Float(distance));
		sommeTemps += temps;
		if (j1.getDuration() == 0) {
			nb_joueurs_duration1++;
		}
		if (j2.getDuration() == 0) {
			nb_joueurs_duration1++;
		}
		if (j1.getDuration() >= 5) {
			nb_joueurs_duration5++;
		}
		if (j2.getDuration() >= 5) {
			nb_joueurs_duration5++;
		}
		
		if (distance > 20 && distance <= 50) {
			nbJoueursMatches20 += 2;
		}
		else if (distance > 50 && distance <= 100) {
			nbJoueursMatches50 += 2;
		}
		else if (distance > 100) {
			nbJoueursMatches100 += 2;
		}
		
		if (distance < meilleureDistance) {
			meilleureDistance = distance;
			meilleursJoueursDistance[0] = j1;
			meilleursJoueursDistance[1] = j2;
		}
		if (temps1 < meilleurTemps) {
			meilleurTemps = temps1;
			meilleurJoueurTemps = j1;
		}
		if (temps2 < meilleurTemps) {
			meilleurTemps = temps2;			
			meilleurJoueurTemps = j2;
		}

		if (ecartsSummonerElo < meilleurEcartSummonerElo) {
			meilleurEcartSummonerElo = ecartsSummonerElo;
			meilleursJoueursSummonerElo[0] = j1;
			meilleursJoueursSummonerElo[1] = j2;
		}
		if (ecartsLatence < meilleurEcartLatence) {
			meilleurEcartLatence = ecartsLatence;
			meilleursJoueursLatence[0] = j1;
			meilleursJoueursLatence[1] = j2;
		}
		if (distance > pireDistance) {
			pireDistance = distance;
			piresJoueursDistance[0] = j1;
			piresJoueursDistance[1] = j2;
		}
		if (temps1 > pireTemps) {
			pireTemps = temps1;
			pireJoueurTemps = j1;
		}
		if (temps2 > pireTemps) {
			pireTemps = temps2;
			pireJoueurTemps = j2;
		}
		if (ecartsSummonerElo > pireEcartSummonerElo) {
			pireEcartSummonerElo = ecartsSummonerElo;
			piresJoueursSummonerElo[0] = j1;
			piresJoueursSummonerElo[1] = j2;
		}
		if (ecartsLatence > pireEcartLatence) {
			pireEcartLatence = ecartsLatence;
			piresJoueursLatence[0] = j1;
			piresJoueursLatence[1] = j2;
		}
	}
	
	
	public String convert (double ms) {
		String res;
		double millisecondes = ms % 1000; 
		ms = ms / 1000; 
		double secondes = ms % 60; 
		ms = ms / 60; 
		double minutes = ms % 60; 
		ms = ms / 60; 
		double heures = ms;
		
		res = (int)heures + " h " + (int)minutes + " min " + (int)secondes + " s " + (int)millisecondes + " ms";
		return res;
	}
}
