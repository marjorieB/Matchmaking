#!/bin/bash

for i in `seq 1 10`;
do
	touch Statistiques_random_temps_$i.csv;	
	echo -e "temps_total_exécution" >> Statistiques_random_temps_$i.csv;
	cat Statistiques_random_temps$i.csv >> Statistiques_random_temps_$i.csv;
	rm -f Statistiques_random_temps$i.csv;
	mv Statistiques_random_temps_$i.csv Statistiques_random_temps$i.csv
done
