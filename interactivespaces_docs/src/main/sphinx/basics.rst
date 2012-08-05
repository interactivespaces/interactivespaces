Interactive Spaces Basics
*************************

Now that you have installed and run your first Interactive Spaces activity it is time to 
understand more of the pieces of what you have done.

Overview
========

An "interactive space", in Interactive Spaces terms, is a collection of event producers
and consumers in a physical space which can sense what is happening inside the
space and then react to it in some manner. 

A simple example of an event producer would be a pressure sensor under
the carpet signaling that someone has stepped on it. A more complex event might include the
use of a depth camera whose events would give the angles of all the joints of the
person it is tracking.

A simple example of an event consumer would be a light which turns on, while a more
complex one might be speech synthesis being used to tell you to not step on the
carpet.

These event consumers and producers can then be connected in interesting ways. The events
used by gesture recognition could be used to both turn on the light and have
the speech synthesis machine tell you it is impolite to point.

The event producers and consumers in Interactive Spaces are implemented as Live Activities.
A Live Activity is some computer program running somewhere in the space which is
either producing events or consuming events, or even both at the same time.

These Live Activities need to run on a computer somewhere in the space. 
In Interactive Spaces, a Space Controller is the container for Live Activities 
which runs on a given computer. A given Space Controller can run many Live Activities
as need to run on that machine. Though there is nothing to stop you from running
multiple Space Controllers on a given machine, there is rarely, if ever, a reason
to. Live Activities need a Space Controller to run in. The Controller directly 
controls the Live Activities it contains and tells them to start, stop, and 
several other operations to be described later.

Live Activities are controlled remotely by the Master. The Master knows about all 
Space Controllers in the space and what Live Activities are on each Controller.
If you want to deploy a Live Activity, update it, start it running, shut it down,
or any of a number of other operations, this is all done from the Master which tells
the Space Controller holding the Live Activity what it wants the Live Activity
to do. You can also control Space Controller-specific operations from the Master, 
such as telling the Controller to shut down all Live Activities currently running
on it and/or shut itself down.

A given installation of Interactive Spaces can run on a single computer or
an entire network of computers. You can run both a master and a Space Controller
on the same machine, in fact, this is often done to give yourself a development
environment, but usually you will have a lot of machines talking with each other.

To summarize, Live Activities provide the functionality and interactivity of the 
space. The Master controls the entire space by speaking remotely to the 
Space Controllers which contain and directly control the Live Activities. The Live 
Activities produce and consume events happening inside the space by speaking 
directly with each other.

Activities and Live Activities
==============================

There is a lot to Live Activities. They contain an Activity and potentially
a configuration. They also have a lifecycle.

Let's look at each of these.

Activities and Configurations
-----------------------------

Live Activities are versions of Activities which are installed on a given 
Space Controller. A good analogy for an Activity is a program 
you have an install disk for, say a graphics program, and the Live Activities 
are the copies of that graphics program that you have installed on both your 
laptop and on your desktop. You can think of the laptop and desktop in this example
as the Space Controllers running your graphics program Live Activities.

Activities come with configurations which give default values for different
parameters which can control how the Live Activity works. Each Live Activity can
also have its own configuration which can override any of the values found in the
configuration which is part of the Activity the Live Activity is based on. In
the graphics program example, perhaps on your laptop you have it configured to use the
touchpad for drawing, whereas the desktop uses an active pen tablet.

You can have more than one Live Activity based on a given Activity, even on the 
same Space Controller, each one potentially configured differently from any of 
the other copies of the Activity in your space. So a Live Activity has potentially 3 parts,
it has an Activity, which is the program the Live Activity runs, it has a Space Controller,
which specifies which machine the Live Activity runs on. And it potentially has
a configuration which makes it slightly different than any of the other Live
Activities based on the same underlying Activity.


The Live Activity Lifecycle
---------------------------

Live Activities have a lifecycle, which says what state they are in at any given time.
The full lifecycle has a lot of different states to it, for now we will look at the
major ones.

READY

  Deployed within the controller
  
RUNNING

  The Live Activity is loaded and running, but can't necessarily handle requests
  
ACTIVATE

  The Live Activity can handle requests

  This is needed because some Live Activities can take a long time to reach RUNNING
  state. For example, a piece of hardware may take a long time to warm up.
  
CRASHED

  The Live Activity has crashed


The Master
==========

Contains model of entire space
Controls controllers
Shutdown
Shutdown all installed applications
Deploys Activities to Controllers
Controls the lifecycle of Live Activities on the Controllers
Maintains Activity Repository
Provides naming services for pubsub topics

Controllers
===========

Provides running environment for Live Activities
Controls lifecycle of Live Activities under the direction of the Mmaster
Provides services to Live Activities
e.g. web server, browser control, logging
Provides alerting mechanism when Live Activities fail
Can automatically try to restart failed Live Activities

Activities
==========
a base configuration
the base activity binaries (if any)
initial data (if any)

Live Activity
=============

an Activity (as above)
a Controller that it runs on
an installation-specific configuration which can modify the base configuration


Live Activity Group
===================

A Live Activity Group is a group of Live Activities which are controlled as a single 
unit. The Group only has meaning on the Master, Controllers only understand about
Live Activities.

Groups are deployed by deploying each Live Activity in the Group. They also have 
the same lifecycle as a Live Activity and can be started, activated, 
deactivated, and shutdown. The particular lifecycle request will be sent to each Live
Activity in the Group. However, there is one slight difference in that the lifecycle
requests are reference counted.

Reference counted for deactivation and shutdown
