The Master API
**************

The Master exposes a web-based API which allows you to write your own web 
applications to control the master. The interface is mostly REST-ful, and
returns results in JSON.

Common Features of the API
==========================

The API has several features which are common to all methods

API URL
------

Every API URL fill be prefixed with the following

::

  http://spacemaster:8080/interactivespaces/

where *spacemaster* is the domain name of the machine running the Interactive
Spaces Master.

For example, the API call to obtain all Live Activities know by the system is

::

  http://spacemaster:8080/interactivespaces/liveactivity/all.json


Results
-------

The JSON results returned have features in common.

Success
~~~~~~~

The JSON result for a successful API command which has no return data
will be

::

  { 
    "result": "success"
  }

If there is data to be returned, the results will be

::

  { 
    "result": "success", 
    "data": result data
  }

where *result data* will be a JSON object giving the result.

Failure
~~~~~~~

The JSON result for a failed API command will be

::

  { 
    "result": "failure", 
    "message": content
  }
  
where *content* will be a description of what failed.


Space Controllers
===============

The Space Controller API allows you to get a collection of all Space Controllers 
known by the Master. Once you have the IDs, you can then connect and disconnect
from them.

Getting All Space Controllers
---------------------------

The Master API call to get the list of all Space Controllers is

::

  /spacecontroller/all.json
  
This returns a JSON list in the *data* portion of a successful JSON response
of the following form

::

  {
    "id": id,
    "uuid": uuid,
    "name": name,
    "description": description,
  }
    
The names and a description of their values is given below.

The information includes a small amount of information about the Activity portion
of the Live Activity.

+------------------+-------------------------------+
| id               | The ID of the Controller      |
+------------------+-------------------------------+
| uuid             | The UUID of the Controller    |
+------------------+-------------------------------+
| name             | The name of the Controller    |
+------------------+-------------------------------+
| description      | The description of the        |
|                  | Controller                    |
+------------------+-------------------------------+

Getting the Status of All Space Controllers
-------------------------------------------

The full status from all space controllers is requested by the API command

::

  /spacecontroller/all/status.json

The JSON returned will the simple JSON success result.

Connecting to a Space Controller
----------------------------

A Space Controller is connected to by the API command

::

  /spacecontroller/id/connect.json
  
where *id* is the ID of the Controller. Be sure you use the ID
of the Controller, not the UUID.

The JSON returned will the simple JSON success result.


Disconnecting from a Space Controller
----------------------------

A Space Controller is disconnected from the master by the API command

::

  /spacecontroller/id/disconnect.json
  
where *id* is the ID of the Controller. Be sure you use the ID
of the Controller, not the UUID.

The JSON returned will the simple JSON success result.

Shutting Down All Activities on a Space Controller
----------------------------

All Live Activities on Space Controller can be shut down by the API command

::

  /spacecontroller/id/activities/shutdown.json
  
where *id* is the ID of the Controller. Be sure you use the ID
of the Controller, not the UUID.

The JSON returned will the simple JSON success result.

Shutting Down a Space Controller
----------------------------

A Space Controller can be remotely shut down by the API command

::

  /spacecontroller/id/shutdown.json
  
where *id* is the ID of the Controller. Be sure you use the ID
of the Controller, not the UUID.

The JSON returned will the simple JSON success result.

Deploying all Known Live Activities
----------------------------

All known Live Activities on the controller are deployed by the API command

::

  /spacecontroller/id/deploy.json
  
where *id* is the ID of the Controller. Be sure you use the ID
of the Controller, not the UUID.

The JSON returned will the simple JSON success result.


Activities
===============

The Activities API allows you to get a collection of all Activities 
known by the Master. Once you have the IDs, you can then deploy all known
Live Activity instances using that Activity.

Getting All Activities
---------------------------

Suppose the Master is running on your local host. The URL to get the list
of all Activities is

::

  /activity/all.json
  
This returns a JSON list in the *data* portion of a successful JSON response
of the following form

::

  {
    "id": id,
    "identifyingName": identifyingName,
    "version": version,
    "name": name,
    "description": description,
    "lastUploadDate", lastUploadDate,
    "metadata": metadata
  }
    
The names and a description of their values is given below.


+------------------+--------------------------------+
| id               | The ID of the Activity         |
+------------------+--------------------------------+
| identifyingName  | The identifying name of the    |
|                  | Activity                       |
+------------------+--------------------------------+
| version          | The version of the             |
|                  | Activity                       |
+------------------+--------------------------------+
| name             | The name of the Activity       |
+------------------+--------------------------------+
| description      | The description of the         |
|                  | Activity                       |
+------------------+--------------------------------+
| metadata         | The metadata of the            |
|                  | Activity                       |
+------------------+--------------------------------+
| lastUploadDate   | The number of milliseconds     |
|                  | since January 1, 1970 that     |
|                  | the Activity was last uploaded |
+------------------+--------------------------------+

