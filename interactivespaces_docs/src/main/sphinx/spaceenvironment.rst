The Space Environment
*********************

The Space Environment is the access point for many of the core services 
needed in Interactive Spaces. It is available to Live Activities, Space Controllers,
and the Master.

The definitive documentation is found in the
:javadoc:`interactivespaces.system.InteractiveSpacesEnvironment` Javadoc.

Logging
=======

The Space Container gives access to logging at the container level. 
There you can find everything logged that happened in both the Master 
and in the Space Controller. This includes any logging done in the
Live Activities.

``spaceEnvironment.getLog()`` gives access to this log.


If the
root folder for your master is *interactivespaces*, then the container logs are found
in the folder

::

  interactivespaces/master/logs

If the
root folder for your controller is *interactivespaces*, then the container logs are found
in the folder

::

  interactivespaces/controller/logs


Activity logging should be done with the usual :ref:`activity logging <activity-logging-reference-label>`.


Getting Services
================

Services provide more advanced functionality to your Activities.
Services provide the ability to do such things as run scripts, 
schedule future events, send email, and receive email.

Services are obtained through the Service Registry, which is available
from the ``getServiceRegistry()`` call on the Space Environment.

If your Live Activity is written in Java, you can get the Service
Registry with the following code

.. code-block:: java

  getSpaceEnvironment().getServiceRegistry()

Once you have the Service Registry, you use the *getService()* method to
get an actual service. For instance, if you want the scripting service you
would use the following Java code

.. code-block:: java

  ScriptService scriptService = 
      getSpaceEnvironment().getServiceRegistry().getService("scripting");

A safer way to do it would be

.. code-block:: java

  ScriptService scriptService = 
      getSpaceEnvironment().getServiceRegistry().getService(ScriptService.SERVICE_NAME);
  
You can get more details about the Service Registry in the
:javadoc:`interactivespaces.service.ServiceRegistry` 
Javadoc.

You can read more about services in the :doc:`services` Chapter.

Thread Pools
------------

It is sometimes necessary to run several things at the same time and the
typical way to do that is with threads. However, threading in Interactive Spaces
can be a little tricky because you want the Master or Space Controller
to shut down when you want it shut down. If threads are not used properly,
your Master or Space Controller will not shut down because there are
threads still running.

Interactive Spaces maintains a thread pool which it maintains control
over. Al threads from this pool will be properly shut down when
Interactive Spaces shuts down and only threads from this thread pool
should be used. You should never create a thread on your own.

You can access the thread pool through the
``spaceEnvironment.getExecutorService()`` call.

If you need threads in your Activity and are using Supported Activities,
you should make use of the :ref:`Managed Commands <activity-supported-managed-commands>` functionality
which gives a per-Activity thread pool which will be properly shut down when
the Activity is cleaned up.
