Activity Types
**************

Activity Types make it easy for you to implement an Activity with mostly
configuration parameters and only as much code as is necessary to add in the
behavior you want.

Web Activity
============

The simplest Live Activity at all is a *Web* Activity. Web Activities will
automatically start up a web browser and web server when the Live Activity
starts up. The web server will have content supplied with the Activity
and the browser will start up pointing at that content.

Web Activity Configuration
--------------------------

The configuration for a web Activity is pretty simple, the simplest
``activity.conf``  is given below.

::

  space.activity.type=web
  space.activity.name=webExample

  space.activity.webapp.content.location=webapp
  space.activity.webapp.url.initial=index.html

The configuration parameter ``space.activity.type`` gives the Activity Type, which
here is ``web``.

The configuration parameter ``space.activity.webapp.content.location`` gives
the location of the content to be served. Here the folder it will use 
(which will contain the HTML, CSS, images, Javascript, and other web resources)
is relative to to where your Live Activity is deployed. In the example
above, this is a subfolder called ``webapp``.

The configuration parameter ``space.activity.webapp.url.initial`` gives the initial
webpage that will be shown in the browser. In the example here, the initial
URL is ``index.html``, so the browser URL will be
``http://localhost:9000/webExample/index.html``.

Notice that ``webExample`` is part of the URL. The name of the Activity is used
as the initial part of the URL. The Activity name is set with the required
configuration parameter ``space.activity.name``.

The file ``index.html`` will be found in the location

``live activity install dir/webapp/index.html``

where ``live activity install dir`` is where the Live Activity is installed
on the Space Controller.

You may notice that the web server port is ``9000``. This is the default server port
for the web server. If you want to set it to another value, say for example
you need to run multiple web servers on the same machine, or port 9000 is
being used for something else on the computer, you can change it with the
configuration parameter ``space.activity.webapp.web.server.port``

For example, if you have

::

  space.activity.webapp.web.server.port=9091

in your ``activity.conf``, the initial URL will be 
``http://localhost:9091/webExample/index.html``.

Sometimes there may be a need for some query string parameters on that initial
URL. A query string parameter list can be created using the
configuration parameter ``space.activity.webapp.url.query_string``. For example,
the setting

::

  space.activity.webapp.url.query_string=foo=bar

would make the initial URL be 
``http://localhost:9091/webExample/index.html?foo=bar``.

The query string parameters are best used when created from combinations of 
other configuration parameters. for instance, if your ``activity.conf`` included
the following parameter


::

  space.activity.webapp.url.query_string=foo=${a}&bar=${c}
  a=b
  c=d

the initial URL would be 
``http://localhost:9091/webExample/index.html?foo=a&bar=b``.

The final configuration parameter says whether or not the browser should be
in debug mode or not. The default value is to not be in debug mode and the
browser will be started in kiosk mode if that is possible. In debug mode,
the browser will be opened as normal so that the browser's debugger can
be used to debug your Activity.

The configuration parameter which sets whether or not the browser is started in debug
mode is ``space.activity.webapp.browser.debug``. It should be given a value
of ``true`` or ``false``. An example would be

::

  space.activity.webapp.browser.debug=true
  
Multiple Browsers
-----------------

Interactive Spaces starts every browser instance with its own profile. This
means that you can start up the browser-based Live Activities on the same
machine where you have your normal browser open and they won't affect
each other.

Native Activity
===============

Native Activities give you the ability to run native programs on your computer.
Native programs could be ones that came with the operating system the
computer runs, or a C++ activity that you write in a framework like
openFrameworks. Pretty much it can be any program that you can start from
the command line of your operating system's shell.


Native Activity Configuration
-----------------------------

A pretty simple ``activity.conf`` for a Native Activity is given below.

::

  space.activity.type=native
  space.activity.name=nativeExample

  space.activity.executable.linux=my_mp3_player
  space.activity.executable.flags.linux=-q ${activity.installdir}/NativeActivityExample.mp3

Here you can see that the Activity Type is ``native``. Notice there is also
the other required configuration parameter ``space.activity.name``.

The next two lines give the executable to run and any command line flags
that the executable might need.

The first one gives the executable with the configuraton parameter
``space.activity.executable.linux``. Here it has the value ``my_mp3_player``.
If you were running your Live Activity on a Linux box, Interactive Spaces
would start up the program

``live activity install dir/my_mp3_player``

where ``live activity install dir`` is where the Live Activity is installed
on the Space Controller.

Notice the ``.linux`` on the end of the configuration parameter name. This specifies which
operating system this particular executable is for. his way you can create
a Universal Activity which contains executables for any operating system
the Activity might run on. Legal values for the moment are

* linux - A Linux computer
* osx - A Mac OSX computer
* windows - a Windows conputer


As an example, the ``activity.conf`` might contain

::

  space.activity.executable.linux=my_linux_mp3_player
  space.activity.executable.osx=my_osx_mp3_player
  space.activity.executable.windows=my_windows_mp3_player

This would mean the Activity would contain the 3 executables

