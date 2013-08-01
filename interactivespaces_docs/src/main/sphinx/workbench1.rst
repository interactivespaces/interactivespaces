The Workbench
**************

The Interactive Spaces Workbench provides the ability to create
and build projects. You can also create projects for IDEs such
as Eclipse.

The Workbench also contains the documentation for Interactive Spaces,
which includes both this manual, and the Javadoc for the code
implementing Interactive Spaces.

The Basics
==========

Building an Activity Project
----------------------------

Interactive Spaces projects must be built with the Workbench Builder. From the
directory where you installed the Workbench, you can type something
like

::

  bin/isworkbench.bash <dir> build

where ``dir`` is the folder which contains the project. For example, you can build
the Simple Web Activity Example project with the command

::

  bin/isworkbench.bash examples/interactivespaces-example-activity-java-simple build


Creating an Activity Project
----------------------------

You can create activity projects very simply.

::

  bin/isworkbench.bash create language <language>

where ``<language>`` is one of ``java``, ``javascript``, ``python``,
or ``android``.

You then get a project of one of those types which contains an initial
piece of code for you to then start editing. You will be prompted for
identifying name, version, name, and description of the new project.

If you are using ``android``, please see the chapter on Android for details about
initial configurations.

Using an IDE
----------------------------

if you are doing Java projects, you will want an IDE project. Right
now, only Eclipse is supported. You can also use Eclipse for a
non-Java project, the same command works.

::

  bin/isworkbench.bash <dir> ide eclipse

where ``<dir>`` is the directory containing the Activity project.

For example

::

  bin/isworkbench.bash foo.bar.yowza ide eclipse

will take the ``foo.bar.yowza`` project and generate the Eclipse
files for it. You can then import the project into Eclipse.

You should rebuild the IDE project every time you add a new JAR into the
``bootstrap`` folder of your controller so that you will have access to the classes
in that JAR file.

Project Files
=============

Interactive Spaces Workbench projects use a ``project.xml`` file which describes
aspects about the project, including its type, its name and description,
how to build it, and any dependencies or resources that the project needs.

A basic ``project.xml`` file will be created for your project if you create
it with the Workbench. There may be a need to extend the contents of this
file or to edit it later on, so it is good to understand all of its pieces.

A Basic Activity Project File
-----------------------------

Let's look at one of the simplest project files. This is for the
``interactivespaces.example.activity.hello`` example which comes with
Interactive Spaces.

::

  <?xml version="1.0"?>
  <project type="activity" builder="java">
    <name>Simple Hello World Activity Example in Java</name>
    <description>
  A very simple Java-based activity which just logs at INFO level its
  callback messages.
    </description>

    <identifyingName>interactivespaces.example.activity.hello</identifyingName>
    <version>1.0.0</version>
  </project>

As you can see, project files are XML-based. The root element is
called ``project`` and has a couple of attributes giving information
about the project. One is ``type`` which gives the type of the project.
Since this example is an activity project, so the ``type`` attribute
has the value ``activity``.

The Workbench needs to know how to build projects. The ``build`` attribute
says which builder to use for the project. The ``interactivespaces.example.activity.hello``
project is a Java-based project, so uses the Java builder. So we give
the attribute ``builder`` the value ``java``.

Java projects are somewhat complicated to build because they need to use the
Java compiler and create a jar file. Projects that use scripting
languages or are web only can use a very simple builder that merely takes the
contents of the ``src/main/resources`` folder and places them into
a properly formatted zip file, the format that Interactive Spaces uses for
its activity files. These projects do not need to specify the ``builder``
attribute, as you can see in the example project
``interactivespaces.example.activity.hello.python``.

::

  <?xml version="1.0"?>
  <project type="activity">
    <name>Simple Hello World Activity Example in Python</name>
    <description>
  A very simple Python-based activity which just logs at INFO level its callback messages.
    </description>

    <identifyingName>interactivespaces.example.activity.hello.python</identifyingName>
    <version>1.0.0</version>
  </project>

Notice that the ``project`` element only contains the ``type`` attribute, not the
``builder``.

The next part of the project file is the ``name`` element. This gives
the informational name of the project, the name that will appear in the
Interactive Spaces Web Admin.

The ``description`` element gives a more detailed description of the project.
It is optional. It is also displayed in the Interactive Spaces Web Admin
when looking at the specific page for the Activity.

The ``identifyingName`` element gives the Identifying Name for the project.
This name is used by the internals of Interactive Spaces and has very strict
rules on its syntax. The combination of the Identifying Name and the Version
uniquely identify the Activity to Interactive Spaces.


The Identifying Name is is a dot separated set of names, Examples would be
things like

* a.b.c
* com.google.myactivity

Each part of the name must start with a letter and can then be letters,
digits, and underscores.


The ``version`` element gives the version number of the project. It is of the
form `x.y.z` where each component is a number.

Other Project Types
===================

Library Projects
----------------

Service Projects
----------------

Resource Projects
-----------------

Other Workbench Operations
==========================

OSGi Bundle Wrapping
--------------------

Interactive Spaces uses a Java technology called OSGi as its runtime
container. OSGi permits Interactive Spaces to do many things, such as
run multiple versions of the same library or Live Activity, even if
the different versions are binary incompatible with each other.

For this to work, the libraries that Interactive Spaces uses must be
made into what is called an OSGi bundle. Many open source libraries are already
OSGi compatible, but not all are. Because of this, the Interactive Spaces
Workbench provides a way of making a Java jar into an OSGi compatible one.

