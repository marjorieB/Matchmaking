#!/bin/bash

for i in `seq 1 10`;
do
	touch nb_connexions_par_seconde_random_5V5$i.csv;	
	echo -e "nb_connexions_par_seconde" >> nb_connexions_par_seconde_random_5V5$i.csv;
	cat nb_connexions_par_seconde_random_5V5_$i.csv >> nb_connexions_par_seconde_random_5V5$i.csv;
	rm -f nb_connexions_par_seconde_random_5V5_$i.csv;
	mv nb_connexions_par_seconde_random_$i.csv nb_connexions_par_seconde_random$i.csv
done
