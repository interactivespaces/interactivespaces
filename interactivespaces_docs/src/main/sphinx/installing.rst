Installing Interactive Spaces
*****************************

Prerequisites
=============

Before you can install Interactive Spaces on your computer, you should make sure you have Java installed first. 

Interactive Spaces requires at least Java 1.6.

If you want to be able to easily build activities, you should install the Ant 
building tool. This is going away for non-Java activities and possibly even 
for Java activities.

Installing a Local Master
=========================

Installing a master is pretty easy. You will run an installer activity, and finally test your installation.

.. _installing-the-master:

Installing the Master
---------------------

If you are using a windowing system, find the icon for the Interactive Spaces Master installer and double click on it.

If you are using a command line interface for your operating system, use the command

::

  java -jar interactivespaces-master-installer-0.0.0-SNAPSHOT-standard.jar


where 0.0.0 is the version of the Interactive Spaces Master you are installing.

For now just accept all of the default settings by clicking Next on the configuration page. 

Testing the Master Installation
-------------------------------

To test if your installation happened correctly, open up a command shell and go to the directory where you installed the master. Once there, type the command 

::

  bin/startup_linux.bash

You should see a bunch of text scroll by as the master starts up. When you see no more text going by, go to a web browser and go to

::

  http://localhost:8080/interactivespaces

If everything installed correctly you should be seeing the Master Web Interface in your browser.

Installing a Local Controller
=============================

Installing a controller is pretty easy if you chose to let the controller autoconfigure itself. You will run an installer activity, and finally test your installation.

You can also manually configure the controller, though there usually isn't a good reason for this.

Installing the Controller
-------------------------

If you are using a windowing system, find the icon for the Interactive Spaces Controller installer and double click on it.

If you are using a command line interface for your operating system, use the command

::

  java -jar interactivespaces-controller-installer-0.0.0-SNAPSHOT-standard.jar

where 0.0.0 is the version of the Interactive Spaces Controller you are installing.

If you are auto-configuring the controller, make sure you don't check the manual configuration checkbox.

Testing the Controller Installation 
-----------------------------------

To test if your installation happened correctly, first make sure you have a Master started. Then 
open up a command shell and go to the directory where you installed the controller. 
Once there, type the command 

::

  bin/startup_linux.bash

You should see a bunch of text scroll by as the controller starts up. 

Go to the Master Web Interface in your browser. The URL is

::

  http://localhost:8080/interactivespaces

Go to the Controller menu. You should see an entry with the name of the controller you created. 
Click on this and click Connect. If everything is working you should see

::

  New subscriber for controller status

appear in the controller window. Also, if you refresh the controller page in the Master Web 
Interface you should see it say that the controller is in the running state.

Manually Configuring a Controller
---------------------------------

Before you install a manually configured controller, you need a UUID for the controller. 

You can get this by creating a new controller in the Master webapp. Click on the 
**Space Controller** menu, 
then *New*. Decide on a Host ID for the controller. The Controller Name you use is only for the 
master, pick something descriptive for the controller. Then click *Save*. The master will create a 
UUID for the controller and display it in the next screen. 

You will enter both the controller Host ID and UUID during the controller installation when 
prompted. Be sure to chose the manually configured option during installation.

Installing the Workbench
========================

The Interactive Spaces Workbench provides you with example code, documentation, and the 
Workbench application which can help you maintain and deploy your activities.


If you are using a windowing system, find the icon for the Interactive Spaces Controller 
installer and double click on it.

If you are using a command line interface for your operating system, use the command

::

  java -jar interactivespaces-workbench-installer-0.0.0-SNAPSHOT-standard.jar

where 0.0.0 is the version of the Interactive Spaces Workbench you are installing.

