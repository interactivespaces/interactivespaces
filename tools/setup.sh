#!/bin/bash -e

source tools/setup_functions.sh

check_gradle Gradle 1.6 || install_gradle 1.6
check_gradle Groovy 1.8.6
check_gradle Ivy 2.2.0
check_gradle JVM 1.6.0

package mercurial 2.0.2
package maven 3.0.4
package jython 2.5.1
package texlive-latex-base 2009-15
package texlive-latex-recommended 2009-15
package texlive-latex-extra 2009-10
package texlive-fonts-recommended 2009-15

checkprop rxtx     2.1
checkprop bluecove 2.1.0
checkprop jsr80    1.0.1
checkprop usb4java 1.0.0
checkprop jython   jython

check_android sdk 21.1 
check_android platform android-16

check_ros main fuerte || install_ros fuerte
check_ros path $PWD

check_maven repository $HOME/.m2
