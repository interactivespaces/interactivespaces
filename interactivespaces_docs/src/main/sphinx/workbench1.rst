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

Building a Project
----------------------------

Interactive Spaces projects must be built with the Workbench Builder. From the
directory where you installed the Workbench, you can type something
like

::

  bin/isworkbench.bash <projectdir> build

where ``<projectdir>`` is the folder which contains the project. For example, if you are in the Workbench root directory,
you can build
the Simple Java Activity Example project with the command

::

  bin/isworkbench.bash examples/basics/hello/interactivespaces.example.activity.hello build

Cleaning a Project
----------------------------

Sometimes your project build directory gets a lot of extraneous files in, perhaps you renamed one of your classes,
and you want to clean out the build directory and have a fresh build. You can delete the entire build folder with the
command

::

  bin/isworkbench.bash <projectdir> clean

where ``<projectdir>`` is the folder which contains the project. For example, if you are in the Workbench root directory,
you can clean
the Simple Java Activity Example project with the command

::

  bin/isworkbench.bash examples/basics/hello/interactivespaces.example.activity.hello clean

Combining Workbench Commands
----------------------------

You can combine workbench commands very easily by placing them one after another on the commandline. For example, if you
wish to first clean your project and then build it, you can use the command

::

  bin/isworkbench.bash <projectdir> clean build

where ``<projectdir>`` is the folder which contains the project. For example, if you are in the Workbench root directory,
you can clean and build
the Simple Java Activity Example project with the command

::

  bin/isworkbench.bash examples/basics/hello/interactivespaces.example.activity.hello clean build


Creating a Project
----------------------------

You can create  projects very simply.

::

  bin/isworkbench.bash create language <language>

where ``<language>`` is one of ``java``, ``javascript``, ``python``,
or ``android``.

You then get a project of one of those types which contains an initial
piece of code for you to then start editing. You will be prompted for
identifying name, version, name, and description of the new project.

If you are using ``android``, please see the chapter on Android for details about
initial configurations.

.. _workbench1-using-ide-label:

Using an IDE
----------------------------

if you are doing Java projects, you will want an IDE project. Right
now, only Eclipse is supported. You can also use Eclipse for a
non-Java project, the same command works.

::

  bin/isworkbench.bash <projectdir> ide eclipse

where ``<projectdir>`` is the directory containing the Activity project.

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


The ``version`` element gives the version number of the project.
Versions consists of 3 sets of numbers, separated by dots. Examples would be

* 1.0.0
* 0.1.0-beta

Notice the last one has a dash followed by some text.

.. _workbench1-resource-copying:

Resource Copying
----------------

Often you will find that you have resources which several of your projects will use. An
example would be Javascript libraries that are being used in several of your web browser-based
activities.

You can have these resources copied into every project that uses them by putting a
``<resources>`` section in your ``project.xml`` file. This section will specify all
resources that should be packaged in your project.

An example would be

::

  <resources>
    <resource destinationDirectory="webapp/fonts/OpenSans"
        sourceDirectory="${repo}/resources/fonts/OpenSans" />
    <resource destinationDirectory="webapp/js/libs"
        sourceDirectory="${repo}/resources/web/js/base" />
    <resource destinationDirectory="webapp/js/libs"
        sourceFile="${repo}/resources/web/js/external/jquery/core/jquery-1.9.1.min.js" />
  </resources>

These resource declarations are giving the location of resources that are needed
and where they should be copied to.

Lets look at the first one.

::

  <resource destinationDirectory="webapp/fonts/OpenSans"
      sourceDirectory="${repo}/resources/fonts/OpenSans" />

This gives the destination directory where the resources should be copied to in the
``destinationDirectory`` attribute. Here the OpenSans font files are being copied to the
``webapp/fonts/OpenSans`` subfolder of an Activity.

The source of the OpenSans fonts is given by the ``sourceDirectory`` attribute. The entire
content of the source directory will be copied from the source directory, including the
content of any subfolders of the source directory, their subfolders, and all the way down.

One thing to notice here is the use of ``${repo}``. This is an example of using a local
configuration variable to specify where the resources are being copied from or to.
The example here is demonstrating having a code repository which contains all resources
being used for all projects. See :ref:`workbench1-configure-workbench-label`
for more details on how to declare local configuration variables.

