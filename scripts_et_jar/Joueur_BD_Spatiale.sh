#!/bin/bash

for i in `seq 1 10`
do
	java -jar Joueur.jar &
	sleep 35m;
	kill $!;
done
