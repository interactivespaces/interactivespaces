History and Design Philosophy 
**************

History
=======

Interactive Spaces (IS) was created for an Experience Center on the Mountain View 
Campus of Google, which was to contain interactive displays. IS is the software 
glue that holds the activities in the Center together. It was started in 2011 and
the Center went live in 2012.

The interactive displays in the Center were a collaboration between Google and 
the  
`Rockwell Group's LAB division <http://www.rockwellgroup.com/lab/>`_.
Google
wrote the IS code. Rockwell wrote the activities which run in the Center using 
a combination of the Interactive Spaces APIs and also wrote some of the software 
for some of the pieces that weren't supported by IS for the initial release of 
that project.

The initial implementation didn't have everything envisioned for the project,
but contained enough of an API for all of the initial installations planned for 
the Center and many of the pieces needed to run a production installation.

Design Philosophy
================

Interactive Spaces was designed with several concepts in mind: 

* First, it needed to run a production Center like the one in Mountain View.
  So it had to support deployment, control, maintenance, and monitoring of 
  applications which run in an interactive space, much like a small data center. 
* It had to be as portable as possible, hence the choice of Java. 
* It should be able to leverage code meant for controlling physical devices 
  from Centers, part of the reason that it was decided that the main communication 
  protocol should be the `Robot Operating System (ROS) <http://www.ros.org>`_.
* It should be easy to reconfigure and redeploy an entire space quickly, part of 
  the reason for the use of OSGi.
* The learning curve of IS should be shallow in the beginning and gradually get 
  steeper as a developer needs more functionality. It should be an onion, where 
  the developer keeps peeling layers away until they find the level where they 
  have the power they need.
* IS should provide as many of the sorts of functionality that an interactive 
  designer and developer would need out of the box. Hence it comes with web 
  servers, easy to use thread pools, remote communication, job schedulers, and a 
  whole host of other functionality.
* It should be possible to implement activities in languages other than Java, 
  and have some sort of control over activities which are not implemented as
  IS activities.
