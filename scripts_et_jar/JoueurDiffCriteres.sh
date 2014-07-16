#!/bin/bash

for i in `seq 1 80`
do 
	java -jar JoueurDiffCriteres.jar &
	sleep 6m;
	kill $!;
done
