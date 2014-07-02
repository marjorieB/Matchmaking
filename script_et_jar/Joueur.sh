for i in `seq 1 10`
do
	java -jar Joueur.jar &
	sleep 12m;
	sleep 30s;
	kill $!;
done

for i in `seq 1 10`
do 
	java -jar Joueur.jar &
	sleep 13m;
	sleep 30s;
	kill $!;
done

for i in `seq 1 10`
do
	java -jar Joueur.jar &
	sleep 22m;
	sleep 30s;
	kill $!;
done
