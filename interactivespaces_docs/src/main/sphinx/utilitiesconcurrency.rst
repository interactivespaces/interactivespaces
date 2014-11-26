Interactive Spaces Concurrency Utilities
****************************************

A common need in Interactive Spaces activities is the need to do multiple operations at the same time or to do
certain operations in the background while waiting for events to happen.
Examples would be an animation thread that calculates animation frames and sends them to be displayed or
something that periodically samples a value to see if it has changed in an important way.

In Java, this sort of multiprocessing is commonly done with threads, either by creating threads directly
or by using an ``ExecutorService``. However, using threads in Java can be tricky. For example, unless all threads have
been properly terminated, your Java process cannot terminate. Since the Interactive Spaces controller
is a Java application, this would mean that the controller could not be shut down.

It is easy to forget to terminate your threads, so Interactive Spaces includes the concept of the
``ManagedCommand`` (this is different than a ``ManagedResource``) so you don't need to worry about thread termination. 
Managed Commands are started up in their own thread and properly shut down when your activity shuts down.

ManagedCommands
==============

The ``ManagedCommands`` object gives you the ability to create Managed Commands at any point in your activity's lifecycle.
Any Managed Commands added in the ``onActivitySetup()`` will not be started until just before ``onActivityStartup()``.
After this point Managed Commands will be started immediately when they are added to the
``ManagedCommands`` object. As stated before, all Managed Commands will
be automatically shut down when your activity shuts down, as part of ``onActivityCleanup()``, but you can also
shut them down at any time by yourself.

You can create a Managed Command by using the ``ManagedCommands`` object that every activity contains.
You can obtain the ``ManagedCommands`` object through the activity's ``getManagedCommands()`` method.

Suppose you want to run a background thread. You want the thread to run to completion and you don't
really care to know when it is done and you probably won't need to stop it. You can do this with the following
code

.. code-block:: java

  getManagedCommands().submit(new Runnable() {
    @Override
    public void run() {
      // code to run in separate thread
    }
  });

This example omits whatever code should run in this thread.

From now on, the term *command* will be used to describe the code that *threads* execute.


Now suppose you want to be able to tell if the Managed Command is still running. Now you need to keep the
``ManagedCommand`` object that the ``submit()`` method returns.

.. code-block:: java

  ManagedCommand command = getManagedCommands().submit(new Runnable() {
    @Override
    public void run() {
      // code to run in separate thread
    }
  });

The ``ManagedCommand`` object contains several methods of interest.

``cancel()`` will cancel the command. If the thread is blocked waiting for I/O or is asleep for some reason,
an ``InterruptedException`` will be thrown. If your code contains a loop, you should use the 
method ``Thread.interrupted()`` as part of your loop so that it can be terminated properly. For example:

.. code-block:: java

  ManagedCommand command = getManagedCommands().submit(new Runnable() {
    @Override
    public void run() {
      while (!Thread.interrupted()) {
        // the loop computation
      }
    }
  });

``isDone()`` will return ``false`` if the thread is still running, and ``true`` if it has completed,
either successfully or with an exception.

``isCancelled()`` will return ``true`` if the ``cancel()`` method was called, and ``false`` otherwise.

``ManagedCommands`` Methods
---------------------------

The ``ManagedCommands`` object has a variety of methods for creating threads. Some will just start a thread
and let it run. Others will run the thread on a periodic schedule.

All methods take at minimum an object whose class extends the Java interface ``Runnable``.

Methods that use the ``TimeUnit`` class can use the following values

* ``TimeUnit.DAYS``
* ``TimeUnit.HOURS``
* ``TimeUnit.MICROSECONDS``
* ``TimeUnit.MILLISECONDS`` 
* ``TimeUnit.MINUTES``
* ``TimeUnit.NANOSECONDS``
* ``TimeUnit.SECONDS``

``submit()``
^^^^^^^^^^^^

The ``submit()`` method will run the command once through to completion. The command will start immediately.

.. code-block:: java

  getManagedCommands().submit(command);

``schedule()``
^^^^^^^^^^^^^^

The ``schedule()`` method schedules the command to run at some point in the future. The command
will be run only once.

The method has the arguments ``schedule(Runnable command, long delay, TimeUnit unit)``.

The ``delay`` argument says how long to wait before the command is started. The ``unit`` argument
gives the time units. For example, the following code snippet will run your command 10 seconds in
the future.

