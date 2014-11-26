Native Applications
*******************

Sometimes you will find that you cannot get the functionality you want from a pure Interactive Spaces
activity. While it is recommended that you try to use a pure Interactive Spaces
activity so you can use all of the APIs available, you may need to go some other direction for a variety 
of reasons:

* some legacy software must be used
* there is a native application that provides the needed functionality, such as a graphics engine
* it is too hard or too complicated to implement the needed functionality in Java or another JVM language

Interactive Spaces provides support for easily running native applications. With the support supplied
you can

* start and stop applications
* calculate the command line parameters for the application startup
* get callbacks during specific lifecycle events
* control the operating system environment variables for the process
* log the output and error streams from the activity
* supply a ``RestartStrategy`` that specifies how to restart the application if it crashes

Communication With The Native Application
=========================================

Unless your application is only going to be started and stopped by Interactive Spaces and nothing else,
you will probably want to have the Interactive Spaces activity that is running the native application
be able to communicate with the application. You have a variety of communication technologies you can
use. Network protocols include

* TCP or UDP clients or servers
* web protocols, including web sockets
* Open Sound Control

For a complete list, see :doc:`Network Communication Services </servicesnetworkcommunication>`.

It is also be possible to write commands into a file in the filesystem and have the native application
periodically read the contents of the file.

Using The Native Application Runner
===================================