You can add a query parameter called *filter* onto the URL which will
restrict the activities returned. For details on how to write the filters,
For details on the expression language that you write filters in,
see :ref:`expression-language-chapter-label`. The context of the filter
will be the activity itself, so you can refer to any of the above
properties directly.

For example

::

  metadata['author'] eq 'Keith Hughes'
  
will return all activities if its metadata contains an *author* field with
*Keith Hughes* as its value.

Deploying a Activity
----------------------------

All out of date Live Activity instances of a Activity are
deployed by the API command

::

  /activity/id/deploy.json
  
where *id* is the ID of the Activity.

The JSON returned will the simple JSON success result.


Live Activities
===============

The Live Activities API allows you to get a collection of all Live Activities 
known by the Master. Once you have the IDs, you can then deploy, configure, start,
stop, activate, deactivate, and get the status on all live activities.

.. _live-activity-all-label:

Getting All Live Activities
---------------------------

Suppose the Master is running on your local host. The URL to get the list
of all Live Activities is

::

  /liveactivity/all.json
  
This returns a JSON list in the *data* portion of a successful JSON response
where each entry in the list will be of the form

::

  {
    "id": id,
    "uuid": uuid,
    "name": name,
    "description": description,
    "status" : status,
    "statusMessage" : statusMessage,
    "metadata" : metadata
    "activity": {
      "identifyingName": identifyingName,
      "version": version,
      "metadata": activityMetadata
    },
    "controller": {
      "id": controllerId,
      "name": controllerName
    }
  }
    
The names and a description of their values is given below.

The information includes a small amount of information about the Activity portion
of the Live Activity.

+------------------+-------------------------------+
| id               | The ID of the Live Activity   |
+------------------+-------------------------------+
| uuid             | The UUID of the Live Activity |
+------------------+-------------------------------+
| name             | The name of the Live Activity |
+------------------+-------------------------------+
| description      | The description of the Live   |
|                  | Activity                      |
+------------------+-------------------------------+
| metadata         | The metadata of the Live      |
|                  | Activity                      |
+------------------+-------------------------------+
| identifyingName  | The identifying name of the   |
|                  | Activity                      |
+------------------+-------------------------------+
| version          | The version of the            |
|                  | Activity                      |
+------------------+-------------------------------+
| activityMetadata | The metadata of the           |
|                  | Activity                      |
+------------------+-------------------------------+
| controllerId     | The ID of the controller      |
+------------------+-------------------------------+
| controllerName   | The name of the controller    |
+------------------+-------------------------------+

See :ref:`live-activity-status-label` for details on the status fields.

You can add a query parameter called *filter* onto the URL which will
restrict the activities returned. For details on how to write the filters,
For details on the expression language that you write filters in,
see :ref:`expression-language-chapter-label`. The context of the filter
will be the activity itself, so you can refer to any of the above
properties directly.

For example

::

  metadata['author'] eq 'Keith Hughes'
  
will return all live activities whose metadata contains an *author* field with
*Keith Hughes* as its value.

Viewing a Live Activity
----------------------------

You can get the basic information for a Live Activity by the API command

::

  /liveactivity/id/view.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live Activity, not the UUID.

  
This returns JSON in the *data* portion of a successful JSON response
of the form

::

  {
    "id": id,
    "uuid": uuid,
    "name": name,
    "description": description,
    "status" : status,
    "statusMessage" : statusMessage,
    "metadata" : metadata
    "activity": {
      "identifyingName": identifyingName,
      "version": version,
      "metadata": activityMetadata
    },
    "controller": {
      "id": controllerId,
      "name": controllerName
    }
  }
    
The names and a description of their values is given below.

The information includes a small amount of information about the Activity portion
of the Live Activity.

+------------------+-------------------------------+
| id               | The ID of the Live Activity   |
+------------------+-------------------------------+
| uuid             | The UUID of the Live Activity |
+------------------+-------------------------------+
| name             | The name of the Live Activity |
+------------------+-------------------------------+
| description      | The description of the Live   |
|                  | Activity                      |
+------------------+-------------------------------+
| metadata         | The metadata of the Live      |
|                  | Activity                      |
+------------------+-------------------------------+
| identifyingName  | The identifying name of the   |
|                  | Activity                      |
+------------------+-------------------------------+
| version          | The version of the            |
|                  | Activity                      |
+------------------+-------------------------------+
| activityMetadata | The metadata of the           |
|                  | Activity                      |
+------------------+-------------------------------+
| controllerId     | The ID of the controller      |
+------------------+-------------------------------+
| controllerName   | The name of the controller    |
+------------------+-------------------------------+

