Advanced Master Usage
********************

The `run` Folder
================

The folder where the Master is installed contains a subfolder named `run`.
This folder contains information about the running system and allows control of the
Master through files.

The PID File
------------

The `run` folder contains a file called `interactivespaces.pid`. This file gives the
Process ID (or PID) of the operating system process the Master is running under. The
file contains only the Process ID number with nothing else, including no end of line
characters.

This file should not exist when the Master starts. If the Master 
tries to start and this file exists, the Master will complain about the
existence of the file and shut itself down. This prevents the Master from
having multiple instances running at the same time from the same installation.

The PID file is deleted when the Master is cleanly shut down. Should the Master 
crash, the PID file will be left and must be deleted before the Master
can be started again.

The `control` Folder
--------------------

The `run` folder can contain a subfolder called `control`. This subfolder allows control
of the Master by files which are added to it.

Creating a file called `shutdown` in this folder will shut the master
down softly.

Creating a file called `space-controllers-shutdown-all` will shut down all Space Controllers
the master knows about.

Creating a file called `space-controllers-shutdown-all-activities` will shut down all Live 
Activities running on allSpace Controllers the master knows about.

Creating a file called `live-activity-group-startup-id` start up a Live Activity Group whose ID
is `id`. For example, `live-activity-group-startup-652` will start up Live
Activity Group `652`.

Creating a file called `live-activity-group-activate-id` start up a Live Activity Group whose ID
is `id`. For example, `live-activity-group-activate-652` will activate Live
Activity Group `652`.

Creating a file called `space-startup-id` start up a Space whose ID
is `id`. For example, `space-startup-652` will start up Space `652`.

Creating a file called `space-activate-id` start up a Space whose ID
is `id`. For example, `space-activate-652` will activate Space `652`.

The `control` folder can be created after the Master has been started.

This kind of control of the Master is useful for automated starts and stops
of the Master. A job scheduling system on the Master's host computer, for example
CRON on a Linux box, can start the Master up before it is needed. A second job 
could then write `shutdown` into the control when the master is no longer needed.

Space Controllers are not automatically shut down if the Master is shut down.
Stopping the Master while Space Controllers are running is currently not supported,
make sure you shut down all Space Controllers before shutting the Master down.
The SpaceOperations script helps with this task.

Automatic Activity Import
=========================

Activities can be autoimported in to the master.

To use this, you must create a folder called *master/import/activity* in the 
same folder where your Master is installed.

Now any Activity zip files which are placed in this folder will be auto-imported
into the master. If there is already an Activity with the same identifying name
and version, it will be replaced with this new Activity.

The Live Activities based on this Activity will not be immediately updated. You
must do that manually using the Master webapp or Master API.

Scripting the Master
====================

It is possible to write small scripts which can instruct the master to do a 
variety of tasks. The Interactive Spaces scripting service currently
supports Javascript, Python, and Groovy.

An Example Script
-----------------

The following is an example of a Python script which will get all Live
Activities that the master knows about and deploys them.

.. code-block:: python

  for liveActivity in activityRepository.allLiveActivities:
    print "%s: %s" % (liveActivity.id, liveActivity.name)
    uiControllerManager.deployLiveActivity(liveActivity.id)

Named Scripts
-------------

The Master also supports Named Scripts, which are scripts stored in the master database.
These scripts are run from the Master Webapp or the Master API. They can also be
scheduled to run via the scheduler.


Startup Master Extensions
---------

One way to script the Master is through the use of Startup Master Extensions.
These extensions are run after the master starts up.

During startup, the Master will look in the folder *extensions/startup*
in the same folder where your master is installed. These files will
then be run in sorted order alphabetically by name.

For instance, if the extensions folder contains *011-foo.groovy* and
*001-bar.py*. They will be run in the order

1. 001-bar.py
2. 011-foo.groovy

Any extensions added after the master is started will be run immediately.
They will then be run in the name sorted order next time the Master is started.

So say you add *05-banana.groovy* to the extensions folder. It will be
run immediately. But next time the master starts, the order will be


1. 001-bar.py
2. 005-banana.groovy
3. 011-foo.groovy

API Master Extensions
---------

API Master Extensions allow you to add special extensions to the Master WebSocket API.

The Master looks for API Extensions in the folder *extensions/api*
in the same folder where your master is installed. Extensions can be added to
this folder before the Master is started and while it is running.

For the first example, suppose you have the file *extensions/api/settings-get.groovy*,
which is a Groovy based script. You could call it with the following web socket call.

.. code-block:: javascript

  {command: '/extension/settings-get', args: {map: 'b'}}

The script in *extensions/api/settings-get.groovy* could be something like

.. code-block:: groovy

  def map = spaceEnvironment.getValue('master.settings.map')
  if (map) {
    [result: "success", data: map.getMap(args.map)]
  } else {
    [result: "failure", reason: "no map"]
  }

This script is written to get a *SimpleMapPersister* named *master.settings.map* in
the Space Environment. If the map is there, the Script returns the map with the
name *args.map*, which, in the example call given above, would have a
value of *b*. *args* is a map of arguments for the call.
The *b* map would then be sent over the web socket channel. If the persister doesn't
exist, a map giving a failure result would be returned.

Then suppose there was a script called *extensions/api/settings-put.groovy* which
is called with the following command

.. code-block:: javascript

  { command: '/extension/settings-put', args: {map: 'b', data: {e: 'f', g: 'h'}}}

with the script contents being

.. code-block:: groovy

  def map = spaceEnvironment.getValue('master.settings.map')
  if (map) {
    map.putMap(args.map, args.data)
  
    [result: "success"]
  } else {
    [result: "failure", reason: "no map"]
  }

