#!/bin/bash

if [[ "$1" == "clean" ]]; then
  mvn clean
elif [[ "$1" == "help" ]]; then
    echo "  clean   Pulisce la cache della directory "
    echo "  noArgs  pulisci e compila il progetto"
    echo "  help    Mostra comandi"
    echo "  project Pulisce la cache, compila e crea il file .jar nella cartella target"
    echo "  run     Lancia il server"
elif [[ "$1" == "project" ]]; then
  mvn clean compile package
elif [[ "$1" == "run" ]]; then
    mvn clean compile package
    mvn spring-boot:run
else
  mvn clean compile
fi