See :ref:`live-activity-status-label` for details on the status fields.

Configuring a Live Activity
----------------------------

The configuration for a Live Activity is sent to the
remote installation by the API command

::

  /liveactivity/id/configure.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live Activity, not the UUID.

The JSON returned will the simple JSON success result.

Getting the Configuration of a Live Activity
----------------------------

The configuration for a Live Activity is obtained by the API command

::

  /liveactivity/id/configuration.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

This returns a JSON map in the *data* portion of a successful JSON response.
The map will be keyed by the name of a configuration parameter. The
map value will be the value for the configuration parameter.


::

  {
    "param1": "value of param 1",
    "param2": "value of param 2"
  }

Setting the Configuration of a Live Activity
----------------------------

The configuration for a Live Activity be set by the API command

::

  /liveactivity/id/configuration.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

This must be a POST call with type *application/json*. The body of
post should be a JSON map where the keys are the names of configuration
parameters and the values will be the value of the associated parameter.

::

  {
    "param1": "value of param 1",
    "param2": "value of param 2"
  }


The JSON returned will the simple JSON success result.

Deploying a Live  Activity
----------------------------

A Live Activity is deployed by the API command

::

  /liveactivity/id/deploy.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

The JSON returned will the simple JSON success result.

Starting Up a Live Activity
----------------------------

A Live Activity is started up by the API command

::

  /liveactivity/id/startup.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

The JSON returned will the simple JSON success result.  

Activating a Live Activity
----------------------------

A Live Activity is activated by cthe API command

::

  /liveactivity/id/activate.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

The JSON returned will the simple JSON success result.

Deactivating a Live Activity
----------------------------

A Live Activity is deactivated by calling the API command

::

  /liveactivity/id/deactivate.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

The JSON returned will the simple JSON success result.

Shutting Down a Live Activity
----------------------------

A Live Activity is shut down calling the API command

::

  /liveactivity/id/shutdown.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live  Activity, not the UUID.

The JSON returned will the simple JSON success result.
  
.. _live-activity-status-label:

Getting the Status of a Live Activity
---------------------------

The status of a Live Activity is obtained by calling the API command

::

  /liveactivity/id/status.json
  
where *id* is the ID of the Live Activity. Be sure you use the ID
of the Live Activity, not the UUID.

The JSON success result with a *data* field which contains
the following result.

::

  { 
    "status" : status,
    "statusMessage" : statusMessage
  }

*status* will be one of the following.

**space.activity.state.unknown**

  The status is unknown

**space.activity.state.deployment.attempt**

  A deployment is being attempted

**space.activity.state.deployment.failure**

  A deployment attempt has failed

**space.activity.state.ready**

  The Live Activity is ready to to run

**space.activity.state.start.attempt**

  A startup is being attempted

**space.activity.state.start.failure**

  A startup attempt has failed

**space.activity.state.running**

  The Live Activity is running

**space.activity.state.activate.attempt**

  An activation is being attempted

**space.activity.state.activate.failure**

  An activation attempt has failed

**space.activity.state.active**

  The Live Activity is active

**space.activity.state.deactivate.attempt**

  A deactivation is being attempted

**space.activity.state.deactivate.failure**

  A deactivation attempt has failed

**space.activity.state.shutdown.attempt**

  A shutdown is being attempted

**space.activity.state.shutdown.failure**

  A shutdown attempt has failed

**space.activity.state.crash**

  The Live Activity has crashed

*statusMessage* will be the status in a more human-readable format.


Live Activity Groups
===============

The Live Activity Groups API allows you to get a collection of all 
Live Activity Groups known by the Master. Once you have the IDs, 
you can then deploy, configure, start, stop, activate, and 
deactivate all Groups.

Getting All Live Activity Groups
---------------------------

The API call to get the list of all Live Activity Groups is

::

  /liveactivitygroup/all.json
  
This returns a JSON list in the *data* portion of a successful JSON response
of the following form

::

  {
    "id": id,
    "name": name,
    "description": description,
    "metadata", metadata
  }
    
The names and a description of their values is given below.

+------------------+-------------------------------+
| id               | The ID of the Group           |
+------------------+-------------------------------+
| name             | The name of the Group         |
+------------------+-------------------------------+
| description      | The description of the Group  |
+------------------+-------------------------------+
| metadata         | The metadata of the Group     |
+------------------+-------------------------------+

