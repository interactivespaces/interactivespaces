Cookbook
**************

The following cookbook will point out various useful recipes for things you might
want to do with Interactive Spaces.

Repeating An Action Over and Over
=========

Sometimes you need to repeat some action over and over again. It is a bad
idea to never exit any of your Live Activity's lifecycle methods, so another
way is needed instead of a loop in a lifecycle method which never exits.

Interactive Spaces includes thread pools, which give you the ability to
run multiple processes at the same time. In fact, you should never create
a thread on your own, you should only use Interactive Space's thread pools
as you can prevent a controller from being able to be easily shut down
if you create your own threads.

The following examples show how to use the thread pools in a variety of
languages. The task will be a simple one, print *Hello World* every 5 seconds.
The very first time it will happen will be 10 seconds in the future.

The examples start up the thread when the Activity is activated. It is then
shut down when the activity is deactivated and also when the activity is
shut down. Notice the use of the *onActivityCleanup()* method, which will be
called no matter what happens during shutdown.

Notice that the cleanup method calls *cancel(false)*. This means to let the
method finish whatever it is doing before canceling any further iterations.

Python
------

.. code-block:: python

  class RepeatingActionExampleActivity(BaseActivity):
      def onActivityActivate(self):
          class Foo(Runnable):
              def run(self):
                  print "Hello, world"

          self.repeater = self.spaceEnvironment.executorService.scheduleWithFixedDelay(Foo(), 10, 5, TimeUnit.SECONDS)

      def onActivityDeactivate(self):
          self.repeaterCleanup()

      def onActivityCleanup(self):
          self.repeaterCleanup()

      def repeaterCleanup(self):
          if self.repeater:
              self.repeater.cancel(False)
              self.repeater = None

              
Cloning A Space
=========

A complex installation may sometimes have repetitive elements in it, where
those elements are somewhat complex themselves. For example, there may be a 
Space with child Spaces, many of them with Live Activity Groups containing
Live Activities which need to run on different Space Controllers. Duplicating
these complex networks manually could be quite timeconsuming.

This cloning operation can be simplified by cloning the Space. This will in turn
clone all the child objects.

You set the *namePrefix* to be a name which will be prefixed to all 
generated objects.

You can map how you want the Space Controllers in the original Space to map
into the Space Controllers in cloned Live Activities by using a
*controllerMap*. If there is no map supplied, or no mapping found 
for a particular Space Controller in the *controllerMap*, 
the Space Controllers used in the clone will be the same as the Space 
Controller in the original.

The following examples show how to clone a space in a variety of
scripting languages. They will prepend *Clone Test* to all cloned objects
and map only 1 Space Controller to another Space Controller.

Groovy
------

.. code-block:: groovy

  def cloner = new interactivespaces.master.server.services.SpaceCloner(activityRepository, spaceRepository);
  cloner.namePrefix = 'Clone Test';

  def controller1 = controllerRepository.getSpaceControllerByUuid('8826e47c-a08a-4b3c-a320-06f420a33904');
  def controller2 = controllerRepository.getSpaceControllerByUuid('1a32c84a-3786-4329-9ae3-31c9424823d5');
  cloner.controllerMap = [(controller1): controller2];

  def space = spaceRepository.getSpaceById('cfdce17a-d841-485a-9214-e2e47e4865a6');
  cloner.cloneSpace(space);
  cloner.saveClones();              