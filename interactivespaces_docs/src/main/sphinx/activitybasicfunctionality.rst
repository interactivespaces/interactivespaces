Basic Activity Functionality
****************************

There is a basic set of functionality which is accessible from any
Activity no matter how it is implemented. This includes access to logging,
the state of the Activity, and the Space Controller that the Activity is running
in.

If you are using :doc:`Supported Activities <activitysupportedclasses>`
you can just use these methods. If you are implementing your own 
Activities completely from scratch, which you should rarely have to do, you will
have to implement all of them yourself.

The definitive documentation for activities is found in the
:javadoc:`interactivespaces.activity.Activity` Javadoc.

General Activity Information
============================

There are several methods in Activities which give information about
the Activity.

``getName()`` return the name of the Activity as set in the 
``activity.conf``.

``getUuid()`` returns the UUID of the Activity when it was added to a 
Live Activity. The UUID is assigned by Interactive Spaces when an
a Live Activity is created. The Master uses the UUID to control the 
Live Activity through the Space Controller.

The Activity Configuration
==========================

The Activity's configuration can be accessed with the 
``getConfiguration()`` method.

You can find out more details of an Activity's configuration in the
:doc:`activityconfigurations` chapter and in the
:javadoc:`interactivespaces.configuration.Configuration` Javadoc. 

.. _activity-logging-reference-label:

Activity Status
===============

There are several method which can be called to get or set the status of an
Activity. 

Getting the Activity Status
---------------------------

An Activity can check its own status by calling 
``getActivityStatus()``. This returns an ActivityStatus object
(:javadoc:`interactivespaces.activity.ActivityStatus` Javadoc)
which contains information about the current status of the Activity.

Is the Activity Activated?
--------------------------

Activities have a method which can say whether or not the Activity has
been activated.

``isActivated()`` returns ``true`` if the Activity is activated and
``false`` otherwise.

Setting the Activity Status
---------------------------

An Activity can change its status by calling ``setActivityStatus()``. 
This method takes an ActivityStatus object
(:javadoc:`interactivespaces.activity.ActivityStatus` Javadoc) as its
only argument.

There should be little reason for you to use this method if you are 
using :doc:`Supported Activities <activitysupportedclasses>`.

Logging
=======

There are per-Live Activity logs if you want to look at information
specific to only one particular Live Activity.

This log is accessed via the ``getLog()`` method of your Activity.

The per-Live activity logs are found in the Live Activity's folder on the
Space Controller. 

Suppose the UUID of your Live Activity (which you can find on the Live Activity's
page in the Interactive Spaces Master webapp) is 
*34eb3c27-5d37-45aa-a9cd-22d46bc85701*. The logs for that specific Live 
Activity would then be found in the folder

::

  interactivespaces/controller/controller/activities/installed/
      34eb3c27-5d37-45aa-a9cd-22d46bc85701/log

The configuration parameter *space.activity.log.level* lets you set the logging
level, which by default is set to *error*. The following parameter added to
your *activity.conf* will take the logging level up to *info*.

::

  space.activity.log.level=info

Legal values are

* *fatal* - Fatal events.
* *error* - Error and fatal events
* *info* - Info, error, and fatal events
* *debug* - Debug, info, error and fatal events
* *off* - No logging

The Activity Filesystem
========================

When a Live Activity is installed on a Space Controller, it is placed
in a directory with a number of subdirectories which contain a variety
of files needed for the Live Activity.

The Activity Filesystem is accessed with the ``getActivityFilesystem()``
call.

A Live Activities is installed in the

::

  controller/controller/activities/installed/uuid

folder, where ``uuid`` is the UUID of the Live Activity.

Each of the following directories are under this directory.

The Install Directory
-------------------------

The Install Directory is where the resources that were contained in
the Activity's install bundle are placed. This includes the 
``activity.conf`` and any code and resources necessary for the Activity
to run.

The Install Directory is in the ``install`` directory of the Activity's
fileystem.

This directory is accessed with the ``getInstallDirectory()`` method
on the Activity Filesystem.

.. code-block:: java

  File installDir = getActivityFilesystem().getInstallDirectory();

You can access a specific file in the install directory with the
``getInstallFile()`` method. Say, for example, you want to access the
file ``data.dat`` which is in the ``resource`` subdirectory of the
install directory.

.. code-block:: java

  File dataFile = getActivityFilesystem().getInstallFile("resource/data.dat");

.. _activity-filesystem-permanent-label:

The Permanent Data Directory
-------------------------

The Permanent Data Directory is where a Live Activity can place data it
wishes to keep around permanently. Interactive Spaces guarantees it
will not touch this folder unless the Live Activity is deleted from 
the controller.

The Permanent Data Directory is in the ``data`` directory of the Activity's
fileystem.


This directory is accessed with the ``getPermanentDataDirectory()`` method
on the Activity Filesystem.

.. code-block:: java

  File dataDir = getActivityFilesystem().getPermanentDataDirectory();

You can access a specific file in the permanent data directory with the
``getPermanentDataFile()`` method. Say, for example, you want to access the
file ``data.cache`` which is in the ``cache`` subdirectory of the
permanent data directory.

.. code-block:: java

  File dataFile = getActivityFilesystem().getPermanentDataFile("cache/data.cache");

The Temporary Data Directory
-------------------------

The Temporary Data Directory is where a Live Activity can place data it
wishes to keep around while it is running. Interactive Spaces only
guarantees that it won't delete this directory while a Live Activity is
running. Any data which needs to be kept between runs should be put
in the :ref:`activity-filesystem-permanent-label`.


The Temporary Data Directory is in the ``tmp`` directory of the Activity's
fileystem.

This directory is accessed with the ``getTempDataDirectory()`` method
on the Activity Filesystem.

.. code-block:: java

  File tmpDir = getActivityFilesystem().getTempDataDirectory();

You can access a specific file in the Temporary Data Directory with the
``getTempDataFile()`` method. Say, for example, you want to access the
file ``data.cache`` which is in the ``cache`` subdirectory of the
Temporary Data Directory.

.. code-block:: java

  File cacheFile = getActivityFilesystem().getTempDataFile("cache/data.cache");

The Log Directory
-------------------------

The Log Directory is where a Live Activity places its
:ref:`per-Activity logs <activity-logging-reference-label>`.

The Log Directory is in the ``log`` directory of the Activity's
fileystem.

This directory is accessed with the ``getLogDirectory()`` method
on the Activity Filesystem.

.. code-block:: java

  File logDir = getActivityFilesystem().getTLogDirectory();

The Space Controller
====================

The Activity can access the Space Controller which it is running under.

The Space Controller is accessed with the ``getController()`` call.

See the
:javadoc:`interactivespaces.controller.SpaceController` Javadoc for
details.

The Space Environment
=====================

The Space Environment gives access to many of the core Interactive Spaces
services. You can find more information in the chapter
:doc:`spaceenvironment`.

The Space Environment is accessed through the ``getSpaceEnvironment()``
call in an Activity.






