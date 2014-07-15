#!/bin/bash

for i in `seq 1 10`;
do
	touch statistiques_joueurs_random_$i.csv;	
	echo -e "summonerId1,summonerElo1,latence1,duration1,temps_exécution1,summonerId2,summonerElo2,latence2,duration2,temps_exécution2" >> statistiques_joueurs_random_$i.csv;
	cat statistiques_joueurs_random$i.csv >> statistiques_joueurs_random_$i.csv;
	rm -f statistiques_joueurs_random$i.csv;
	mv statistiques_joueurs_random_$i.csv statistiques_joueurs_random$i.csv
done
