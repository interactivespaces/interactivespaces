Interactive Spaces Startup
**************************

Interactive Spaces has a somewhat involved startup process to make sure everything works properly.
Understanding this startup process can help when you are trying to figure out how to implement something
or why your activity doesn't work. This is a rather advanced section, so do be aware that it may require reading
over a few times with an installation of Interactive Spaces so you can see everything that is being discussed.

In the following discussion, **container** will refer to either an Interactive Spaces Master or an
Interactive Spaces Space Controller.

Starting Up a Container
=======================

The best way to start a container is to use the ``bin/startup_linux.bash`` command found in the installation
directory for the container. While the command does have ``linux`` in its name, it will work for any operating
system that supports the ``bash`` shell, which includes Linux and OSX.

The ``startup_linux.bash`` script starts by figuring out in exactly which directory the container is installed.
It then changes the directory for the operating system process which is running the container to that installation
directory.

By default, a container will start up in a ``foreground`` mode, a normal blocking terminal process. If
``background`` is specified as the first argument to ``startup_linux.bash``, then it will run in a sub-process
with detached input/output pipes.

Setting up the Java Process Environment
---------------------------------------

Next the startup script will execute the bash file ``.interactivespaces.rc`` in the home directory of the user
that is starting the container.  This file can set any
environment variables for the operating system process that will run the container and can be used, for example,
to set the ``DISPLAY`` variable to be used for specifying which display the process should use when the container
opens any windows, such as a web browser. Additionally, this script can modify ``INTERACTIVESPACES_MAIN_ARGS``
variable to add additional container arguments.

Next the startup script executes the file ``config/environment/localenvironment.rc``. This file can set any
environment variables for the operation system process that will run the container. 

An environment variable 
often set by ``localenvironment.rc`` is ``CLASSPATH_ADDITIONAL`` which adds any additional Java jars that must 
be on the bootstrap classloader for the Java process in which the container runs for the code contained within
to work properly. Jars that fit this category include jars that contain code that attempt to load a native
library from the jar.

Another environment variable 
that is set by ``localenvironment.rc`` for Linux systems is ``LD_LIBRARY_PATH``, which tells the 
Java process where to find various 
native libraries that are also needed by the running Java process. An example is where the
OpenCV libraries are installed for vision processing. The equivalent environment variable for OSX is 
``DYLD_LIBRARY_PATH``.

The startup script then looks for the file ``config/environment/container.args``. This file gives any extra
command line flags that should be handed to the JVM command line that will start the Java process. For
example this
could be used to set debugging ports so that you can debug your programs with a Java debugger. Another important
use is setting ``-Djava.library.path``, which tells the JVM where to find native libraries.

As an aside, the ``LD_LIBRARY_PATH`` is the basis for ``-Djava.library.path``, so using either is fine.

The ``INTERACTIVESPACES_MAIN_ARGS`` environment variable can be used to pass command line arguments to the
underlying container launcher. Any command line arguments to ``startup_linux.bash`` (after the initial
``background``, if any) will be appended to the environment variable and passed to the container.
See :ref:`command-line-arguments` for more details on the command line arguments that can be used.

.. _command-line-arguments:

Container Command Line Arguments
--------------------------------

There are several command-line arguments that can be passed to a container:

* ``--noshell`` start the container without an interactive OSGi shell.
* ``-Dx=y`` defines a container system configuration parameter ``x`` to have the value ``y``.

Preparing to start the OSGi Container
-------------------------------------

The next step is to start up the OSGi container itself. A small Java class called the **launcher** is used to 
start the OSGi container (the term **OSGi container** is why the Master and Controller are called containers). 

The launcher first creates the ``run/interactivespaces.pid`` file. This file contains the operating system 
process ID for the Java process on operating systems that have process IDs. The launcher then places a file lock
on this file. If any other launcher tries to start, it will fail to obtain this file lock and it will stop
launching the container. This means you can only run one container from a given directory at a time.

For the moment, operating systems that do not use process IDs will get a process ID of ``0``. Operating systems
that do not support file locks are not supported.

The launcher's next task is to build the classpath that will be used for starting the OSGi
container. Since the classpath for the OSGi container is built dynamically
by the launcher, it does not have to be a fixed path specified in
``startup_linux.bash``.

