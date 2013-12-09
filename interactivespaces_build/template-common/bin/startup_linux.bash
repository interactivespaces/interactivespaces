#!/bin/bash

# Figure out, regardless of any symlinks, aliases, etc, where this script
# is located.
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Get to the directory above the bin directory.
cd $DIR/..

EXTRAARGS=
# Read user configuration file, if present.
RCFILE=$HOME/.interactivespaces.rc
if [ -f $RCFILE ]; then
  echo Reading config from $RCFILE...
  source $RCFILE
fi

CONTAINERARGS=./lib/system/java/container.args
if [ -f $CONTAINERARGS ]; then
  CONTAINERARGS_CONTENTS=`cat ${CONTAINERARGS}`
  EXTRAARGS="${EXTRAARGS} ${CONTAINERARGS_CONTENTS}"
fi

# Start up Interactive Spaces
if [ $# == 0 ] || [ $1 == "foreground" ]; then
  java ${EXTRAARGS} -server -jar interactivespaces-launcher-@INTERACTIVESPACES_VERSION@.jar
fi

if [ "$1" == "background" ]; then
  nohup java ${EXTRAARGS} -server -jar interactivespaces-launcher-@INTERACTIVESPACES_VERSION@.jar --noshell &>/dev/null &
fi
