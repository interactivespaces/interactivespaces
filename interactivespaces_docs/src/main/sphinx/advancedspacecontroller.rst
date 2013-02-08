Advanced Space Controller Usage
********************

The `run` Folder
================

The folder where the Space Controller is installed contains a subfolder named `run`.
This folder contains information about the running system and allows control of the
Space Controller through files.

The PID File
------------

The `run` folder contains a file called `interactivespaces.pid`. This file gives the
Process ID (or PID) of the operating system process the controller is running under. The
file contains only the Process ID number with nothing else, including no end of line
characters.

This file should not exist when the Space Controller starts. If the Space Controller 
tries to start and this file exists, the Space Controller will complain about the
existence of the file and shut itself down. This prevents the Space Controller from
having multiple instances running at the same time from the same installation. If two Space
Controllers are needed on the same host computer, two separate installs must be done
on that Host Computer and each Space Controller must have a different Host ID and UUID.

The PID file is deleted when the Space Controller is cleanly shut down. Should the Space 
Controller crash, the PID file will be left and must be deleted before the pace Controller
can be started again.

The `control` Folder
--------------------

The `run` folder can contain a subfolder called `control`. This subfolder allows control
of the Space Controller by files which are added to it.

For example, creating a file called `shutdown` in this folder will shut the controller
down softly. All running Live Activities will be cleanly shutdown and then the entire Space 
Controller will exit.

The `control` folder can be created after the Space Controller has been started.