The launcher's first step for building the OSGi classpath is to take each file found in the 
``lib/system/java`` folder and place it on the OSGi classpath. 

The launcher then scans the ``config/environment`` folder for any files ending in ``.ext``. These
are **extension** files that can modify the OSGi runtime environment. These files describe jars and classes
that are not available as OSGi bundles or cannot work as OSGi bundles and so must be on the same classloader 
as the OSGi container. Examples of extension files can be found in the ``extras`` folder for the Interactive
Spaces Space Controller.

Why is it useful to have both extension files and the ``CLASSPATH_ADDITIONAL`` environment variable? 
The extension files give a dynamic
and easy way of extending the classes available for Interactive Spaces and are the preferred method. The files
just need to be placed in the ``config/environment`` folder and specify everything the OSGi container needs 
to do
for the extension to run. But sometimes the bootstrap classloader is the only way to make things work, for example
JNA, and so  the ``CLASSPATH_ADDITIONAL`` functionality was added. See :ref:`so-how-do-i` for how to think
about this issue.

For each extension file found by the launcher, it looks for any lines that
start with ``path:``. The content of the rest of the line is expected to be a file system pathname to a jar
file that will be added to the OSGi classpath. An extension file can contain multiple ``path:`` lines and
each will be placed on the OSGi classpath. Relative pathnames are relative to the base folder the container is
installed in.

Once the OSGi classpath is formed, the **launcher bootstrap** takes over. This creates an instance of the OSGi
container with a classloader based on the OSGi classpath.

Exporting Java Packages from the OSGi System Bundle
---------------------------------------------------

Bundles in OSGi have to state what Java packages are exported from that bundle. Anything not explicitly listed as
exported cannot be seen by any other bundle in the OSGi environment. This is what makes OSGi able to run
multiple versions of libraries simultaneously in the same JVM.

Packages in the ``java`` root package, like ``java.collection`` are automatically exported by the OSGi
system bundle, otherwise they would not be visible to any other bundles in the OSGi container. However, these
are the only classes that are automatically exported from the OSGi system bundle
and Java bootstrap classpaths. It is necessary to 
explicitly export classes in, for example, the ``javax`` root package.

One source of packages to be exported by the root bundle is found in ``lib/system/java/delegations.conf``.
Every line of this file is expected to be a Java package to be exported by the OSGi system bundle. This
particular file is used to export packages found in the Java JRE/JDK and includes things like Java sound
and graphics APis.
 
The other location for finding packages to export from the OSGi system bundle is within the extension files
mentioned above. The launcher bootstrap once again scans all those extension files in ``config/environment``
and looks for all lines starting with ``package:``. The rest of the line is added as one of the packages
to be exported by the OSGi system bundle.

Loading Native Libraries and Classes
------------------------------------

It is sometimes necessary to have the OSGi classloader load particular classes to make sure they are 
initialized properly. Also, it is sometimes necessary to call the Java method ``System.loadLibrary()``
so that the libraries are made available for the classes being loaded by the OSGi classloader or because they
need to be used later on. An example that requires this is the OpenCV Java native library. Unfortunately there are no hard, fast
rules when initial class loading is necessary and, for the moment, there seems to be only experimentation to 
discover when it is necessary. Library loading is necessary if you know that ``System.loadLibrary()`` 
is needed to make the library available.

This information is found inside the extension files. 

Lines starting with ``loadclass:`` specify classes that should be loaded by the ``loadClass()`` method
of the OSGi classloader. The rest of the line should contain the fully qualified classname of the class to
be loaded.

Lines starting with ``loadlibrary:`` specify libraries which should be loaded by ``System.loadLibrary()``.
The rest of the line should contain the path to the libary to be loaded. Relative pathnames are relative
to the base folder of the container being started.

Starting the OSGi Container
---------------------------

Finally, it is time to start the OSGi container!

As the OSGi container starts, it first loads all OSGi bundles from the ``bootstrap`` folder. This folder
contains what is considered the core functionality for the Interactive Spaces container. It will also at times
contain OSGi bundles copied from services and extensions found in the ``extras`` folder.