The last entry in the above example shows how to copy a specific file.

::

  <resource destinationDirectory="webapp/js/libs"
      sourceFile="${repo}/resources/web/js/external/jquery/core/jquery-1.9.1.min.js" />

This ``<resource>`` element uses the ``sourceFile`` attribute to specify an exact file
to be copied into the destination directory. In this case the file will be copied and given
the exact name that the source file has, in this case ``jquery-1.9.1.min.js``.

If you want to rename the file, you can use the ``destinationFile`` attribute instead of the
``destinationDirectory`` attribute.


::

  <resource destinationFile="webapp/js/libs/jquery.js"
      sourceFile="${repo}/resources/web/js/external/jquery/core/jquery-1.9.1.min.js" />

The above example would copy the file ``jquery-1.9.1.min.js``, but would name it
``jquery.js`` in the destination location.

Your Project Source Directory
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The Workbench supplies a configuration property that gives you the location of your project
folder. It is called ``project.home`` and gives the full filepath to where your project lives.
This can be useful for copying resources from your project from, say, a library from a C
or C++ build.

An example could be

::

  <resource sourceFile="${project.home}/native/build/artifact" />

Which would copy the file ``artifact`` from the subfolder ``native/build`` in your project
folder. ``native/build`` might be the folder that your C build places the final library or
executable that it builds.

.. _workbench1-resource-assemblies:

Resource Assemblies
^^^^^^^^^^^^^^^^^^^

In addition to copied resources, it's possible to include an ``assembly``, which is a single bundle file
that's extracted into a collection of files. See :ref:`workbench1-assembly-projects` for documentation
on how assembly projects are created. To use a resource assembly, specify an ``<assembly>`` tag as in the
example below.

::

  <resources>
    <assembly packFormat="zip" sourceFile="${project.home}/javascript.bundle-1.0.0.zip" />
  </resources>


Additional Sources
^^^^^^^^^^^^^^^^^^

Using the ``sources`` directive in a project, it's possible to include additional directories into the project
build source path. For example, this can be used to create a shared source java file that contains constants used
across a number of different activities. The additional sources are passed to the underlying project builder,
which will typically process them in the same manner as source files in the activity home directory itself.

Note that is is also possible to use :ref:`workbench1-library-projects` to create shared activity functionality.

::

  <sources>
    <source sourceDirectory="${project.home}/../shared/src/main/java" />
  </sources>


.. _workbench1-import-deploy-label:

Quick Importing or Deploying Your Projects
------------------------------------------

After building a project you will need to import it into the Interactive Spaces Master Web
Admin. This can involve a lot of mouse clicks, so Interactive Spaces makes it easy to
import or deploy your application from the Workbench command line. For example, something
I do a lot is use the command

::

  bin/isworkbench.bash my/project/location clean build deploy testdeploy

This command would do a clean build of the project contained in the folder
``my/project/location`` and then deploy it to the ``testdeploy`` target.

Deploy targets are found in the ``project.xml`` file in the Deployments section.
An example would be

::

  <deployments>
    <deployment type="testdeploy" location="${deployment.test.deploy}" />
    <deployment type="testimport" location="${deployment.test.import}" />
  </deployments>

The command line example given above refers to a deploy target called ``testdeploy``. The
deployment target is defined with a ``<deployment>`` element. The ``testdeploy`` example

::

  <deployment type="testdeploy" location="${deployment.test.deploy}" />

specifies the deployment target name in the ``type`` attribute. The Workbench would then copy
the activity to the value of the ``location`` attribute. Here we are using a local configuration
variable to specify where the built Activity should be copied to. See
:ref:`workbench1-configure-workbench-label`
for more details on how to declare local configuration variables. The value of this variable
would be the autoimport folder (see :ref:`workbench1-best-practice-import-deploy-label`
for details) for the Interactive spaces master for your development
installation. You could also provide deployments for your QA environment, your production
network, etc.

A Complete Project File
-----------------------

Here is an example of a complete Activity project file with resource and deployment sections.

