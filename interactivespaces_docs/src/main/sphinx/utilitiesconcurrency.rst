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

Container-Wide Threads
======================

Though it is not recommended, you can easily create container-wide threads through the use of the
``ScheduledExecutor``, which is available from the activity's Space Environment. You should only use
this container wide scheduler if you know what you are doing, as any threads started through this method are not
immediately stopped when your activity stops. This method is particularly useful if you are implementing
your own Interactive Spaces Services.