You should not place your own files in the ``bootstrap`` folder. If you have bundles for functionality of your
own or things which are not part of core Interactive Spaces, you should place them in the ``startup`` folder.
The contents of this folder are expected to be OSGi bundles and are also loaded by the container as OSGi
starts up.

Normally, the Interactive Spaces Controller container will launch and start a Standard Space Controller instance.
However, this behavior can be controlled with the ``interactivespaces.controller.mode`` variable, and setting it
to something other than ``standard`` will result in no default controller instance being started, e.g., by specifying
``-Dinteractivespaces.controller.mode=none`` on the ``startup_linux.bash`` command line.

Core Services
-------------

There are some core services that the launcher bootstrap supplies to the OSGi service registry.

One is the logging provider, which provides the container logger and the factory for activity logs. You can
set the properties of this logger in ``lib/system/java/log4j.properties`` for non-mobile containers. It
connects to the Android logger for Android devices.

Another service is the configuration provider, which provides access to the initial configuration for the container.
On non-mobile devices this reads the ``config/container.conf`` and on Android looks at the configuration
provider for the Interactive Spaces Android activity (Android calls applications *activities* as well, which
which can be a tad confusing when discussing both Android and Interactive Spaces in the same conversation).

A final core service is the container customization provider, which can provide things like the Android service
for the Android controller. Because of the way Android works, this service has to be created by the Android bootstrap.

.. _so-how-do-i:

So How Do I...
==============

If you want to make a Java package available for use in your Interactive Spaces activities and libraries,
first see if you can find it as an OSGi bundle. This is the easiest way to get new functionality into
Interactive Spaces.
If you find the OSGi bundle, then place it in the container's ``startup`` folder and you are done.

If the Java jar is not available as an OSGi bundle, you can always use the Interactive Spaces workbench
to make a Java jar file into an OSGi bundle. Once you do this, drop it into the ``startup`` folder.

Some Java libraries do not use the Java classloader properly and can't be made to work as OSGi bundles.
It is not easy to tell which Java libraries won't work as OSGi bundles, you have to either try
it or find someone on the web who says that the library will not work as an OSGi bundle. If
that is the case, then you will have to have the jars
for the library on the OSGi classpath. To do this, create an extension file.
As stated above, this is a file whose file extension is ``.ext``. For examples, look in the ``extras`` folder
of an Interactive Spaces Space Controller.

The extension file should contain lines starting with ``path:``. After the ``path:`` keyword should be
a file system path to the JAR file to be added to the OSGi classpath. Relative paths are resolved relative to
the Interactive Spaces container.

Lines starting with ``package:`` in the extension file list the Java packages that you want to be able
to use from OSGi.

If you need to load native libraries, use ``loadlibrary:`` lines. If you aren't using native libraries you
won't need this. After the ``loadlibrary:`` keyword should be
a file system path to the native library. Relative paths are resolved relative to
the Interactive Spaces container. You should only use this technique if you can't make things work any 
other way.

If you need to have classes loaded by the OSGi classloader during startup, use ``loadclass:`` lines. This is 
not needed very often, but it has been useful in some instances. You should only use this technique if you 
can't make things work any other way.

Now drop that extension file in ``config/environment`` and test.

Sigh! Making that extension file by itself didn't work. Keep trying for a little while.
Make sure all classes that need to be exported are handled with ``package:``. check whether you should have
used ``loadclass:``. Perhaps you needed ``loadlibrary:``. But you may find it 
just never works. In the case of Interactive Spaces, the ``bridJ`` library could not be made to work this way.
Then your final choice is to use the Java process bootstrap classloader. Edit the 
``config/environment/localenvironment.rc`` file, or create it if it doesn't exist. Add your jar to the
``CLASSPATH_ADDITIONAL`` environment variable, or create it if it doesn't exist. Jars in this environment
variable are separated by ``:``.

You can also add native
libraries to ``LD_LIBRARY_PATH``, or ``DYLD_LIBRARY_PATH`` on OSX in ``config/environment/localenvironment.rc``,
or add them to ``-Djava.library.path`` in ``config/environment/container.args``.

Your extension file in this case should probably not contain any ``path:`` lines unless multiple jar files
are needed and some of them can be loaded through the OSGi classpath.
