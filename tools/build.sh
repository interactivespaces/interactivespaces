#!/bin/bash -e

source tools/setup_functions.sh

setup_ros_path $PWD

env | fgrep ROS_PACKAGE

gradle install

mvnsub master clean install
mvnsub controller clean install
mvnsub workbench clean install



