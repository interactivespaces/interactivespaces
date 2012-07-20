Activity Configurations
***********************

One of the more powerful parts of Interactive Spaces is the Activity Configuration.


Common Configuration Parameters
===============================

There are some values found in every activity configuration which you might
find useful.

*activity.installdir*

  This is the directory where the live activity was installed. It will contain
  the base activity configuration, the activity descriptor, and all other resources
  which were part of the Activity.

*activity.logdir*

  This is the directory which stores the live  activity's log files.

*activity.datadir*

  This is a private directory where the live activity can store data which needs
  to persist for some time. This data will only be deleted if the live
  activity is deployed again.

*activity.tmpdir*

  This is a private directory where the live activity can store temporary data. 
  This data can be deleted whenever the live activity is not running.

*system.datadir*

  This folder is writable by any live activity. Any files here will persist 
  between controller shutdowns and startups, and live activity deployments. 

*system.tmpdir*

  This folder is writable by any live activity. Files here can be deleted during
  controller startup or after controller shutdown.