::

  <?xml version="1.0"?>
  <project type="activity" builder="java">
    <name>My Web Activity</name>
    <description>
  A simple web activity.
    </description>

    <identifyingName>my.web</identifyingName>
    <version>1.0.0</version>

    <resources>
        <resource destinationDirectory="webapp/fonts/OpenSans"
            sourceDirectory="${repo.cec}/resources/fonts/OpenSans" />
        <resource destinationDirectory="webapp/js/libs"
            sourceDirectory="${repo.cec}/resources/web/js/base" />
        <resource destinationDirectory="webapp/js/libs"
            sourceFile="${repo.cec}/resources/web/js/external/jquery/core/jquery-1.9.1.min.js" />
    </resources>

    <deployments>
        <deployment type="testdeploy" location="${deployment.test.deploy}" />
        <deployment type="testimport" location="${deployment.test.import}" />
    </deployments>
  </project>


Other Project Types
===================

.. _workbench1-library-projects:

Library Projects
----------------

Library projects let you write code which can be shared across multiple Interactive Spaces
Activities. Libraries are one way in which you can extend the functionality of
Interactive Spaces with your own functionality.

An example ``project.xml`` file for a library project is given below.

::

  <?xml version="1.0"?>
  <project type="library" >
    <name>Support for Interactive Spaces projects</name>
    <description>
  Support For Interactive Spaces projects.
    </description>

    <identifyingName>my.support</identifyingName>
    <version>1.0.0</version>
  </project>

Library projects must be Java-based, hence the lack of the ``builder`` attribute on the
``<project>`` element. The project file has the same name, description, identifying name,
and version sections that all projects must have. But the ``type`` attribute of the
``<project>`` element has the value ``library``.

The artifact built for a Library project will be a Java jar file. It can be copied into
the ``bootstrap`` folder of an Interactive Spaces controller and will then be available for
Activities to use.

A Resource section (see :ref:`workbench1-resource-copying`) in your Library ``project.xml`` will copy the files
such that they will appear in the JAR file created for the library. Destination pathnames will be relative to the
root of the JAR file.

If you add a new Library into a Controller, make sure you recreate the IDE project
for any Activities which will use the Library and refresh the project in your IDE.
See :ref:`workbench1-using-ide-label`
for more details on creating the IDE project for a Workbench project.


Service Projects
----------------

Resource Projects
-----------------

.. _workbench1-assembly-projects:

Assembly Projects
-----------------

Assembly projects create a bundle file (typically as a compressed ``zip`` file) that consists of several constituent files.
The resulting bundles can then be included using an ``assembly`` resource directive
(see :ref:`workbench1-resource-assemblies`). The project snippet below will create an assembly
named ``javascript.bundle-1.0.0.zip`` that can then be included elsewhere.

::

    <project type="assembly" packFormat="zip" >
      <identifyingName>javascript.bundle</identifyingName>
      <version>1.0.0</version>
      <sources>
        <source sourceFile="src/main/css/base_admin.css" />
        <bundle destinationFile="webapp/js/bundle.js">
          <source sourceFile="src/main/js/external/jquery/core/jquery-1.9.1.min.js"/>
          <source sourceFile="src/main/js/base_admin.js" />
        </bundle>
      </sources>
    </project>

Internal to the bundle, there is a ``base_admin.css`` file as well as a bundle file ``bundle.js``, which
contains two individual source files concatenated together.

Other Workbench Operations
==========================

.. _workbench1-configure-workbench-label:

Configuring the Workbench
-------------------------

You can provide configuration variables to the Workbench which become
available during project builds. These configurations would go in a file
called ``local.conf`` and placed in the ``config`` folder found
where you installed your Workbench.

An example of a local configuration file would be

::

  repo=/my/home/repo
  deployment.test.deploy=/my/home/interactivespaces/master/master/activity/deploy
  deployment.test.import=/my/home/interactivespaces/master/master/activity/import

This configuration file would make the variables ``${repo}``,
``${deployment.test.deploy}``, and ``${deployment.test.import}``
available for your ``project.xml`` files.

