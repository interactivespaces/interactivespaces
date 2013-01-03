The Interactive Spaces Comm Support
***************************************

Interactive Spaces supplies support for using serial and other communication interfaces to
communicate with a variety of hardware. This lets you develop interactivity in your space
using hardware like Arduinos.

Setting Up Serial Comm Support
=======================

The Serial Comm support does not come set up out of the box. It requires libraries specific to the
platform running the Space Controller.

First locate a version of the RXTX Java Serial library for your platform. There are versions for
Linux, MacOS, and Windows. Follow any directions found to download and install the library anywhere
you want on the computer running the Space Controller.

The folder *extras* in your Space Controller installation contains two files

* interactivespaces-service-comm-serial-<version>.jar
* comm-serial-rxtx.ext

where *<version>* is the version of the Comm library found in the folder.

The JAR file should be copied into the *bootstrap* folder of the Space Controller. The 
*comm-serial-rxtx.ext* file should be copied into the Space Controller's *lib/system/java* folder.
Edit the file and look for a line with the prefix *path:*. Change the rest of the line to
point at the RXTX jar you have installed on the computer.

Now restart the Space Controller and the Serial Comm support will be available.

Using Serial Comm Support
=========================

An Example
----------

An example of using the Serial Comm support can be found in the 
*interactivespaces.example.activity.arduino.analog.java* example in the Workbench's
*examples* folder.


Setting Up Bluetooth Comm Support
=======================

The Bluetooth Comm support does not come set up out of the box. It requires libraries specific to the
platform running the Space Controller.

First locate a version of the Bluecove Java library for your platform. There are versions for
Linux, MacOS, and Windows. Follow any directions found to download and install the library anywhere
you want on the computer running the Space Controller.

The folder *extras* in your Space Controller installation contains two files

* interactivespaces-service-comm-bluetooth-<version>.jar
* comm-bluetooth-bluecove.ext

where *<version>* is the version of the Bluetooth Comm library found in the folder.

The JAR file should be copied into the *bootstrap* folder of the Space Controller. The 
*comm-serial-bluecove.ext* file should be copied into the Space Controller's *lib/system/java* folder.
Edit the file and look for a line with the prefix *path:*. Change the rest of the line to
point at the Bluecove jar you have installed on the computer.

If you are running on Linux, you will need to add a second *path:* line to *comm-serial-bluecove.ext*.
This line should point to the Bluecove GPL jar.

Now restart the Space Controller and the Bluetooth Comm support will be available.


