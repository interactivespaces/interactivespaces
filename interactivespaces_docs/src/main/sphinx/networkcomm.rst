The Interactive Spaces Network Communication Support
***************************************

Interactive Spaces comes with a variety of services and classes to help with network communication.
Network communication tools can be used for communicating with software applications that do not run
natively in Interactive Spaces, or can be used for communicating with hardware.

TCP Communication
=================


UDP Communication
=================

Web Communication
=================

The Web Socket Client Service
=============================

The HttpContentCopier
==============

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
------------------------------------

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
------------------------------------

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
=============



