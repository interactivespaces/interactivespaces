#!/bin/bash

# Figure out, regardless of any symlinks, aliases, etc, where this script
# is located.
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Get to the directory above the bin directory.
cd $DIR/..

java -server -jar interactivespaces-launcher-${interactivespaces.version}.jar ${@}

