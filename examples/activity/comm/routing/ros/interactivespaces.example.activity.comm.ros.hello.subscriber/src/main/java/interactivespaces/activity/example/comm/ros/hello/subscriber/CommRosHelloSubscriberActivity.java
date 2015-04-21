/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.activity.example.comm.ros.hello.subscriber;

import interactivespaces.activity.component.ros.RosActivityComponent;
import interactivespaces.activity.impl.BaseActivity;

import org.ros.message.MessageListener;
import org.ros.node.topic.Subscriber;
import std_msgs.ColorRGBA;

/**
 * An Interactive Spaces Java-based activity that listens on a ROS topic and prints the incoming messages.
 *
 * <p>
 * One way to test is to have the ROS command line tools installed and to use the command
 *
 * <pre>
 * rostopic pub /example/ros std_msgs/ColorRGBA '{r: 12, g: 13, b: 14, a: 45}'
 * </pre>
 *
 * @author Keith M. Hughes
 */
public class CommRosHelloSubscriberActivity extends BaseActivity {

  /**
   * The ROS activity component.
   */
  private RosActivityComponent rosActivityComponent;

  /**
   * The topic name for the message.
   */
  private String topicName;

  /**
   * The OS subscriber for the message.
   */
  private Subscriber<ColorRGBA> subscriber;

  @Override
  public void onActivitySetup() {
    rosActivityComponent = addActivityComponent(RosActivityComponent.COMPONENT_NAME);

    topicName = getConfiguration().getRequiredPropertyString("interactivespaces.example.comm.ros.hello.topic");
  }

  @Override
  public void onActivityStartup() {
    // No need to shutdown the subscriber, this will happen as part of node shutdown which shuts down all
    // publishers and subscribers on the node.
    subscriber = rosActivityComponent.getNode().newSubscriber(topicName, ColorRGBA._TYPE);
    subscriber.addMessageListener(new MessageListener<ColorRGBA>() {
      @Override
      public void onNewMessage(ColorRGBA message) {
        onNewRosMessage(message);
      }
    });
  }

  /**
   * A new ROS message has come in.
   *
   * @param message
   *          the new message
   */
  private void onNewRosMessage(ColorRGBA message) {
    getLog().info(
        String.format("Received ROS message {r: %f, g: %f, b: %f, a: %f} on topic %s", message.getR(), message.getG(),
            message.getB(), message.getA(), topicName));
  }
}
