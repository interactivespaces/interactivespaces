#!/bin/bash -e

source tools/setup_functions.sh

setup_ros_path $PWD

if [ `newest_file interactivespaces_msgs/` -nt `newest_file nrosjava_messages/` ]; then
  echo Messages have been modified, forcing rebuild.
  (cd nrosjava_messages; gradle clean)
fi

gradle install installDev

echo Done with interactivespaces build.
