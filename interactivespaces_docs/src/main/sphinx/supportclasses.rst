Support Classes
***************

Interactive Spaces comes with a lot of builtin support for various common
things you would want to do.

Persisted Maps
==============

The *SimpleMapPersister* interface provides you with the ability to
very simply persist a map of key/value pairs. The values can be lists
or maps themselves. You can find detailed documentation in the
:javadoc:`interactivespaces.util.data.persist.SimpleMapPersister` Javadoc.
One particular implementation, *JsonSimpleMapPersister*, stores the 
maps as JSON files. You can find detailed documentation in the
:javadoc:`interactivespaces.util.data.persist.JsonSimpleMapPersister` 
Javadoc.

Suppose that you want to create a map to persist in a subdirectory of the
controller-wide data directory in an Activity. You can get that directory
from the filesystem property of your Spaces Environment.

.. code-block:: java

  File basedir = getSpaceEnvironment().getFilesystem().getDataDirectory("maps");
  SimpleMapPersister persister = new JsonSimpleMapPersister(basedir);
  
  persister.putMap("foo", map);
  
Here *map* is assumed to be a map of data. Because the *JsonSimpleMapPersister*
is being used, a file in *data/maps/foo.json* relative to where you
installed your controller will be created with the contents of *map*
serialized into the file as JSON.

.. code-block:: java
  
  Map<String, Object> map = persister.getMap("foo");
  
would then read the map from disk.

The SimpleMapPersister classes are thread-safe. They permit multiple
simultaneous readers and only 1 writer. Any readers will block the
writer and the writer will block any readers. The locks are fair, so
continuing to add lots of readers will not block a writer forever.



  