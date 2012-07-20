Services
********

Services provide more advanced functionality to your Activities.
Services provide the ability to do such things as run scripts, 
schedule future events, send email, and receive email.

Getting Services
================

Services are obtained through the Service Registry, which is available
from the *spaceEnvironment*.

If your Live Activity is written in Java, you can get the Service
Registry with the following code

.. code-block:: java

  getSpaceEnvironment().getServiceRegistry()

Once you have the Service Registry, you use the *getService()* method to
get an actual service. For instance, if you want the scripting service you
would use the following Java code

.. code-block:: java

  ScriptService scriptService = 
      getSpaceEnvironment().getServiceRegistry().getService("scripting");

A safer way to do it would be

.. code-block:: java

  ScriptService scriptService = 
      getSpaceEnvironment().getServiceRegistry().getService(ScriptService.SERVICE_NAME);
  
You can get more details about the Service Registry in the
:javadoc:`interactivespaces.service.ServiceRegistry` 
Javadoc.


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
