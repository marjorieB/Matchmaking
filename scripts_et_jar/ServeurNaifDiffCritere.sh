#!/bin/bash

for i in `seq 1 10`
do
	for j in 40 100 200 300 500 1000 2000 3000	
	do
		java -jar ServeurNaifDiffCriteres.jar $i $j;
	done
done
