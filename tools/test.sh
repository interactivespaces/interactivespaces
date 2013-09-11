#!/bin/bash -e

source tools/setup_functions.sh

setup_ros_path $PWD

gradle test jacocoTestReport

echo Done with interactivespaces test.
