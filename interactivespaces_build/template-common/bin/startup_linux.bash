# Figure out, regardless of any symlinks, aliases, etc, where this script
# is located.
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Get to the directory above the bin directory.
cd $DIR/..

# Read user configuration file, if present.
RCFILE=$HOME/.interactivespaces.rc
if [ -f $RCFILE ]; then
  echo Reading config from $RCFILE...
  source $RCFILE
fi

# Start up Interactive Spaces
if [ $# == 0 ] || [ $1 == "foreground" ]; then
  java -server -jar interactivespaces-launcher-${interactivespaces.version}.jar
fi

if [ "$1" == "background" ]; then
  nohup java -server -jar interactivespaces-launcher-${interactivespaces.version}.jar --noshell &>/dev/null &
fi
