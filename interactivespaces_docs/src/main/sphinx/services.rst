Services
********

Services provide more advanced functionality to your Activities.
Services provide the ability to do such things as run scripts, 
schedule future events, send email, and receive email.

Services can be obtained from the Service Registry, which is found in the
Interactive Spaces Environment, in two ways.

Services are obtained by giving a string name for the service. Examples
of service names are ``mail.sender`` and ``speech.synthesis``.

If you want to see if a service exists and your activity can run without
it, you can use the ``getService()`` method of the Service Registry. This method
will either return an instance of the requested service, or ``null``
if the service is not available.

.. code-block:: java

  Service service = 
      getSpaceEnvironment().getServiceRegistry().getService("speech.synthesis");

If you need the service to exist and you want your activity to fail if the service
is not available, you can use the ``getRequiredService()`` method of the Service
Registry. if the service doesn't exist, the method will throw an 
``InteractiveSpacesException``.


.. code-block:: java

  Service service = 
      getSpaceEnvironment().getServiceRegistry().getRequiredService("speech.synthesis");

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

Mail Sender Service
=============

Interactive Spaces has the ability to send email through the mail Sender Service.
With this service you can compose an email and then send it.

An example showing how to use the service is given below.

.. code-block:: java

  MailSenderService mailSenderService = 
      getSpaceEnvironment().getServiceRegistry().getService(MailSenderService.SERVICE_NAME);

  ComposableMailMessage message = new SimpleMailMessage();
  message.setSubject("Greetings");
  message.setFromAddress("the-space@interactivespaces.com");
  message.addToAddress("you@you.com");
  message.setBody("Hello World");
  
  mailSenderService.sendMailMessage(message);
  

For more details about what you can do with the Mail Sender Service, see the
:javadoc:`interactivespaces.service.mail.sender.MailSenderService` 
Javadoc.

Configuring the Mail Sender Service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Mail Sender Service needs to be configured properly if it going to
be able to send mail. Configurations for the mail service should be placed in the
``config/interactivespaces`` directory. It is usually placed in a file called ``mail.conf``.

The mail sender service needs to know an SMTP server which it can use
to transport the mail to its destination. The SMTP
server host is set with the ``interactivespaces.service.mail.sender.smtp.host`` configuration
property. The port of the SMTP server is set with the
``interactivespaces.service.mail.sender.smtp.port`` configuration property.

An example would be

::

  interactivespaces.service.mail.sender.smtp.host=172.22.58.11
  interactivespaces.service.mail.sender.smtp.port=25
  

Mail Receiver Service
=============

Interactive Spaces can also receive email through the Email Receiver Service.
This service sets up a very simple SMTP server which can receive emails when properly
configured. Event listeners are registered with the service which have methods
which are called when an email is received.

n example showing how to use the service is given below.

.. code-block:: java

  MailReceiverService mailReceiverService = 
      getSpaceEnvironment().getServiceRegistry().getService(MailReceiverService.SERVICE_NAME);

  mailReceiverService.addListener(new MailReceiverListener() {
    public void onMailMessageReceive(MailMessage message) {
      getLog().info("Received mail from " + message.getFromAddress();
    }
  });

The example listener merely prints the from address from the received email
and nothing else.

A listener can me removed with the ``removeListener()`` method on the service.

For more details about what you can do with the Mail Receiver Service, see the
:javadoc:`interactivespaces.service.mail.receiver.MailReceiverService` 
Javadoc.


Configuring the Mail Receiver Service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Mail Receiver Service normally listens for SMTP traffic on port
``9999``. It can be reconfigured. Configurations for mail services should 
be placed in the``config/interactivespaces`` directory, usually placed in
a file called ``mail.conf``.

The port of the SMTP receiver is set with the
``interactivespaces.service.mail.receiver.smtp.port`` configuration property.

An example would be

::

  interactivespaces.service.mail.receiver.smtp.port=10000

Speech Synthesis Service
===============

The Speech Synthesis Service allows your activities to speak. The service takes
a string of text which is then spoken by the computer which contains the
Space Controller running the service.

To use the Speech Synthesis Service, you must get an instance of a 
``SpeechSynthesisPlayer``. These players use various system resources
which must be released when you are through with the player. One way of
handling this would be to allocate a player in ``onActivitySetup()``
and add it as a Managed Resource for the Activity. Then Interactive Spaces
will automatically clean up any resources used by the player when your
activity stops running.

As an example, here is the first part of your Activity, showing the player
instance variable and the code to obtain a player. Notice the player is
added as a Managed Resource.

.. code-block:: java

  private SpeechSynthesisPlayer speechPlayer;

  @Override
  public void onActivitySetup() {
    SpeechSynthesisService speechSynthesisService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            SpeechSynthesisService.SERVICE_NAME);

    speechPlayer = speechSynthesisService.newPlayer();

    addManagedResource(speechPlayer);
  }

Now making your activity speak is easy, you just use the ``speak`` method
on the player.

.. code-block:: java

  speechPlayer.speak("Hello, world.", true);

The second argument for the ``speak()`` method determines if the method will
block while the text is being spoken, or if it will return immediately
with the text spoken asynchronously. if the value is ``true`` the method
will block, if it is ``false`` the method will return immediately.

For more details about what you can do with the Speech Synthesis Service, see the
:javadoc:`interactivespaces.service.speech.synthesis.SpeechSynthesisService` 
Javadoc.

Chat Service
===============

The Chat Service provides support for both reading from and writing to chat services.
The current implementation only supports XMPP-based chat.

For more details about what you can do with the Chat Service, see the
:javadoc:`interactivespaces.service.comm.chat.ChatService` 
Javadoc.

Twitter Service
===============

The Twitter Service provides support for both sending Twitter Status updates and
being notified of any tweets containing a specified hashtag.

For more details about what you can do with the Chat Service, see the
:javadoc:`interactivespaces.service.comm.twitter.TwitterService` 
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

