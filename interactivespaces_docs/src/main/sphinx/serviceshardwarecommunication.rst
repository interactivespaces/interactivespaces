The Interactive Spaces Hardware Communication Support
***************************************

Interactive Spaces supplies support for using serial and other communication interfaces to
communicate with a variety of hardware. This lets you develop interactivity in your space
using hardware like Arduinos.

Serial Communication
====================

A lot of hardware you may want to control with Interactive Spaces can be communicated with over a serial
connection. Interactive Spaces includes support for working with serial connections.

Setting Up Serial Communication Support
------------------------------

The Serial Communication support does not come set up out of the box. It requires libraries specific to the
platform running the Space Controller.

First locate a version of the RXTX Java Serial library for your platform. There are versions for
Linux, MacOS, and Windows. Follow any directions found to download and install the library anywhere
you want on the computer running the Space Controller.

The folder *extras* in your Space Controller installation contains two files

* interactivespaces-service-comm-serial-<version>.jar
* comm-serial-rxtx.ext

where *<version>* is the version of the Communication library found in the folder.

The JAR file should be copied into the ``bootstrap`` folder of the Space Controller. The 
``comm-serial-rxtx.ext`` file should be copied into the Space Controller's ``config/environment`` folder.
Edit the file and look for a line with the prefix *path:*. Change the rest of the line to
point at the RXTX jar you have installed on the computer.

If the controller runs on a Linux device, the user which runs the controller must
be in the ``dialout`` Linux group to use serial devices.

If you are using OSX, you will probably need to set the environment variable ``DYLD_LIBRARY_PATH`` to point
at the proper location for the native serial libraries. Look up ``RXTX`` and ``OSX`` on the web for
details. The issue comes from the 32 bit vs. 64 bit versions of the libraries on OSX.

Now restart the Space Controller and the Serial Communication support will be available.

Using Serial Communication Support
-------------------------

An Example
^^^^^^^^^^

An example of using the Serial Communication support can be found in the 
``interactivespaces.example.activity.arduino.analog.java`` example in the Workbench's
``examples`` folder.

Bluetooth Communication
=======================

Bluetooth allows an Interactive Spaces activity to communicate with hardware that communicates via Bluetooth. 
An example, supported by Interactive Spaces, is a Wii Remote.

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

where *<version>* is the version of the Bluetooth Communication library found in the folder.

The JAR file should be copied into the ``bootstrap`` folder of the Space Controller. The 
``comm-serial-bluecove.ext`` file should be copied into the Space Controller's ``config/environment`` folder.
Edit the file and look for a line with the prefix ``path:``. Change the rest of the line to
point at the Bluecove jar you have installed on the computer.

If the controller is running on Linux, you will need to add a second ``path:`` line to 
``comm-serial-bluecove.ext`` which should point to the Bluecove GPL jar.

Also, the user which runs the controller must
be in the ``lp`` Linux group to use bluetooth devices.


Now restart the Space Controller and the Bluetooth Communication support will be available.

XBee Communication
====================

XBee radios can be used to create wireless sensor meshes very easily. Interactive Spaces provides support for
communication with an XBee radio over a serial connection on the host controller.

Only the Series 2 XBee API protocol with escaped bytes is supported.

Ready to run examples for XBee communication are found in the ``examples/activity/comm/xbee`` folder in the Interactive
Spaces Workbench. You will need a USB board for the XBee radios, such as the SparkFun
XBee USB Explorer. If you want to run the full example, you will need two radios and two USB
boards.

USB Communication
=================

Interactive Spaces has limited support for working with native USB devices on the host controller.


