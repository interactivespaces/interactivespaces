Scripting The Master
********************

It is possible to write small scripts which can instruct the master to do a 
variety of tasks. The Interactive Spaces scripting service currently
supports Javascript, Python, and Groovy.

An Example Script
=================

The following is an example of a Python script which will get all Live
Activities that the master knows about and deploys them.

.. code-block:: python

  for liveActivity in activityRepository.allLiveActivities:
    print "%s: %s" % (liveActivity.id, liveActivity.name)
    uiControllerManager.deployLiveActivity(liveActivity.id)


System Objects Available
========================

Functionality for controlling the master is found in a collection of
repositories which store the various entities the master understands,
and managers which can perform operations like deploying a live activity
or starting up a live activity group.

The Scripting Service
---------------------

The Scripting Service allows you to run scripts in the master in
a variety of languages.

The service will be called *scriptService* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.service.script.ScriptService` Javadoc.


The Scheduler Service
---------------------

The Scheduler Service allows you to schedule tasks in the master.

The service will be called *schedulerService* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.service.scheduler.SchedulerService` Javadoc.


The Controller Repository
---------------------

The Controller Repository contains all known space controllers.

The service will be called *controllerRepository* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.ControllerRepository` Javadoc.


The Activity Repository
-----------------------

The Activity Repository contains all known activities, live activities,
and live activity groups.

The service will be called *activityRepository* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.ActivityRepository` Javadoc.

The Space Repository
-----------------------

The Space Repository contains all known Spaces.

The service will be called *spaceRepository* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.SpaceRepository` Javadoc.

The Active Controller Manager
-----------------------------

The Active Controller manager is used to control the Alive Activities
on a remote Space Controller.

The service will be called *activeControllerManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.ActiveControllerManager` Javadoc.

The UI Activity Manager
-----------------------------

The UI Activity Manager is used to perform various operations on
Activities. It is a UI Manager as it only requires a few arguments, like
an Activity ID, rather than an actual domain object.

The service will be called *uiActivityManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.ui.UiActivityManager` Javadoc.

The service will be called *uiControllerManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.ui.UiControllerManager` Javadoc.

The service will be called *uiMasterSupportManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.ui.UiMasterSupportManager` Javadoc.

The service will be called *spaceEnvironment* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.system.InteractiveSpacesEnvironment` Javadoc.

The service will be called *automationManager* in your script.

You can find detailed documentation in the
:javadoc:`interactivespaces.master.server.services.AutomationManager` Javadoc.





