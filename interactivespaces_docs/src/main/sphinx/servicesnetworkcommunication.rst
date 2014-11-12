The Interactive Spaces Network Communication Services and Utilities
***************************************

Interactive Spaces comes with a variety of services and classes to help with network communication.
Network communication tools can be used for communicating with software applications that do not run
natively in Interactive Spaces, or can be used for communicating with hardware.

The Interactive Spaces services are easy to use and allow you to let Interactive Spaces take care
of resource management, such as shutting down connections and cleaning up resources when your activity is
done using the service.

TCP Communication
=================

Interactive Spaces has services which simplify the creation and usage of TCP-based network servers and clients.


UDP Communication
=================

Interactive Spaces has services which simplify the creation and usage of UDP-based network servers and clients,
including UDP Broadcast clients.

Web Communication
=================

The Web Server Service
----------------------

The Interactive Spaces Web Server service allows you to easily create web servers with little or no configuration.
The Interactive Spaces Web Server supports

* Standard HTTP GET and POST requests
* Easy to use handlers for static and dynamic content
* Web socket server support
* Automatic MIME type resolution for content being served
* HTTPS connections


The Web Socket Client Service
-----------------------------

The Interactive Spaces Web Socket Client Service makes it easy to create Web Socket clients that can communicate
with remote web socket-based services. The service takes care of the connections to the remote server and handles
all message serialization and deserialization.

The HttpContentCopier
---------------------

The ``HttpContentCopier`` interface provides a way of retrieving content from a remote HTTP server into a string
or into a file in the file system. It can also be used to ``POST`` a file to a remote HTTP server.

Instances of the ``HttpContentCopier`` interface are instances of ``ManagedResource`` and their lifecycle can
be controlled by an Activity through the ``addManagedResource()`` call.

The only current implementation of ``HttpContentCopier`` is 
``interactivespaces.util.web.HttpClientHttpContentCopier``, which uses the Apache Commons HttpClient libraries.

.. code-block:: java

  HttpContentCopier copier = new HttpClientHttpContentCopier();

By default this will give you the possibility of 20 simultaneous connections. If you need more you can specify
how many you need in the constructor. The example below gives you 100 connections.


.. code-block:: java

  HttpContentCopier copier = new HttpClientHttpContentCopier(100);

Normally you then register this as a ManagedResource with your activity.


.. code-block:: java

  HttpContentCopier copier = new HttpClientHttpContentCopier();
  addManagedResource(copier);

Copying Content From a Remote Server
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Using the copier to copy content from a remote HTTP server is very simple. The call

.. code-block:: java

  copier.copy("http://www.foo.com/glorg", getActivityFilesystem().getPermanentDataFile("banana")));

will take the content found at ``http://www.foo.com/glorg`` and copy it to the file ``banana`` in the
activity's permanent data folder. If the URL
is malformed, the remote server does not return a code ``200`` for the content, or something bad happens
during the copy, the call will throw an ``InteractiveSpacesException``.

You can get the remote content as a Java string. For this you use the call

.. code-block:: java

  String content = copier.getContentAsString("http://www.foo.com/glorg");

This assumes the content is in UTF-8. If you need to specify the character set of the content, you can 
specify the charset in the call. For example, if the content is in UTF-16, you could use

.. code-block:: java

  String content = copier.getContentAsString("http://www.foo.com/glorg", Charset.forName("UTF-16"));


Copying Content To a Remote Server
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The copier can also copy content to a remote HTTP server. It does this by creating an 
HTTP ``multipart`` ``POST`` that contains the file as part of the post.

There are two ways to do this. The first takes a file as the source of content to copy.

.. code-block:: java

  copier.copyTo("http://www.foo.com/meef", new File("/var/tmp/foo", "bar", null);

This call will post to the destination URL ``http://www.foo.com/meef``. The file to be copied is
``/var/tmp/foo``. The file will be given the ``POST`` parameter name of ``bar``. No other ``POST`` parameters
will be added.

The call

.. code-block:: java

  Map<String, String> parameters = Maps.newHashMap();
  parameters.put("name", "Me");
  parameters.put("quality", "not so bad");
  copier.copyTo("http://www.foo.com/meef", new File("/var/tmp/foo", "bar", parameters);

will do the same thing as above, but will add the contents of the ``parameters`` map as ``POST`` parameters.

You can also send content from an arbitrary ``java.io.InputStream`` by replacing the second argument of the
``copyTo()`` call with the input stream you want to use. The copier will take care of closing the input stream
after the copy succeeds or fails.


.. code-block:: java

  InputStream in = ... get from somewhere ...
  copier.copyTo("http://www.foo.com/meef", in, "bar", null);


The UrlReader
^^^^^^^^^^^^^

The ``UrlReader`` class provides a safe way to read content accessible with a URL. It
makes sure that all resources are properly cleaned up after the reader completes, even
after error conditions. It provides a ``BufferedReader`` for processing the content, making it
easy to process the content one line at a time.

Email
=====

Interactive Spaces has the ability for you to send and receive email from your spaces.

Sending Email
-------------

Interactive Spaces has the ability to send email through the Mail Sender Service.
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
``config/interactivespaces`` directory. These configurations are usually placed in a
file called ``mail.conf``.

The mail sender service needs to know an SMTP server that it can use
to transport the mail to its destination. The SMTP
server host is set with the ``interactivespaces.service.mail.sender.smtp.host`` configuration
property. The port of the SMTP server is set with the
``interactivespaces.service.mail.sender.smtp.port`` configuration property.

An example would be

::

  interactivespaces.service.mail.sender.smtp.host=172.22.58.11
  interactivespaces.service.mail.sender.smtp.port=25


Receiving Email
---------------

Interactive Spaces can also receive email through the Email Receiver Service.
This service sets up a very simple SMTP server that can receive emails when properly
configured. Event listeners are registered with the service that have methods
that are called when an email is received.

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

A listener can be removed with the ``removeListener()`` method on the service.

For more details about what you can do with the Mail Receiver Service, see the
:javadoc:`interactivespaces.service.mail.receiver.MailReceiverService` 
Javadoc.


Configuring the Mail Receiver Service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Mail Receiver Service normally listens for SMTP traffic on port
``9999``. It can be reconfigured. Configurations for mail services should 
be placed in the``config/interactivespaces`` directory. These configurations are usually 
placed in a file called ``mail.conf``, the same file as the Email Sender configurations.

The port of the SMTP receiver is set with the
``interactivespaces.service.mail.receiver.smtp.port`` configuration property.

An example would be

::

  interactivespaces.service.mail.receiver.smtp.port=10000

Misc
====

The following are network based services that don't fit into a particular category.

Chat Service
------------

The Chat Service provides support for both reading from and writing to chat services.
The current implementation only supports XMPP-based chat.

For more details about what you can do with the Chat Service, see the
:javadoc:`interactivespaces.service.comm.chat.ChatService` 
Javadoc.

Twitter Service
---------------

The Twitter Service provides support for both sending Twitter Status updates and
being notified of any tweets containing a specified hashtag.

For more details about what you can do with the Chat Service, see the
:javadoc:`interactivespaces.service.comm.twitter.TwitterService` 
Javadoc.

Open Sound Control
------------------

Interactive Spaces includes support for building both Open Sound Control servers and clients. The current implementations
only support UDP connections.

For more details about what you can do with the Open Sound Control Services, see the
:javadoc:`interactivespaces.service.control.opensoundcontrol` 
Javadoc.