You can add a query parameter called *filter* onto the URL which will
restrict the Groups returned. For details on how to write the filters,
For details on the expression language that you write filters in,
see :ref:`expression-language-chapter-label`. The context of the filter
will be the Group itself, so you can refer to any of the above
properties directly.

For example

::

  metadata['author'] eq 'Keith Hughes'
  
will return all Groups whose metadata contains an *author* field with
*Keith Hughes* as its value.

Viewing a Live Activity Group
-----------------------------

The URL to get information about a specific Live Activity Group is

::

  /liveactivitygroup/id/view.json
 
where *id* is the ID of the Group.

This returns a JSON object in the *data* portion of a successful JSON response
of the form
 
::

  {
    "id": id,
    "name": name,
    "description": description,
    "liveActivities": liveActivities,
  }

    
The names and a description of their values is given below.

+------------------+-------------------------------+
| id               | The ID of the Group           |
+------------------+-------------------------------+
| name             | The name of the Group         |
+------------------+-------------------------------+
| description      | The description of the Group  |
+------------------+-------------------------------+
| metadata         | The metadata of the Group     |
+------------------+-------------------------------+

The *liveActivities* field will be a list of information for each Live
Activity in the Group.
See :ref:`live-activity-all-label` to see the data that will be given for
each Live Activity.


Requesting the Status of all Live Activities a Live Activity Group
------------------------------------------------------------------

A request to get a status update of all Live Activities in the Group can be initiated
by the API command

::

  /liveactivitygroup/id/liveactivitystatus.json
  
where *id* is the ID of the Group.

The JSON returned will the simple JSON success result.


Deploying a Live Activity Group
----------------------------

A Live Activity Group is deployed by the API command

::

  /liveactivitygroup/id/deploy.json
  
where *id* is the ID of the Group.

The JSON returned will the simple JSON success result.

Starting Up a Live Activity Group
----------------------------

A Live Activity Group is started up by the API command

::

  /liveactivitygroup/id/startup.json
  
where *id* is the ID of the Group.

The JSON returned will the simple JSON success result.  

Activating a Live Activity Group
----------------------------

A Live Activity Group is activated by cthe API command

::

  /liveactivitygroup/id/activate.json
  
where *id* is the ID of the Group.

The JSON returned will the simple JSON success result.

Deactivating a Live Activity Group
----------------------------

A Live Activity Group is deactivated by calling the API command

::

  /liveactivitygroup/id/deactivate.json
  
where *id* is the ID of the Group.

The JSON returned will the simple JSON success result.

Shutting Down a Live Activity Group
----------------------------

A Live Activity Group is shut down calling the API command

::

  /liveactivitygroup/id/shutdown.json
  
where *id* is the ID of the Group.

The JSON returned will the simple JSON success result.

Spaces
======

The Spaces API allows you to get a collection of all Spaces 
known by the Master. Once you have the IDs, you can then deploy, 
configure, start, stop, activate, and deactivate all Spaces.

Getting All Spaces
------------------

The API call to get the list of all Spaces is

::

  /space/all.json
  
This returns a JSON list in the *data* portion of a successful JSON response
of the following form

::

  {
    "id": id,
    "name": name,
    "description": description,
    "metadata", metadata
  }
    
The names and a description of their values is given below.

+------------------+-------------------------------+
| id               | The ID of the Space           |
+------------------+-------------------------------+
| name             | The name of the Space         |
+------------------+-------------------------------+
| description      | The description of the Space  |
+------------------+-------------------------------+
| metadata         | The metadata of the Space     |
+------------------+-------------------------------+

You can add a query parameter called *filter* onto the URL which will
restrict the Spaces returned. For details on how to write the filters,
For details on the expression language that you write filters in,
see :ref:`expression-language-chapter-label`. The context of the filter
will be the activity itself, so you can refer to any of the above
properties directly.

For example

::

  metadata['author'] eq 'Keith Hughes'
  
will return all Spaces whose metadata contains an *author* field with
*Keith Hughes* as its value.

Viewing a Space
-----------------------------

The URL to get information about a specific Space is

::

  /space/id/view.json
 
where *id* is the ID of the Space.

This returns a JSON object in the *data* portion of a successful JSON response
of the form
 
::

  {
    "id": id,
    "name": name,
    "description": description,
    "metadata", metadata,
    "liveActivityGroups": liveActivityGroups,
    "subspaces", subspaces
  }

    
The names and a description of their values is given below.