These examples are showing where the code repository being used for the project
would be found, useful if there are common resources that you want
to use in multiple projects, and also where the Interactive Spaces Master
being used for your development work is located. The directories given are
folders watched by the master for when files are copied into them
which are then automatically imported into the master or deployed to all controllers
containing Live Activities based on the Activity being copied.


Creating Documentation for a Project
----------------------------

When creating projects like library projects that may be shared with others,
it is important to give good documentation for those who will use the library.
The Workbench can create documentation fort your project.

::

  bin/isworkbench.bash <projectdir> docs

where ``<projectdir>`` is the folder which contains the project. For example, you can build
the Simple Web Activity Example project with the command

::

  bin/isworkbench.bash examples/basics/hello/interactivespaces.example.activity.hello docs

The Workbench can only create Javadocs at the moment, which means it currently
only works on Java-based Activities
or Library projects. The output of the command will be placed in the ``build/docs`` folder
of your project.

Performing Workbench Operations on a Collection of Projects
---------------------------------

Sometimes you want to build a collection of Interactive Spaces projects.
If all of the projects are contained within a given root folder this is easy to do.

::

  bin/isworkbench.bash <rootdir> walk <commands>

Here ``<rootdir>`` is the root directory containing all of the projects
and ``<commands>`` is the list of commands to be done on all of the
projects.

The ``walk`` command will walk all subfolders of the root directory
looking for folders which contain a ``project.xml`` file. Those it finds
it will perform the commands on.

For instance, if you want to do a clean build all of the examples
which come with the Workbench, you could use the command

::

  bin/isworkbench.bash /my/home/interactivespaces/workbench/examples walk clean build

where ``/my/home/interactivespaces/workbench`` would be the directory
where you installed the Workbench.

Adding Flags to the Java Compiler
---------------------------------

You can add additional flags to the Java compiler by defining the local configuration variable
``interactivespaces.workbench.builder.java.compileflags`` in your local Workbench
configuration. This will add the space-separated flags to the command line to the Java
compiler.

See :ref:`workbench1-configure-workbench-label`
for more details on how to declare local configuration variables.

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

To create an OSGi bundle, you use the command

::

  bin/isworkbench osgi <pathToJar>

where ``<pathToJar>`` is the path to the Java library you want made into an OSGi
bundle. The output of the command will be a new jar with the prefix ``interactivespaces.``
added to its name. For instance, if your jar was originally called ``foo-1.0.0.jar``,
the OSGi bundle created from the jar will be called ``interactivespaces.foo-1.0.0.jar``.

Best Practices for Developing in Interactive Spaces
===================================================

There are some simple things you can do if you want to develop reasonably
quickly in Interactive Spaces.

.. _workbench1-best-practice-import-deploy-label:

Importing and Deploying
-----------------------

It is important to understand the difference between importing and deploying in Interactive Spaces.
Importing an Activity places an Activity into the Master's Activity Repository and makes it possible
to find from the Master Web Admin. It is normally done from the **Activity** tab in the Master
Web Admin.

However importing an Activity into the Master does not immediately send it to the Space
Controllers where instances of the Activity are deployed. To do that you need to deploy
the Live Activities which are based on the Activity
you are developing.

You should create the following two folders where your Master is installed

* ``/my/home/interactivespaces/master/master/activity/import``
* ``/my/home/interactivespaces/master/master/activity/deploy``

where ``/my/home/interactivespaces/master`` is where your Master is installed.

Yes, that bit in the middle is real, it is not a stutter, it really is meant to be ``master/master``.
These two folders are watched by the master and are used for automatically importing or dpeloying
Activities or the Live Activities they are based on.

If you copy an Activity into the ``import`` folder, it is the same as importing it from the Master Web Admin.
The Activity will be created in the Master if no other activity has the same Identifying Name
and Version. If an Activity has the same Identifying Name and Version the Activity just
imported will replace the old Activity that was in the Activity Repository.

If you copy the Activity into the ``deploy`` folder, it will first be imported into the Activity
Repository using the same rules given above. Then any Live Activities based on the Activity
will be re-deployed to the Space Controllers they are on. If you use the Deployment project file
section discussed in :ref:`workbench1-import-deploy-label`. This means you can compile and deploy to the
controller in one fell swoop.

