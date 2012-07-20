The Workbench
**************

The Interactive Spaces Workbench provides the ability to create
and build projects. You can also create projects for IDEs such
as Eclipse.

The Workbench also contains the documentation for Interactive Spaces,
which includes both this manual, and the Javadoc for the code
implementing Interactive Spaces.

Building an Activity Project
==========================

These projects must be built with the Workbench Builder. From the
directory where you installed the Workbench, you can type something
like

::

  java -jar interactivespaces-launcher-0.0.0.jar <dir> build

where *dir* is the folder which contains the project. For example, you can build
the Simple Web Activity Example project with the command

::

  java -jar interactivespaces-launcher-0.0.0.jar examples/interactivespaces-example-activity-java-simple build


Creating an Activity Project
==========================

You can create activity projects very simply.

::

  java -jar interactivespaces-launcher-0.0.0.jar create language <language>

where *<language>* is one of *java*, *javascript*, or *python*.

You then get a project of one of those types which contains an initial
piece of code for you to then start editing. You will be prompted for
identifying name, version, name, and description of the new project.

Using an IDE
==========================

if you are doing Java projects, you will want an IDE project. Right
now, only Eclipse is supported. You can also use Eclipse for a
non-Java project, the same command works.

::

  java -jar interactivespaces-launcher-0.0.0.jar <dir> ide eclipse

where *<dir>* is the directory containing the Activity project.

For example

::

  java -jar interactivespaces-launcher-0.0.0.jar foo.bar.yowza ide eclipse

will take the *foo.bar.yowza* project and generate the Eclipse
files for it. You can then import the project into Eclipse.

You should rebuild the IDE project every time you add a new JAR into the 
*bootstrap* folder of your controller so that you will have access to the classes
in that JAR file.
