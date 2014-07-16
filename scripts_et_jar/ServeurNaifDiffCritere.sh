#!/bin/bash

for i in `seq 1 10`
do
	for i in 40 100 200 300 500 1000 2000 3000	
	do
		java -jar ServeurNaif_5V5.jar 1 $i;
	done
done