* my_linux_mp3_player
* my_osx_mp3_player
* my_windows_mp3_player

and Interactive Spaces will pick the correct executable based on the OS the
Activity is running on.

Often there may be a need for command line arguments, for instance, the
mp3 player needs to know which song to play. In the example above, the
configuration parameter ``space.activity.executable.flags.linux`` gives the
command line flags when the Linux executable is being used.

The value you see

::

  space.activity.executable.flags.linux=-q ${activity.installdir}/NativeActivityExample.mp3

gives the command line flags to play a file which is in the Live Activity's
install directory on its Space Controller.

``live activity install dir/NativeActivityExample.mp3``

where ``live activity install dir`` is where the Live Activity is installed
on the Space Controller.

The executable can also be somewhere else on the machine the Activity is running
on. For example, the ``activity.conf`` below uses the program ``/usr/bin/mpg321``
to play the MP3 file that comes with the Activity.

::

  space.activity.type=NATIVE
  space.activity.name=nativeExample

  space.activity.executable.linux=/usr/bin/mpg321
  space.activity.executable.flags.linux=-q ${activity.installdir}/NativeActivityExample.mp3

Native Activities Automatic Keep Alive
--------------------------------------

Every once in a while, a native activity may crash. Interactive Spaces
tries to keep things alive, and this is particularly true for native
activities. If, for instance, you shut the web browser down or otherwise kill
it, you will notice it starts up again for some limited number of times.

Scripted Activity
=================

A lot of people feel uncomfortable programming in Java. Programming in Java
gives the most direct access to the power of Interactive Spaces,
but Scripted Activities do have a lot of advantages. You can edit them
directly from their installation folder, which helps a lot when you are
writing your Activity in the first place.

Interactive Spaces supports writing Activities in Javascript and Python, with
more languages coming soon.

Scripted Activity Configuration
-------------------------------

A simple ``activity.conf`` for a Scripted Activity is given below.

::

  space.activity.type=script
  space.activity.name=activityPythonScriptExample

  space.activity.executable=ExamplePythonActivity.py

Notice that the Activity Type is ``script``.

The important configuration parameter here is ``space.activity.executable``
which gives the Activity executable. Here it has the value ``ExamplePythonActivity.py``.
Interactive Spaces uses the file extension to determine the scripting language
being used.

The guaranteed extensions are

+------------+------------+
| Language   | Extensions |
+------------+------------+
| Javascript | js         |
+------------+------------+
| Python     | py         |
+------------+------------+

Scripting Paths
---------------

Scripted Activities can use more than 1 scripting file for their implementation.
Interactive Spaces supports 2 places for scripting libraries to be placed,
one at the Space Controller-wide level, and one at the per-Live Activity
level.

Container Wide Paths
~~~~~~~~~~~~~~~~~~~~

The Space Controller-wide scripting library path is in the 
``interactivespaces/controller/lib`` folder. For example, 
``interactivespaces/controller/lib/python`` contains the Python libraries
which can be used by every Python script in Interactive Spaces. 

``interactivespaces/controller/lib/python/PyLib``
contains the Python system libraries. 


``interactivespaces/controller/lib/python/site``
is where you should put any of the libraries you want to include. Every
directory in the ``site`` directory is automatically added to the Python
path.

per-Activity Paths
~~~~~~~~~~~~~~~~~~

Any files found in the subdirectory ``lib/python`` in the 
Live Activity's install folder will also be added to the Python path.
For example, suppose the UUID of your Live Activity (which you can find 
on the Live Activity's page in the Interactive Spaces Master webapp) is 
``34eb3c27-5d37-45aa-a9cd-22d46bc85701``. The per-Live Activity Python lib path 
for that specific Live Activity would then be found in the folder

::

  interactivespaces/controller/controller/activities/installed/
      34eb3c27-5d37-45aa-a9cd-22d46bc85701/install/lib/python


Interactive Spaces Native Activities
====================================

Interactive Spaces Native Activities (not to be confused with Native
Activities) are Activities written in Java that have direct access to all
of Interactive spaces services. This is true of some of the scripting languages
as well, but Interactive Spaces Native Activities guarantee access to everything.

Interactive Spaces Native Activity Configuration
------------------------------------------------

A simple ``activity.conf`` for a Interactive Spaces Native Activity is 
given below.

::

  space.activity.type=interactivespaces_native
  space.activity.name=example_activity_java_simple

  space.activity.executable=interactivespaces.example.activity.java.simple-1.0.0.jar
  space.activity.java.class=interactivespaces.activity.example.java.simple.SimpleJavaExampleActivity

Notice that the Activity Type is ``interactivespaces_native``.

The executable this time is a Java jar file which contains all of the
classes needed by the Activity. The Workbench IDE builds this jar file
for you with all of the things that it needs, like the OSGi manifest
headers.

The important configuration parameter is ``space.activity.java.class``, which
gives the name of the Java class which is the Activity. here it has the
value ``interactivespaces.activity.example.java.simple.SimpleJavaExampleActivity``.