+------------------+-------------------------------+
| id               | The ID of the Space           |
+------------------+-------------------------------+
| name             | The name of the Space         |
+------------------+-------------------------------+
| description      | The description of the Space  |
+------------------+-------------------------------+
| metadata         | The metadata of the Space     |
+------------------+-------------------------------+

The *liveActivityGroups* field will be a list of information for each Live
Activity Group in the Space. Each list element will have the form

::

  {
    "id": groupId,
    "name": groupName,
    "description": groupDescription,
    "metadata", groupMetadata
  }

    
The names and a description of their values is given below.

+-----------------------+-------------------------------+
| groupId               | The ID of the Group           |
+-----------------------+-------------------------------+
| groupName             | The name of the Group         |
+-----------------------+-------------------------------+
| groupDescription      | The description of the Group  |
+-----------------------+-------------------------------+
| groupMetadata         | The metadata of the Group     |
+-----------------------+-------------------------------+

The *subspaces* field will be a list of information for each child
Space in the Space. Each list element will have the form

::

  {
    "id": subspaceId,
    "name": subspaceName,
    "description": subspaceDescription,
    "metadata", subspaceMetadata
  }

    
The names and a description of their values is given below.

+--------------------------+-------------------------------------+
| subspaceId               | The ID of the child Space           |
+--------------------------+-------------------------------------+
| subspaceName             | The name of the child Space         |
+--------------------------+-------------------------------------+
| subspaceDescription      | The description of the child Space  |
+--------------------------+-------------------------------------+
| subspaceMetadata         | The metadata of the child Space     |
+--------------------------+-------------------------------------+


Requesting the Status of all Live Activities in a Space
-------------------------------------------------------

A request to get a status update of all Live Activities in a Space can be initiated
by the API command

::

  /space/id/liveactivitystatus.json
  
where *id* is the ID of the Space.

The Live Activities in the Space is the set of all Live Activities in all 
Live Activity Groups in the space and all subspaces of the Space, and their subspaces.

The JSON returned will the simple JSON success result.

Deploying a Space
----------------------------

A Space is deployed by the API command

::

  /space/id/deploy.json
  
where *id* is the ID of the Space. 

Deploying a Space ultimately
deploys all Live Activities defined in all Live Activity Groups
in the Space and all child Spaces.

The JSON returned will the simple JSON success result.

Configuring a Space
----------------------------

A Space is configured by the API command

::

  /space/id/configure.json
  
where *id* is the ID of the Space. 

Configuring a Space ultimately
configures all Live Activities defined in all Live Activity Groups
in the Space and all child Spaces.

The JSON returned will the simple JSON success result.

Starting Up a Space
----------------------------

A Space is started up by the API command

::

  /space/id/startup.json
  
where *id* is the ID of the Space. 

Starting up a Space ultimately
starts up all Live Activities defined in all Live Activity Groups
in the Space and all child Spaces.

The JSON returned will the simple JSON success result.  

Activating a Space
----------------------------

A Space is activated by the API command

::

  /space/id/activate.json
  
where *id* is the ID of the Space. 

Activating a Space ultimately
activates all Live Activities defined in all Live Activity Groups
in the Space and all child Spaces.

The JSON returned will the simple JSON success result.

Deactivating a Space
----------------------------

A Space is deactivated by calling the API command

::

  /space/id/deactivate.json
  
where *id* is the ID of the Space. 

Deactivating a Space ultimately
deactivates all Live Activities defined in all Live Activity Groups
in the Space and all child Spaces.

The JSON returned will the simple JSON success result.

Shutting Down a Space
----------------------------

A Space is shut down calling the API command

::

  /space/id/shutdown.json
  
where *id* is the ID of the Space. 

Shutting down a Space ultimately
shuts down all Live Activities defined in all Live Activity Groups
in the Space and all child Spaces.

The JSON returned will the simple JSON success result.


Named Scripts
=============

The Named Scripts API allows you to get a collection of all 
scripts known by the Master. Once you have the IDs, you can then 
run the scripts.

Getting All Named Scripts
-------------------------

The API call to get the list of all Named Scripts is

::

  /admin/namedscript/all.json
  
This returns a JSON list in the *data* portion of a successful JSON response
of the following form

::

  {
    "id": id,
    "name": name,
    "description": description,
  }
    
The names and a description of their values is given below.

+------------------+-------------------------------+
| id               | The ID of the Script          |
+------------------+-------------------------------+
| name             | The name of the Script        |
+------------------+-------------------------------+
| description      | The description of the Script |
+------------------+-------------------------------+

Running a Named Script
----------------------

A Named Script is run by the API command

::

  /admin/namedscript/id/run.json
  
where *id* is the ID of the Script.

The JSON returned will the simple JSON success result.
