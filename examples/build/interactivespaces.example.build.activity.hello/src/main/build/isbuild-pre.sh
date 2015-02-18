#!/bin/bash

FULLPATH="${1}/interactivespaces/build/test"
mkdir -p "${FULLPATH}"
NEWFILE="${FULLPATH}/Foo.java"
echo $NEWFILE

read -d '' classdef <<- EOF
package interactivespaces.build.test;

public class Foo {
}
EOF

echo "$classdef" >$NEWFILE

