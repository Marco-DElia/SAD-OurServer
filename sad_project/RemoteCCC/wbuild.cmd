@echo off

IF "%1"=="clean" (
  mvn clean
) ELSE IF "%1"=="help" (
  echo "  clean   Pulisce la cache della directory "
  echo "  noArgs  pulisci e compila il progetto"
  echo "  help    Mostra comandi"
  echo "  project Pulisce la cache, compila e crea il file .jar nella cartella target"
  echo "  crun    Pulisce la cache, compila ,crea il file .jar e lancia il server"
  echo "  run     Lancia il server, ATTENZIONE lanciare dopo il comando project"
) ELSE IF "%1"=="project" (
  mvn clean compile package
  cd ClientProject
  mvn clean
) ELSE IF "%1"=="crun" (
  mvn clean compile package
  cd target
  java -jar RemoteCCC-0.0.1.jar
) ELSE IF "%1"=="run" (
  cd target
  java -jar RemoteCCC-0.0.1.jar
) ELSE (
  mvn clean compile
)