.. code-block:: java

  getManagedCommands().schedule(command, 10, TimeUnit.SECONDS);

``scheduleAtFixedRate()``
^^^^^^^^^^^^^^^^^^^^^^^^^

``scheduleAtFixedRate()`` executes a periodic command that starts after the given initial delay, and 
repeatedly starts the command again at a fixed interval.  You can end up with multiple instances of the command
running simultaneously if a given instance does not complete before the next one is scheduled to begin.
By default, the command will stop repeating if any execution of the task throws an exception. 
The task will also terminate if you call ``cancel()`` on the returned ``ManagedCommand`` object or
when your activity shuts down.

One way of calling the method has the arguments 
``scheduleAtFixedRate(Runnable command, long initialDelay, EventFrequency commandFrequency)``.

``commandFrequency`` specifies how long
to wait before repeating the command. It provides the period and the time units for the period. 

The schedule for running the command will be

* ``0``
* ``period``
* ``2 * period``
* ``3 * period``
* ...

Here ``0`` means that the command will start immediately.

For example, the following code snippet will repeat your command at 30 calls per second.


.. code-block:: java

  getManagedCommands().scheduleAtFixedRate(command, EventFrequency.eventsPerSecond(30.0));

Another way to call the method has the arguments 
``scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)``.

``initialDelay`` says how far in the future to start running the command. ``period`` gives how long
to wait before repeating the command. ``unit`` specifies the time units for both the initial delay
and the period. The schedule for running the command will be

* ``initialDelay``
* ``initialDelay + period``
* ``initialDelay + 2 * period``
* ``initialDelay + 3 * period``
* ...

For example, the following code snippet will start repeating your command in 5 minutes and repeat once
a minute after that.


.. code-block:: java

  getManagedCommands().scheduleAtFixedRate(command, 5, 1, TimeUnit.MINUTES);

There is an extra argument for both method variations that allows you to determine what happens if an 
exception is thrown while your command is running. This version of the method has the arguments 
``scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit, boolean allowTerminate)``.

If ``allowTerminate`` is ``false``, the command will continue running but the exception will be logged.
If ``allowTerminate`` is ``true``, the command will stop repeating, and the exception will be logged.

``scheduleWithFixedDelay()``
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``scheduleWithFixedDelay()`` executes a periodic command that starts after the given initial delay, 
and subsequently with the given delay between the completion of one execution and the starting of 
the next. Only one instance of this command can be active at a time. By default,the command will stop 
repeating if any execution of the task encounters an
exception. The task will also terminate if you call ``cancel()`` on the returned ``ManagedCommand`` 
object or when your activity shuts down.

One way to call the method has the arguments 
``scheduleWithFixedDelay(Runnable command, EventDelay commandDelay)``.

``commandDelay`` specifies how long
to delay before repeating the command. It provides the period and the time units for the delay.

For example, the following code snippet will repeat your command with a delay of 1 minute between runs.


.. code-block:: java

  getManagedCommands().scheduleWithFixedDelay(command, EventDelay.minutes(1.0));

Another way to call the method has the arguments 
``scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)``.

``initialDelay`` says how far in the future to start running the command. ``delay`` gives how long
to wait before repeating the command. ``unit`` specifies the time units for both the initial delay
and the delay.

For example, the following code snippet will start repeating you command in 5 minutes and repeat once
a minute after that.


.. code-block:: java

  getManagedCommands().scheduleWithFixedDelay(command, 5, 1, TimeUnit.MINUTES);

There is an extra argument for both method variations that allows you to determine what happens if an 
exception is thrown while
your command is running. This version of the method has the arguments 
``scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit, boolean allowTerminate)``.

If ``allowTerminate`` is ``false``, the command will continue running but the exception will be logged.
If ``allowTerminate`` is ``true``, the command will stop repeating, and the exception will be logged.

Container-Wide Threads
======================

Though it is not recommended, you can easily create container-wide threads through the use of the
``ScheduledExecutor``, which is available from the activity's Space Environment. You should only use
this container wide scheduler if you know what you are doing, as any threads started through this method are not
immediately stopped when your activity stops. This method is particularly useful if you are implementing
your own Interactive Spaces Services.

The methods for the Space Environment's Executor are the same as the ``ManagedCommands`` class except for the
``allowTerminate`` argument, which does not exist for the Space Environment's Executor.