Here we get the same persisted map from the previous example, map *b*, 
and put the data *{e: 'f', g: 'h'}* into the map.


System Objects Available
-------------------------

Functionality for controlling the master is found in a collection of
repositories which store the various entities the master understands,
and managers which can perform operations like deploying a Live Activity
or starting up a Live Activity Group.

The Scripting Service
~~~~~~~

The Scripting Service allows you to run scripts in the master in
a variety of languages.

The service will be called *scriptService* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.service.script.ScriptService` Javadoc.


The Scheduler Service
~~~~~~~

The Scheduler Service allows you to schedule tasks in the master.

The service will be called *schedulerService* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.service.scheduler.SchedulerService` Javadoc.


The Controller Repository
~~~~~~~

The Controller Repository contains all known space controllers.

The service will be called *controllerRepository* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.ControllerRepository` Javadoc.


The Activity Repository
~~~~~~~

The Activity Repository contains all known activities, live activities,
and live activity groups.

The service will be called *activityRepository* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.ActivityRepository` Javadoc.

The Space Repository
~~~~~~~

The Space Repository contains all known Spaces.

The service will be called *spaceRepository* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.SpaceRepository` Javadoc.

The Active Controller Manager
~~~~~~~

The Active Controller manager is used to control the Alive Activities
on a remote Space Controller.

The service will be called *activeControllerManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.ActiveControllerManager` Javadoc.

The UI Activity Manager
~~~~~~~

The UI Activity Manager is used to perform various operations on
Activities. It is a UI Manager as it only requires a few arguments, like
an Activity ID, rather than an actual domain object.

The service will be called *uiActivityManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.ui.UiActivityManager` Javadoc.


The UI Controller Manager
~~~~~~~

The UI Controller Manager is used to perform various operations on
Space Controllers, including the Live Activities they contain. It is a UI 
Manager as it only requires a few arguments, like a Space Controller ID or
a Live Activity ID, rather than an actual domain object.

The service will be called *uiControllerManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.ui.UiControllerManager` Javadoc.


The UI Master Support Manager
~~~~~~~

The UI Master Support Manager is used for advanced support of the manager. This
includes such operations as getting and importing a Master Domain model which
describes every aspect of the space.

The service will be called *uiMasterSupportManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.ui.UiMasterSupportManager` Javadoc.


The Interactive Spaces Environment
~~~~~~~

The Interactive Spaces Environment is a hook into the guts of Interactive Spaces
for the master. It gives access to logs, the container filesystem, and many
other aspects of the container.

The service will be called *spaceEnvironment* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.system.InteractiveSpacesEnvironment` Javadoc.

The Automation Manager
~~~~~~~~~~~~~~~~~~~~~~~

The Automation Manager is used for automating tasks within the Master. It gives
another way of accessing the scripting service and easily running a script in a
variety of languages.

The service will be called *automationManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.AutomationManager` Javadoc.

Moving Ports for the Master
===========================

Sometimes you might not be able to use the default ports that the Interactive Spaces
Master uses.

The Master contains a ROS master used by the core communication facilities provided
by Interactive Spaces. The file `config/container.conf` contains a line
like

::

  org.ros.master.uri=http://masterhost:11311/

where `masterhost` is the host name for the machine the Master is running on.
The port, here `11311`, can be changed on this line to any other port. For
example, if the ROS master should run on port `11312`, this line should become

::

  org.ros.master.uri=http://masterhost:11312/


The Master Web Application's port, `8080` by default, can be changed with the configuration
property `org.osgi.service.http.port`. This property is set in `config/container.conf`.

The Master uses an HTTP server for deploying Live Activities to their controllers. The controller
receives a URL for this server when the Master tells it a Live Activity is being deployed to
the controller. The port for this HTTP server can be changed with the
configuration property `interactivespaces.master.api.websocket.port`. The default value of
`10000` is used if this property doesn't exist. This configuration property should be set
in `config/interactivespaces/master.conf`.

Notification for Issues
=======================

The Space Controllers are constantly sending a heartbeat back to the Master so that the master
knows the Space Controllers are still connected and alive. If a Space Controller dies or loses
network connectivity, it is possible to receive an alert.

Email Alerts
------------

The only alert mechanism available out of the box is an email-based one.
The alert mechanism will send an email containing information about the alert
to a group of email addresses.

The email alert mechasigm is configured through the file `config/mail.conf`.
A sample file is given below.

::

  interactivespaces.mail.smtp.host=192.168.172.12
  interactivespaces.mail.smtp.port=25

  interactivespaces.service.alert.notifier.mail.to = person1@foo.com person2@foo.com
  interactivespaces.service.alert.notifier.mail.from = interactivespaces@foo.com
  interactivespaces.service.alert.notifier.mail.subject = Death, doom, and destruction in My Space

The property `interactivespaces.mail.smtp.host` specifies a host running an SMTP server which
will relay the alert. The` property `interactivespaces.mail.smtp.port` can
be used to specify the port this SMTP server is listening on.

The property `interactivespaces.service.alert.notifier.mail.to` specifies who should
receive the alert email. The recipient email addresses on this list are separated by
spaces or tabs, and there can be as many addresses as are needed.

The property `interactivespaces.service.alert.notifier.mail.from` specifies what the
From address of the email will be.

The property `interactivespaces.service.alert.notifier.mail.subject` gives
the Subject line the alert email will have.

A sample email, though the format is subject to change, for losing contact with a Space Controller
is

::

  No space controller heartbeat in 30881 milliseconds

  ID: 56
  UUID: 83aab854-ead1-482e-8ce5-0fcca7b508e8
  Name: The Living Room Controller
  HostId: livingroomcontroller











