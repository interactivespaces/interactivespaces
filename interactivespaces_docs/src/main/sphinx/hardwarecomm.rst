The Interactive Spaces Hardware Communication Support
***************************************

Interactive Spaces supplies support for using serial and other communication interfaces to
communicate with a variety of hardware. This lets you develop interactivity in your space
using hardware like Arduinos.

Serial Communication
====================

A lot of hardware you may want to control with Interactive Spaces can be communicated with over a serial
connection.

Setting Up Serial Comm Support
------------------------------

The Serial Comm support does not come set up out of the box. It requires libraries specific to the
platform running the Space Controller.

First locate a version of the RXTX Java Serial library for your platform. There are versions for
Linux, MacOS, and Windows. Follow any directions found to download and install the library anywhere
you want on the computer running the Space Controller.

The folder *extras* in your Space Controller installation contains two files

* interactivespaces-service-comm-serial-<version>.jar
* comm-serial-rxtx.ext

where *<version>* is the version of the Comm library found in the folder.

The JAR file should be copied into the ``bootstrap`` folder of the Space Controller. The 
``comm-serial-rxtx.ext`` file should be copied into the Space Controller's ``config/environment`` folder.
Edit the file and look for a line with the prefix *path:*. Change the rest of the line to
point at the RXTX jar you have installed on the computer.

If the controller runs on a Linux device, the user which runs the controller must
be in the ``dialout`` Linux group to use serial devices.

If you are using OSX, you will probably need to set the environment variable ``DYLD_LIBRARY_PATH`` to point
at the proper location for the native serial libraries. Look up ``RXTX`` and ``OSX`` on the web for
details. The issue comes from the 32 bit vs. 64 bit versions of the libraries on OSX..

Now restart the Space Controller and the Serial Communication support will be available.

Using Serial Comm Support
-------------------------

An Example
^^^^^^^^^^

An example of using the Serial Comm support can be found in the 
``interactivespaces.example.activity.arduino.analog.java`` example in the Workbench's
``examples`` folder.

Bluetooth Communication
=======================

Bluetooth allows an Interactive Spaces activity to communicate with a lot of different hardware. An example would
be a Wii Remote.

Setting Up Bluetooth Communication Support
---------------------------------

The Bluetooth Communication support does not come set up out of the box. It requires libraries specific to the
platform running the Space Controller.

First locate a version of the Bluecove Java library for your platform. There are versions for
Linux, MacOS, and Windows. Follow any directions found to download and install the library anywhere
you want on the computer running the Space Controller.

The folder ``extras`` in your Space Controller installation contains two files

* interactivespaces-service-comm-bluetooth-<version>.jar
* comm-bluetooth-bluecove.ext

where *<version>* is the version of the Bluetooth Comm library found in the folder.

The JAR file should be copied into the ``bootstrap`` folder of the Space Controller. The 
``comm-serial-bluecove.ext`` file should be copied into the Space Controller's ``config/environment`` folder.
Edit the file and look for a line with the prefix ``path:``. Change the rest of the line to
point at the Bluecove jar you have installed on the computer.

If the controller is running on Linux, you will need to add a second ``path:`` line to 
``comm-serial-bluecove.ext`` which should point to the Bluecove GPL jar.

Also, the user which runs the controller must
be in the ``lp`` Linux group to use bluetooth devices.


Now restart the Space Controller and the Bluetooth Comm support will be available.

