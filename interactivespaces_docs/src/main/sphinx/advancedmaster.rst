Advanced Master Usage
********************

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

Master Extensions
---------

One way to script the Master is through the use of Master Extensions.
Extensions are run after the master starts up.

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

Named Scripts
-------------

The Master also supports Named Scripts, which are scripts stored in the master database.
These scripts are run from the Master Webapp or the Master API. They can also be
scheduled to run via the scheduler.

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





