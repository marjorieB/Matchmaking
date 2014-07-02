#!/bin/bash

for i in `seq 1 10`
do
	java -jar ServeurRandom.jar $i;
done

for i in `seq 1 10`
do
	java -jar ServeurNaif.jar $i;
done

for i in `seq 1 10`
do 
	java -jar ServeurBDSpatiale.jar $i;
done
