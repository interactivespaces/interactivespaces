#!/bin/bash

source tools/setup_functions.sh

gradle install

mvnsub master install
mvnsub controller install
mvnsub workbench install



