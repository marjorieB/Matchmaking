#!/bin/bash

for i in `seq 1 10`;
do
	touch toto.csv;	
	echo -e "temps_total_exécution" >> toto.csv;
	cat Statistiques_random_5V5_temps$i.csv >> toto.csv;
	rm -f Statistiques_random_5V5_temps$i.csv;
	mv toto.csv Statistiques_random_5V5_temps$i.csv;
done