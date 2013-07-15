#!/bin/bash -e

source tools/setup_functions.sh

setup_ros_path $PWD

gradle install

mvnsub master clean install
mvnsub controller clean install
mvnsub workbench clean install



