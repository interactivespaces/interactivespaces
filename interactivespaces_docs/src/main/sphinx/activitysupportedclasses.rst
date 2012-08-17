Supported Activity Classes
**************************

Implementing an Interactive Spaces Activity from scratch, even with the
vast number of support classes available, can be daunting. To help with
this, Interactive Spaces comes with a variety of Supported Activities which
can make implementing an Activity as simple as writing methods for only 
the functionality that you care about.

Common Functionality
====================

There is a set of functionality which is common to all of the Supported
Activity classes.

Common Event Methods
--------------------

There are various events which happen to an Activity, they can be started
up, shutdown, activated and deactivated. There are a series of event 
methods which are called during these various Activity events that you
can write your own versions of so that your Activity does what you
need it to do. You don't have to write versions of all of these methods,
but you will probably need to write some of them.

Sometimes multiple
event methods will be called for a single event so that the Activity can
respond properly to its internal state, so make sure you carefully read when
event methods are called.

.. _onActivitySetup-reference-label:

``void onActivitySetup()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

This method is called when Activity is starting up. 
This is the first event method called 
and is called only once. Very minimal things will be set up for the Activity.
The method should be used to set anything up that 
doesn't require access to Activity Components or any other configured 
items.

The following resources are available:

* the :doc:`space environment </spaceenvironment>`
* the Activity's initial configuration
* the Activity's log
* The Space Controller the Activity is running in

.. _onActivityStartup-reference-label:


``void onActivityStartup()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

This method is called once when the Activity is starting up.
It is called after 
The Activity is fully configured and all
Activity components, such as web servers and communication channels,
are running.

This method should throw an exception if it can't properly start.

It is called after
:ref:`onActivitySetup <onActivitySetup-reference-label>`.

.. _onActivityShutdown-reference-label:

``void onActivityShutdown()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

This method is called when the Activity is shutting down.
It should be
used to properly shut down anything that the Activity needed that wasn't
automatically supported (such as components).

This method should throw an exception if it can't shutdown.

There is another method called 
:ref:`onActivityCleanup <onActivityCleanup-reference-label>` which will
always be called whether the activity shuts down or crashes. It is called
after ``onActivityShutdown``, even if ``onActivityShutdown`` throws an
exception.

``void onActivityActivate()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

The Activity is being activated.

This method should throw an exception if the Activity can't activate.

``void onActivityDeactivate()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

The Activity is being deactivated.

This method should throw an exception if the Activity can't deactivate.

``void onActivityFailure()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

Something in the Activity has failed. This can be any installed
components or something the user has set up.

.. _onActivityCleanup-reference-label:

``void onActivityCleanup()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

The Activity has shut down either due to a shutdown or by activity
failure. It should clean up all resources used by the Activity.

It is called after 
:ref:`onActivityShutdown <onActivityshutdown-reference-label>` is called
during shutdown, or when the Activity crashes.

``boolean onActivityCheckState()``
~~~~~~~~~~~~~~~~~~~~~~~~~~

This method will be called when the activity state is being checked by
the controller.

This method should not change the activity state, it should just return
whether or not the activity is doing what it is supposed to in its
current state.

The method should return ``true`` if the Activity is working correctly, 
and ``false`` if it isn't.

``void onActivityConfigurationUpdate(Map<String, Object> update)``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A live configuration update is coming into the Activity.
 
The map gives the contents of the entire update.

The new configuration will also be properly reflected with the
``getConfiguration()`` method on the Activity.

.. _activity-supported-managed-commands:

Thread Pools and Managed Commands
---------------------------------

It is sometimes necessary to run several things at the same time in your Activities 
and the typical way to do that is with threads. However, threading in 
Interactive Spaces can be a little tricky because you want the Master or
Space Controller to shut down when you want it shut down. If threads are 
not used properly, your Master or Space Controller will not shut down 
because there are threads still running.

Managed Commands give you a per-Activity collection of threads which will
all be properly shut down when your Activity is cleaned up.

You can access the Managed Commands with the ``getManagedCommands()`` call
in your Activity.

To use the Managed Commands service, you can create a ``Runnable``
inside your  :ref:`onActivitySetup <onActivitySetup-reference-label>`
or :ref:`onActivityStartup <onActivityStartup-reference-label>`
and submit it to the Managed Commands.

.. code-block:: java

  public void onActivitySetup() {
    ... other setup...
    
    Runnable myTask = ...
    getManagedCommands().submit(myTask);
    
    ... other setup ...
  }

You are now done, you don't have to worry about shutting your task down,
Interactive Spaces will do it automatically when the Activity is
cleaned up.

For more details, see the 
:javadoc:`interactivespaces.util.concurrency.CommandCollection`
Javadoc.

Managed Resources
-----------------

Some of the provided Interactive Spaces functionality needs to be
started up and shut down because of how it works on the inside.
As an example, there is a support class for easily copying
resources needed for your Activity from an arbitrary URL. This complex
support class can work in the background and thus needs to be shutdown
when it is no longer used. Because it is a Managed Resource, however,
you don't need to remember to start it up or shut it down, it will
be taken care of for you automatically.

You tell your Activity about a Managed Resource with the
``addManagedResource()`` call.


.. code-block:: java

  public void onActivitySetup() {
    ... other setup...
    
    httpCopier = new HttpClientHttpContentCopier()
    addManagedResource(httpCopier);
    
    ... other setup ...
  }

You are now done, you don't have to worry about shutting the copier down,
Interactive Spaces will do it automatically when the Activity is
cleaned up.


 