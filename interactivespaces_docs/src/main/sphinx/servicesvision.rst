The Interactive Spaces Core Vision Services
********

Interactive Spaces has a variety of services to support image and vision processing, including gesture controllers.

Blob Detection
===============

Interactive Spaces has an easy to use blob detector. This can be used to take an image and detect blobs
meeting some criteria in the image. The result will be a series of bounding rectangles for the blobs as
well as an image map that will say which pixels are associated with which blobs.

For more details about the Blob Detection classes, see the
:javadoc:`interactivespaces.service.image.blob` 
Javadoc.

Depth Cameras
===============

Interactive Spaces has some elementary support for depth cameras. This support includes user tracking.

Currently only the OpenNI libraries are supported.

For more details about  the Depth Camera classes, see the
:javadoc:`interactivespaces.service.image.depth` 
Javadoc. Examples are found in the ``examples/activity/image/depth`` folder of the Interactive Spaces Workbench.

Leap Motion Gesture Detection
=============================

The Leap Motion Gesture sensor is supported in Interactive Spaces through the ``GestureService``. 
This allows activities to detect hand positions and hand gestures.

Video Processing
================

Interactive Spaces contains some elementary video processing capabilities. The examples in the Interactive Spaces
workbench show how to do such operations as face detection and real time edge extraction.

Currently only OpenCV operations are supported through the OpenCV Java API.


For more details about the Video classes, see the
:javadoc:`interactivespaces.service.image.video` 
Javadoc. Examples are found in the ``examples/activity/image/vision`` folder of the Interactive Spaces Workbench.
