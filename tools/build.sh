#!/bin/bash

source tools/setup_functions.sh

gradle install

mvnsub master clean install
mvnsub controller clean install
mvnsub workbench clean install



