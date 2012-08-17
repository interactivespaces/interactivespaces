Services
********

Services provide more advanced functionality to your Activities.
Services provide the ability to do such things as run scripts, 
schedule future events, send email, and receive email.

The Script Service
=================

The Script Service gives you the ability to script portions of your
Activities. Interactive Spaces supports Python, Javascript, and Groovy
out of the box. If you are running on a Mac, you will also have access
to AppleScript.

The easiest way to use the Script Service is to give it the script as a 
string.

First get a Script Service from the Service Registry.

.. code-block:: java

  ScriptService scriptService = 
      getSpaceEnvironment().getServiceRegistry().getService(ScriptService.SERVICE_NAME);

A very simple script would, in the time honored tradition, say *Hello, World*.

.. code-block:: java

  scriptService.executeSimpleScript("python", "print 'Hello, World'");
  
Sometimes you need to give values to your script from your Activity. You
do that by providing a map with bindings in it. These bindings provide
a set of variables in your script which it can use when it runs.

.. code-block:: java

  Map<String, Object> bindings = new HashMap<String, Object>();
  bindings.put("message", "Hello, World");
  
  scriptService.executeScript("python", "print message", bindings);

For more details about what you can do with the Script Service, see the
:javadoc:`interactivespaces.service.script.ScriptService` 
Javadoc.


The Scheduler Service
=================

The Scheduler Service gives you the ability to schedule some sort of
task at some point in the future. These future tasks can be scheduled in
a variety of ways, from one off events at some point in the future
to tasks which repeat on schedules like every Monday, Wednesday, and Friday
at 3AM.


For more details about what you can do with the Scheduler Service, see the
:javadoc:`interactivespaces.service.scheduler.SchedulerService` 
Javadoc.

Mail Services
=============

Interactive Spaces has support for both sending and receiving email.

Mail Sender Service
-------------------


For more details about what you can do with the Mail Sender Service, see the
:javadoc:`interactivespaces.service.mail.sender.MailSenderService` 
Javadoc.

Configuring the Mail Sender Service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Mail Sender Service needs to be configured properly if it going to
be able to send mail.


Mail Receiver Service
---------------------

For more details about what you can do with the Mail Receiver Service, see the
:javadoc:`interactivespaces.service.mail.receiver.MailReceiverService` 
Javadoc.

