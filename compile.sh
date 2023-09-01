#!/bin/bash

ERROR=0
if [ ! -f "./lib/aima-core.jar" ]; then
  echo "Missing library lib/aima-core.jar"
  ERROR=1
fi
if [ ! -f "./lib/aima-gui.jar" ]; then
  echo "Missing library lib/aima-gui.jar"
  ERROR=1
fi
if [ $ERROR -eq 1 ]; then
  exit 1
fi
mkdir -p bin/
javac -classpath lib/aima-core.jar src/tddc17/*.java -d bin/
