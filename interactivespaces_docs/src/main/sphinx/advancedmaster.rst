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

Here we get the same persisted map and put the data from the call into the map,
with the example call above map *b* will get the data *{e: 'f', g: 'h'}*.


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





