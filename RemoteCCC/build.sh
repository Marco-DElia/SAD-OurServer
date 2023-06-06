#!/bin/bash

if [[ "$1" == "clean" ]]; then
  mvn clean
elif [[ "$1" == "help" ]]; then
    echo "  clean   Pulisce la cache della directory "
    echo "  noArgs  pulisci e compila il progetto"
    echo "  help    Mostra comandi"
    echo "  project Pulisce la cache, compila e crea il file .jar nella cartella target"
    echo "  crun    Pulisce la cache, compila ,crea il file .jar e lancia il server"
    echo "  run     Lancia il server, ATTENZIONE lanciare dopo il comando \"project\"" 
elif [[ "$1" == "project" ]]; then
  mvn clean compile package
elif [[ "$1" == "crun" ]]; then
    mvn clean compile package
    cd target
    java -jar  App-0.0.1-SNAPSHOT.jar
elif [[ "$1" == "run" ]]; then
    cd target
    java -jar  App-0.0.1-SNAPSHOT.jar
elif [[ "$1" == "Mrun" ]]; then
     /usr/bin/env /Users/emanuele/.vscode/extensions/redhat.java-1.18.0-darwin-arm64/jre/17.0.7-macosx-aarch64/bin/java @/var/folders/sg/dy9ntk653yd0qzspgd59ttbh0000gn/T/cp_9ld78d8uuxpgtkncdan4448az.argfile RemoteCCC.App.App
else
  mvn clean compile
fi
