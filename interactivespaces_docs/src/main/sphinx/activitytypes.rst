.. _activity-types-label:

Activity Types
**************

Activity Types make it easy for you to implement an Activity with mostly
configuration properties and only as much code as is necessary to add in the
behavior you want.

Using Activity Types
====================

Activity types are easy to use, simply set the ``type`` attribute on the ``<activity>`` element
in the ``project.xml`` for the activity. For example

::

  ...
  <activity type="web">

This will become clearer in the descriptions below.

Built-In Activity Types
=======================

Out of the box, Interactive Spaces supplies the following activity types:

* ``web``, that provides a web server and a web browser, you just supply the HTML, CSS, and Javascript
* ``native``, that allows you to provide either a executable or run a executable available on the machine the activity is running on
* ``script``, that allows you to supply activities written in a scripting language supported by Interactive Spaces
* ``interactivespaces_native``, that allows you to develop native Interactive Spaces applications in Java

There may be other activity types that are available because of additional libraries you are using in your
Interactive Spaces installation. See the library documentation for details about activity types the library adds
to your setup.

Web Activity
------------

The simplest Live Activity at all is a *Web* Activity. Web Activities will
automatically start up a web browser and web server when the Live Activity
starts up. The web server will have content supplied with the Activity
and the browser will start up pointing at that content.

Web Activity Project
^^^^^^^^^^^^^^^^^^^^

The configuration for a Web Activity is pretty simple, the simplest
``project.xml``  is given below.

::

  <project type="activity">
    <name>Hello Web Activity Example</name>
    <description>
      An example of an activity that consists only of a web page and a browser.
    </description>
  
    <identifyingName>
      interactivespaces.example.activity.hello.web
    </identifyingName>
    <version>1.0.0</version>
  
    <activity type="web">
      <name>interactivespacesExampleActivityHelloWeb</name>
  
      <configuration>
        <property name="space.activity.webapp.content.location" value="webapp" />
        <property name="space.activity.webapp.url.initial" value="index.html" />
      </configuration>
    </activity>
  </project>

The ``type`` attribute on the ``<activity>`` element gives the Activity Type, which
here is ``web``.

The configuration property ``space.activity.webapp.content.location`` gives
the location of the content to be served. Here the folder it will use 
(which will contain the HTML, CSS, images, Javascript, and other web resources)
is relative to to where your Live Activity is deployed. In the example
above, this is a subfolder called ``webapp``.

The configuration property ``space.activity.webapp.url.initial`` gives the initial
webpage that will be shown in the browser. In the example here, the initial
URL is ``index.html``, so the browser URL will be
``http://localhost:9000/webExample/index.html``.

Notice that ``webExample`` is part of the URL. The name of the Activity is used
as the initial part of the URL. The Activity name is set with the ``<name>`` 
element inside the ``<activity>`` section.

The file ``index.html`` will be found in the location

``live activity install dir/webapp/index.html``

where ``live activity install dir`` is where the Live Activity is installed
on the Space Controller.

You may notice that the web server port is ``9000``. This is the default server port
for the web server. If you want to set it to another value, say for example
you need to run multiple web servers on the same machine, or port 9000 is
being used for something else on the computer, you can change it with the
configuration property ``space.activity.webapp.web.server.port``

For example, if you have

::

  <property name="space.activity.webapp.web.server.port" value="9091" />

in your ``<configuration>`` section of your ``<activity>`` section, the initial URL will be 
``http://localhost:9091/webExample/index.html``.

Sometimes there may be a need for some query string parameters on that initial
URL. A query string parameter list can be created using the
configuration property ``space.activity.webapp.url.query_string``. For example,

::

  <property name="space.activity.webapp.url.query_string" value="foo=bar" />

would make the initial URL be 
``http://localhost:9091/webExample/index.html?foo=bar``.

The query string parameters are best used when created from combinations of 
other configuration properties. for instance, if your ``<configuration>`` included
the following properties


