Activities
**********

Activities are the workhorse of your Interactive Space. Any functionality that you
want is implemented as an Activity.

Activity Types
==============

Activity Types make it easy for you to implement an activity with mostly
configuration parameters and only as much code as is necessary to add in the
behavior you want.

Activity Components
===================

Activity Components make it easy to add functionality to your Activity.

web.browser
-----------

The *web.browser* component will start a web browser on your controller.
This browser can be given any URL to start with and can be either a URL external
to your Activity or one found within your Activity itself.

web.server
----------

The *web.server* component starts up a web server within your Activity. This
web server can both serve content found in your Activity and also provide
a web socket connection for the Activity or anything else within your Interactive
Space which needs web socket support.

native.runner
-------------

The *native.runner* component allows your Activity to start and stop applications
native to the Operating System on the computer running a Space Controller.
This can include music programs, OpenFrameworks applications written in languages
like C++, or any other native application.

The configuration parameter

:: 

  space.activity.component.native.executable

gives the path to the executable to be run. It can
be relative to the install directory for your Activity on the Controller or
can be outside your Interactive Spaces installation. Code outside your
installation have to be cleared by a whitelist of allowed applications.

The full name this parameter will have the operating system the controller is 
running on added to the end. For example, if your Controller is on a Linux computer,
the full name for the configuration property is 

::

  space.activity.component.native.executable.linux

This allows you to have multiple versions of an executable in your Activity
and the Controller can pick which one to use based on the operating system
the Activity is run on.

The configuration parameter

::

  space.activity.component.native.executable.flags

gives any flags that need to be given to the executable when it runs. As with 
the executable path, the Controller will add the name of the operating system
to the end of this configuration parameter before looking it up in the Activity
configuration. For example, if the Operating System for the controller was a Mac 
running OS X, the parameters will name would be

::

  space.activity.component.native.executable.flags.osx

