/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.activity.impl.ros;

import interactivespaces.activity.ActivityControlMessageListener;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.ros.RosSubscribers;

import org.ros.message.MessageListener;

/**
 * A listener for ROS control messages.
 * 
 * TODO(keith): Make this into an activity component.
 * 
 * @author Keith M. Hughes
 */
public class RosControlMessageListener {

	/**
	 * Configuration property giving topic name the app will use for getting tag
	 * swipes.
	 */
	public static final String CONFIGURATION_ROS_TOPIC_NAMES_CONTROL = "space.activity.ros.topic.names.control";

	/**
	 * ROS subscribers for the control messages.
	 */
	private RosSubscribers<?> rosSubscribers;

	public <T> RosControlMessageListener(BaseRosActivity activity,
			Configuration configuration, String messageTopicType,
			final ActivityControlMessageListener<T> messageListener) {
		rosSubscribers = new RosSubscribers<T>(activity.getLog());

		RosSubscribers<T> rosSubscribers = new RosSubscribers<T>(
				activity.getLog());
		rosSubscribers
				.addSubscribers(
						activity.getMainNode(),
						messageTopicType,
						configuration
								.getRequiredPropertyString(CONFIGURATION_ROS_TOPIC_NAMES_CONTROL),
						new MessageListener<T>() {
							@Override
							public void onNewMessage(T command) {
								messageListener.onNewControlMessage(command);
							}
						});

		this.rosSubscribers = rosSubscribers;
	}

	/**
	 * Shut this component down.
	 */
	public void shutdownComponent() {
		rosSubscribers.shutdown();
	}
}
