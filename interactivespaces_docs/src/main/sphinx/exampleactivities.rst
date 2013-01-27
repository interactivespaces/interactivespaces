Interactive Spaces Example Activities
***********************

The Interactive Spaces Workbench contains a variety of example
activities in its *examples* folder. These are to provide you with some
very basic examples of the sorts of things you can do with Interactive
Spaces.

The number of examples will continue to grow as more functionality is placed
into Interactive Spaces.

Hello World: The Simple Events
============================

The following are written in a variety of programming languages and are found in the
*examples/basics/hello* folder of the workbench.

* interactivespaces.example.activity.hello
* interactivespaces.example.activity.hello.javascript
* interactivespaces.example.activity.hello.python

These activities merely log some Interactive Spaces events which take
place when a Live Activity runs. These examples can help you understand 
what events happen in what order.

You will need to hit the *Configure* button to see the configuration update
event. *Edit Config* should be used first if you have never configured
the activity.

If you want less output from these Activities, find *onActivityCheckState*
and change *info* to *debug*. This means that checking activity state will
only be logged if the logging level for the activity is set to DEBUG.

For fun, there is also a talking Hello World example. Just turn up your computer's speakers
and enjoy.

* interactivespaces.example.activity.hello.speech


Web Activities
==============

You can easily use browser-based applications to provide a UI for your
Activities.

The following examples are found in the *examples/basics/web* folder of the Workbench.

Simple Web Activity
------

It is very easy to use Interactive Spaces to display a web application. Interactive Spaces
will provide a web server which will serve your HTML, CSS, and Javascript, and a web browser
to display that HTML, CSS, and Javascript.

* interactivespaces.example.activity.hello.web

This is a standalone browser activity which shows a single page and not much
else.

Interactive Spaces tries to keep things from crashing. Try closing the browser
and see what happens.

Web Socket Activities
---------------------

The following Activities are browser based, but use Web Sockets so that
the Activity code running in the browser can communicate with other 
Live Activities in the space. The web socket connection connects to the
Live Activity which is running the web browser and server.

* interactivespaces.example.activity.web
* interactivespaces.example.activity.web.javascript
* interactivespaces.example.activity.web.python

Routable Activities
===================

Interactive Spaces really only become interesting when your space has
event producers and event consumers running as their own Activities which
speak to each other.
only do that on separate controllers.

Interactive Spaces uses ROS for its underlying communication. Usage of ROS
can be somewhat intimidating, so the examples below use *routes* which are JSON-based 
communication over ROS. You can
have *input routes* which listen for messages and deliver them to their Live
Activities, and *output routes* which write messages out for an input route
somewhere to receive. Messages on routes are dictionaries of data which can be turned
into JSON messages and read from JSON messages.

The following examples are found in the
*examples/basics/comm* folder of the workbench.

* interactivespaces.example.activity.routable.input
* interactivespaces.example.activity.routable.output
* interactivespaces.example.activity.routable.input.python
* interactivespaces.example.activity.routable.output.python

The above output examples will write a message onb a route. The above input examples
will read the message and write it in the controller's logs. make sure oyu look in the
window where you are running the controller to see the log output.

You need to run both an input activity and an output activity to see these examples work.
The first two are written in Java and the second two are written in Python. You can run
the pair in the same language or in different languages, it doesn't matter.

Also, the two activities can be on the same controller or on separate
controllers. You can also run multiple versions of each activity (such as
multiple versions of the input route sample), but then they must run on different 
controllers.

Want some fun? Run the following route example which will listen on the same route as the
examples above, but will speak the message sent over the route rather than just logging it.

* interactivespaces.example.activity.routable.input.speech

Native Activities
-----------------

You can start and stop native activities with Interactive Spaces. This
makes it easy for you to write activities in openFrameworks and other languages
that don't run inside the Interactive Spaces container.

The following examples are found in the *examples/basics/native* folder of the Workbench.

* interactivespaces.example.activity.native

This example uses the Linux *mpg321* to play an audio file found in the
activity.


Comm Examples
=============

There are a variety of examples which allow you to use Interactive Spaces for communication to
a variety of hardware devices (through serial and Bluetooth) and external services (such as 
Twitter and Chat).

Serial Comm
-----------

Serial communication lets you communicate with hardware devices that attach via serial ports, often
USB in the modern world.

The examples given with Interactive Spaces typically connect to Arduino microcontrollers and read
read or write to sensors connected to the device.

These examples are found in the *examples/comm/serial* folder of the workbench.

You need to configure your controller to work with serial, please see the chapter on 
Interactive Spaces Comm Support for instructions.

* interactivespaces.example.activity.arduino.echo

This Arduino example is very simple. When you activate the Live Activity, it will generate
a random 8 bit number and write it to the serial port. The source for this activity includes
an Arduino sketch called *Echo* which will read any bytes which come over the serial connection
and write them back. The example will only log the values to keep the example simple, so
make sure to look at the controller's logs.

* interactivespaces.example.activity.arduino.analog.trigger

This Arduino example connects to the Arduino and expects a value from an analog port
to be written. The Arduino code for the example is included in the workbench.

The activity will write on a route if the value read from the Arduino goes over some
value. This gives an example of responding to a hardware event and informing any listening 
activities of the event. If the speech example is activated, it will speak when the
message is sent.

Hardware
========

The following examples show howto use various hardware devices.
They are found in the *examples/hardware* folder of the Workbench.

Bluetooth Comm
--------------

Many wireless devices use Bluetooth for short range wireless communication. 

The example with Interactive Spaces lets you use a Wii Remote as part of your space.
read the activity documentation to see how to use the example.

* interactivespaces.example.activity.wii.remote

Misc
====

The following are a set of examples to show other things you can do with Interactive Spaces.
They are found in the *examples/misc* folder of the Workbench.

Topic Bridges
-------------

Interactive Spaces makes it possible for Live Activities to communicate
with each other. At some point you may find yourself having an event producer
and an event consumer which need to talk to each other, but they were not
written with each other in mind so their messaging protocols are different.

Topic Bridges make it possible for you to translate from one message protocol
to another by writing a tiny script which merely says which field or fields 
from the source message are combined to create the fields of the destination
message.

* interactivespaces.example.activity.bridge.topic

XMPP Chat
---------

Sometimes it would be good if visitors to a space could chat with the space using a
chat client. 

The example with Interactive Spaces will sign into an XMPP-based chat service, such
as Google Chat and echo the chat back to the user chatting with the activity. Instead you
could use information that users send to the space to affect the space.

* interactivespaces.example.activity.chat.xmpp

Music Jukebox
---------

Sometimes you would like to use Interactive Spaces to play music or other audio files.

* interactivespaces.example.activity.music.jukebox

The above will play MP3 files. A folder of music is set in the Live Activity's configuration
and the example will shuffle play MP3s from this folder when activated.

Android
=============

Space Controllers can run on Android devices.

The following examples demonstrate writing activities for Android devices and are found in the
*examples/android* folder of the Workbench.

* interactivespaces.example.activity.android.simple
* interactivespaces.example.activity.android.web
* interactivespaces.example.activity.android.accelerometer

The first merely logs to the Android logs various Activity lifecycle events.
The second will
start up a web browser on the phone which opens a window to the Interactive Spaces
website. The third will read values from the accelerometer on the Android device and
transmit them over a route to any other activities in the space which may be interested.