::

  <property name="space.activity.webapp.url.query_string" value="foo=${a}&bar=${c}" />
  <property name="a" value="b" />
  <property name="c" value="d" />

the initial URL would be 
``http://localhost:9091/webExample/index.html?foo=b&bar=d``.

The final configuration property says whether or not the browser should be
in debug mode or not. The default value is to not be in debug mode and the
browser will be started in kiosk mode if that is possible. In debug mode,
the browser will be opened as normal so that the browser's debugger can
be used to debug your Activity.

The configuration property that sets whether or not the browser is started in debug
mode is ``space.activity.webapp.browser.debug``. It should be given a value
of ``true`` or ``false``. An example would be

::

  <property name="space.activity.webapp.browser.debug" value="true" />
  
Multiple Browsers
^^^^^^^^^^^^^^^^^

Interactive Spaces starts every browser instance with its own profile. This
means that you can start up the browser-based Live Activities on the same
machine where you have your normal browser open and they won't affect
each other.

Native Activity
---------------

Native Activities give you the ability to run native programs on your computer. Native programs could
be ones that came with the operating system the computer runs, or a C++ activity that you write in a
framework like openFrameworks, or even a bash script that you wrote. Pretty much it can be any program
that you can start from the command line of your operating system's shell.


Native Activity Configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A pretty simple ``project.xml`` for a Native Activity is given below.

::

  <project type="activity">
    <name>Native Example</name>
    <description>
      An example of a pure native code activity.
    </description>
  
    <identifyingName>interactivespaces.example.activity.native</identifyingName>
    <version>1.0.0</version>
  
    <activity type="native">
      <name>interactivespacesExampleActivityNative</name>
  
      <configuration>
        <property name="space.activity.executable.linux" value="my_mp3_player" />
        <property name="space.activity.executable.flags.linux" value="-q ${activity.installdir}/NativeActivityExample.mp3" />
      </configuration>
    </activity>
  </project>

Here you can see that the Activity Type is ``native``.

The configuration property
``space.activity.executable.linux`` gives the native executable to run. Here it has the value ``my_mp3_player``.
If you were running your Live Activity on a Linux box, Interactive Spaces
would start up the program

``live_activity_install_dir/my_mp3_player``

where ``live_activity_install_dir`` is the directory where the Live Activity is installed
on the Space Controller.

Notice the ``.linux`` on the end of the configuration property name. This specifies which
operating system this particular executable is for. hTis way you can create
a Universal Activity which contains executables for any operating system
the Activity might run on. Legal values for operating systems at the moment are

* linux - A Linux computer
* osx - A Mac OSX computer
* windows - a Windows computer


As an example, the ``<configuration>`` section might contain

::

  <property name="space.activity.executable.linux" value="my_linux_mp3_player" />
  <property name="space.activity.executable.osx" value="my_osx_mp3_player" />
  <property name="space.activity.executable.windows" value="my_windows_mp3_player" />

This would mean the Activity would contain the 3 executables

* ``my_linux_mp3_player``
* ``my_osx_mp3_player``
* ``my_windows_mp3_player``

and Interactive Spaces will pick the correct executable based on the OS the
Activity is running on.

Often there may be a need for command line arguments, for instance, the
mp3 player needs to know which song to play. In the example above, the
configuration property ``space.activity.executable.flags.linux`` gives the
command line flags when the Linux executable is being used.

The value you see

::

  <property name="space.activity.executable.flags.linux" value="-q ${activity.installdir}/NativeActivityExample.mp3" />

gives the command line flags to play a file which is in the Live Activity's
install directory on its Space Controller.

``live_activity_install_dir/NativeActivityExample.mp3``

where ``live_activity_install_dir`` is the directory where the Live Activity is installed
on the Space Controller.

The executable can also be somewhere else on the machine the Activity is running
on. For example, the ``project.xml`` below uses the program ``/usr/bin/mpg321``
to play the MP3 file that comes with the Activity.

