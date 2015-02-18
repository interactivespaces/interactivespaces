#!/bin/bash -e

echo Hi from just after the build

mkdir -p "${1}"
NEWFILE="${1}/TestResource"
echo "${2}" >$NEWFILE
