Your First Interactive Spaces Activities
***********************

Let's get your first Interactive Spaces activity installed and running. To keep it simple,
you will install one of the sample activities that comes with an Interactive Spaces Workbench
installation. After that you will create an activity from scratch.

Make sure you start the Master up first and then your Controller. When you shut things down,
you should shut the Controller down first, then the Master. To shut them down, go into the shell
window where you started them up and type Ctrl-D, where Ctrl is the control key on your keyboard.

Running An Sample Activity
============================

Let's start off by uploading one of the sample activities found in the
Interactive Spaces Workbench into Interactive Spaces. This will help
demonstrate some of the basic Interactive Spaces concepts.

Uploading The Activity
-------------------------------

The first step is to load the sample activity into the Master. To do this, go to the Master Web
Interface and click on the **Activity** menu, then **Upload**. This will show you the following
screen.

.. image:: images/ActivityUploadStep1.png

Click the **Choose File** button and go to where you installed the Interactive Spaces Workbench.
Find the ``interactivespaces.example.activity.script.python example`` in the examples folder, go
into the target folder, and select the file ending with .zip. You should end up with something
like the following.


.. image:: images/ActivityUploadStep2.png


From here, click **Save**. You should then see

.. image:: images/ActivityView.png

Creating the Live Activity
-------------------------------

Before you can deploy and run an Activity, you must first create an Live Activity. This
allows you to say which Controller the Activity will be deployed on.

Click Live Activity and then **New**. This will give you the following screen.


.. image:: images/LiveActivityNew.png


Since this is the first activity you are installing, you should only have one Controller and
One Activity to choose from the appropriate dropdowns. So pick a descriptive name for your
activity and write a short description and hit **Save**. This will take you to the following
screen.


.. image:: images/LiveActivityView.png


Deploying the Live Activity
-------------------------------

You need to deploy the Live Activity to the Controller before you can run it. To do this,
click on the **Deploy** button on the Live Activity Screen. If you look in the  Master and
Controller consoles, you should see some logging message about deploying the activity. The
logs will use the UUID you see in the example above, in my installation it was
``67d4232c-b61b-46cd-ae20-c0834f9e972d``, yours will be different.

Starting Up The Live Activity
-------------------------------

You can now start the activity up by clicking the **Startup** command. You should see the startup
happen in the logs in both the Master and Controller consoles, once again by using the UUID
to identify the Live Activity. You should also see an error log in the Controller Console
saying that the Live Activity has started up. This message is coming from the Python script,
which you can see in the Workbench examples folder.

Activating The Live Activity
-------------------------------

Live Activities must be Activated before they can handle any requests. You will find out
more about what that means later. For now you can now activate the activity up by clicking the
**Activate** command. You should see the activation happen in the logs in both the Master
and Controller consoles, including an error log in the Controller Console from the Python
script saying that the Live Activity has activated.

Deactivating The Live Activity
-------------------------------

Activated Live Activities can be either Deactivated if you want them to stop processing requests
but keep running, or Shutdown. You can now deactivate the activity up by clicking the
**Deactivate** command. You should see the deactivation happen in the logs in both the
Master and Controller consoles, including an error log in the Controller Console from the
Python script saying that the Live Activity has deactivated.

Shutting Down The Live Activity
-------------------------------

Shut down the activity up by clicking the **Shutdown** command. You should see the shutdown
happen in the logs in both the Master and Controller consoles, including an error log in the
Controller Console from the Python script saying that the Live Activity has shut down.

Creating an Activity Project From Scratch
=========================================

The Workbench provides a bunch of operations for working with activities,
including the ability to create new projects and also build them.

Creating the Activity Project
-----------------------------

First let's create a new Activity project.

You can easily create template projects in Java, Python, and Javascript.
Let's start off with a Java project.

From the command line go to the directory where you installed the Workbench.
Once there, type the following command.

::

  bin/isworkbench.bash create language java

You will then be asked a series of questions. Let's create a project called
*me.activity.first*.

The first question is the identifying name.

::

  Identifying name:

This, along with the version given later
uniquely identifies the Activity to Interactive Spaces.


::

  Identifying name: me.activity.first

The identifying name is is a dot separated set of names, Examples would be
things like

* a.b.c
* com.google.myactivity

Each part of the name must start with a letter and can then be letters,
digits, and underscores.


Next is the version.

::

  Version:

Let's make it version 1.

::

  Version: 1.0.0

Versions consists of 3 sets of numbers, separated by dots. Examples would be

* 1.0.0
* 0.1.0-beta

Notice the last one has a dash followed by some text.

Next is the name.

::

  Name:

Let's use the name *My First Interactive Spaces Activity*.

::

  Name: My First Interactive Spaces Activity

The name is the human readable name which shows up in the Interactive Spaces
web UI.

Finally there is a description.

::

  Description:

Make this description anything you want.

This field is not required. It is a description of the activity.

The project will then be created in the Workbench directory.


Building the Activity
---------------------

The activity is built using the Workbench.

Suppose the name of your project is *me.activity.first*. The command to
build your project is

::

  bin/isworkbench.bash me.activity.first build

This will put a file called *me.activity.first-1.0.0.zip* in the
*me.activity.first/build* directory.

You can upload this activity into Interactive Spaces the same way you did
the sample activity.

Using an IDE
------------

You can create an IDE project for your activity projects, even if they
aren't Java projects.

Suppose the name of your project is *me.activity.first*. The command to
create the IDE project is

::

  bin/isworkbench.bash me.activity.first ide eclipse

This will build an Eclipse project which you can then import into Eclipse.

Next Steps
==========

You have now walked through installing an activity on a controller and running it. You should
look at the various examples in the Interactive Spaces Workbench to get an idea of the types of
activities you can create in Interactive Spaces.

In the next chapter we will examine the basics of Interactive Spaces in more detail.