::

  <project type="activity">
    <name>Native Example</name>
    <description>
      An example of a pure native code activity.
    </description>
  
    <identifyingName>interactivespaces.example.activity.native</identifyingName>
    <version>1.0.0</version>
  
    <activity type="native">
      <name>interactivespacesExampleActivityNative</name>
  
      <configuration>
        <property name="space.activity.executable.linux" value="/usr/bin/mpg321" />
        <property name="space.activity.executable.flags.linux" value="-q ${activity.installdir}/NativeActivityExample.mp3" />
      </configuration>
    </activity>
  </project>

Native Activities Automatic Keep Alive
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Every once in a while, a native application may crash. Interactive Spaces
tries to keep things alive, and this is particularly true for native
activities. If, for instance, you shut a web browser down that Interactive Spaces 
has started or otherwise kill it, you will notice it starts up again for some
limited number of times.

Scripted Activity
-----------------

A lot of people feel uncomfortable programming in Java. Programming in Java
gives the most direct access to the power of Interactive Spaces,
but Scripted Activities do have a lot of advantages. You can edit them
directly from their installation folder, which helps a lot when you are
writing your Activity in the first place.

Interactive Spaces supports writing Activities in Javascript and Python, with
more languages coming soon.

Scripted Activity Configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A simple ``project.xml`` for a Scripted Activity is given below.

::

  <project type="activity">
    <name>Simple Hello World Activity Example in Python</name>
    <description>
      A very simple Python-based activity.
    </description>
  
    <identifyingName>
      interactivespaces.example.activity.hello.python
    </identifyingName>
    <version>1.0.0</version>
  
    <activity type="script">
      <name>interactivespacesExampleActivityHelloPython</name>
      <executable>ExamplePythonActivity.py</executable>
    </activity>
  </project>

Notice that the Activity Type is ``script``.

The important element here is ``<executable>``
that gives the Activity executable. Here it has the value ``ExamplePythonActivity.py``.
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
^^^^^^^^^^^^^^^

Scripted Activities can use more than 1 scripting file for their implementation.
Interactive Spaces supports 2 places for scripting libraries to be placed,
one at the Space Controller-wide level, and one at the per-Live Activity
level.

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

Per-activity paths are any files found in the subdirectory ``lib/python`` in the 
Live Activity's install folder will also be added to the Python path.
For example, suppose the UUID of your Live Activity (which you can find 
on the Live Activity's page in the Interactive Spaces Master webapp) is 
``34eb3c27-5d37-45aa-a9cd-22d46bc85701``. The per-Live Activity Python lib path 
for that specific Live Activity would then be found in the folder

::

  interactivespaces/controller/controller/activities/installed/
      34eb3c27-5d37-45aa-a9cd-22d46bc85701/install/lib/python


Interactive Spaces Native Activities
------------------------------------

Interactive Spaces Native Activities (not to be confused with Native
Activities) are Activities written in Java that have direct access to all
of Interactive Spaces services. This is true of some of the scripting languages
as well, but Interactive Spaces Native Activities guarantee access to everything.

Interactive Spaces Native Activity Configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A simple ``project.xml`` for a Interactive Spaces Native Activity is 
given below.

::

  <project type="activity" builder="java">
    <name>Simple Hello World Activity Example in Java</name>
    <description>
      A simple Java-based activity example.
    </description>
  
    <identifyingName>
      interactivespaces.example.activity.hello
    </identifyingName>
    <version>1.0.0</version>
  
    <activity type="interactivespaces_native">
      <name>interactivespacesExampleActivityHello</name>
      <class>
        interactivespaces.activity.example.hello.HelloActivity
      </class>
    </activity>
  </project>

Notice that the Activity Type is ``interactivespaces_native``.

The element ``<class>`` gives the name of the Java class that is the Activity. Here it has the
value ``interactivespaces.activity.example.hello.HelloActivity``. Interactive Spaces will create an
instance of this class when it runs the activity.

For this to fully work, note the ``builder="java"`` attribute on the ``<project>`` element. This tells the
Interactive Spaces workbench that this is a Java-based activity and that the Java builder must be used to
build it.
