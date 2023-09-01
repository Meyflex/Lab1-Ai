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
if [ ! -f "./bin/tddc17/MyAgentState.class" ]; then
  echo "Project was not compiled"
  ERROR=1
fi
if [ $ERROR -eq 1 ]; then
  exit 1
fi
cd bin
java -classpath ../lib/aima-gui.jar:../lib/aima-core.jar:. aima.gui.applications.liuvacuum.LIUVacuumApp
