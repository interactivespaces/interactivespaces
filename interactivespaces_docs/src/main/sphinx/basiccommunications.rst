Basic Interactive Spaces Communications
***************************************

Bringing Live Activities up and down is nice and all, but if you really
want interesting behaviors in your space, you need your Live Activities
to communicate with each other.

Interactive Spaces has uses ROS to provide communication. ROS gives the
most power, but it is somewhat complex to use. So Interactive Spaces
provides a simpler-to-use mechanism based on the popular communication
format JSON which is used in web applications for transfering name/value
pairs between Live Activities. This mechanism uses ROS under the covers,
but other than a few configuration parameters that are ROS specific,
you need never think about ROS.
