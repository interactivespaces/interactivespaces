#!/bin/bash

# Figure out, regardless of any symlinks, aliases, etc, where this script
# is located.
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Figure out run mode.
if [ "$1" == "foreground" -o "$1" == "background" ]; then
  RUN_MODE=$1
  shift
else
  RUN_MODE=foreground
fi

# Append any additional command-line arguments to pass through to the IS runtime.
INTERACTIVESPACES_MAIN_ARGS+=" $@"

# Get to the directory above the bin directory.
cd $DIR/..

CONTAINER_TYPE=@INTERACTIVESPACES_CONTAINER_TYPE@

EXTRAARGS=
# Read user configuration file, if present.
RCFILE=$HOME/.interactivespaces.rc
if [ -f $RCFILE ]; then
  echo Reading config from $RCFILE...
  source $RCFILE
fi

CONTAINERARGS=./config/environment/container.args
if [ -f $CONTAINERARGS ]; then
  CONTAINERARGS_CONTENTS=`cat ${CONTAINERARGS}`
  EXTRAARGS="${EXTRAARGS} ${CONTAINERARGS_CONTENTS}"
fi

LOCALENVIRONMENTRC=./config/environment/localenvironment.rc
if [ -f $LOCALENVIRONMENTRC ]; then
  echo Reading config from $LOCALENVIRONMENTRC...
  source $LOCALENVIRONMENTRC
fi

CLASSPATH=interactivespaces-launcher-@INTERACTIVESPACES_VERSION@.jar

if [ -n $CLASSPATH_ADDITIONAL ]; then
  CLASSPATH="${CLASSPATH}:${CLASSPATH_ADDITIONAL}"
fi

# Start up Interactive Spaces
if [ $RUN_MODE == "foreground" ]; then
  java ${EXTRAARGS} -server -cp ${CLASSPATH} interactivespaces.launcher.InteractiveSpacesLauncher ${INTERACTIVESPACES_MAIN_ARGS}
fi

if [ $RUN_MODE == "background" ]; then
  nohup java ${EXTRAARGS} -server -cp ${CLASSPATH} interactivespaces.launcher.InteractiveSpacesLauncher --noshell ${INTERACTIVESPACES_MAIN_ARGS} &>/dev/null &
fi
