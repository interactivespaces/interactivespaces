The Interactive Spaces Comm Support
***************************************

Interactive Spaces supplies support for using serial and other communication interfaces to
communicate with a variety of hardware. This lets you develop interactivity in your space
using hardware like Arduinos.

Setting Up Comm Support
=======================

The Comm support does not come set up out of the box. It requires libraries specific to the
platform running the Space Controller.

First locate a version of the RXTX Java Serial library for your platform. There are versions for
Linux, MacOS, and Windows. Follow any directions found to download and install the library anywhere
you want on the computer running the Space Controller.

The folder *extras* in your Space Controller installation contains two files

* interactivespaces-comm-<version>.jar
* comm-rxtx.ext

where *<version>* is the version of the Comm library found in the folder.

The JAR file should be copied into the *bootstrap* folder of the Space Controller. The 
*comm-rxtx.ext* file should be copied into the Space Controller's *lib/system/java* folder.
Edit the file and look for a line with the prefix *path:*. Change the rest of the line to
point at the RXTX jar you have installed on the computer.

Now restart the Space Controller and the Comm support will be available.

Using Serial Comm Support
=========================

An Example
----------

An example of using the Serial Comm support can be found in the 
*interactivespaces.example.activity.arduino.analog.java* example in the Workbench's
*examples* folder.

