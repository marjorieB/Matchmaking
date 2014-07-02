#!/bin/bash

for i in `seq 1 10`
do
	java -jar Joueur.jar &
	sleep 4m;
	sleep 30s;
	kill $!;
done
