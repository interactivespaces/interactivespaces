#!/bin/bash

source tools/setup_functions.sh

MODE=$1
LIBDIR=lib
ISDIR=$HOME/interactivespaces

check_gradle Gradle 1.6
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

download rxtx 2.1 -* http://rxtx.qbang.org
download bluecove 2.1.0 .jar http://bluecove.org/
download jsr80 1.0.1 .jar http://sourceforge.net/projects/javax-usb/
download usb4java 1.0.0 .jar http://kayahr.github.io/usb4java/

check_android sdk 21.1 
check_android platform android-16

check_ros main fuente 
check_ros path $PWD

check_is master 1.4.3
check_is controller 1.4.3
check_is workbench 1.4.3
check_maven repository $HOME/.m2

colorize_output reset

