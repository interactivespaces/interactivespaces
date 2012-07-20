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

package interactivespaces.activity.event.bus.node.json;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.event.Event;
import interactivespaces.event.EventListener;
import interactivespaces.event.EventListenerCollection;
import interactivespaces.event.EventPublisher;
import interactivespaces.event.EventSubscriber;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * An Interativespaces Routable Activity which sits as a hub on a given
 * controller and makes itself available to transmit events network-wide.
 * 
 * @author Keith M. Hughes
 * @since Apr 23, 2012
 */
public class InteractivespacesEventBusNodeJsonActivity extends BaseRoutableRosActivity {

	/**
	 * The name in the space environment for the event subscriber.
	 */
	private static final String SPACE_ENVIRONMENT_VALUE_NAME_SUBSCRIBER = "interactivespaces.event.bus.subscriber";

	/**
	 * The name in the space environment for the event publisher.
	 */
	private static final String SPACE_ENVIRONMENT_VALUE_NAME_PUBLISHER = "interactivespaces.event.bus.publisher";

	/**
	 * The subscriber that event listeners will attach through.
	 */
	private EventListenerCollection eventListeners = new EventListenerCollection();

	/**
	 * The event subscriber for this activity.
	 */
	private EventSubscriber eventSubscriber;

	/**
	 * The event publisher for this activity.
	 */
	private EventPublisher eventPublisher;

	@Override
	public void onActivityStartup() {
		// Hide the rest of the activity class form the published
		// event subscriber.
		eventSubscriber = new EventSubscriber() {
			@Override
			public void removeEventListener(String type, EventListener listener) {
				eventListeners.removeEventListener(type, listener);
			}

			@Override
			public void addEventListener(String type, EventListener listener) {
				eventListeners.addEventListener(type, listener);
			}
		};

		eventPublisher = new EventPublisher() {

			@Override
			public void publishEvent(Event event) {
				createAndPublishEvent(event.getType(), event.getSource(),
						event.getData());
			}

			@Override
			public void createAndPublishEvent(String type, String source,
					Object data) {
				publishSplitEvent(type, source, data);
			}
		};

		getSpaceEnvironment().setValue(SPACE_ENVIRONMENT_VALUE_NAME_SUBSCRIBER,
				eventSubscriber);
		getSpaceEnvironment().setValue(SPACE_ENVIRONMENT_VALUE_NAME_PUBLISHER,
				eventPublisher);

		getLog().info("Event Hub Bus Node started up");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Event Hub Bus Node activated");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Event Hub Bus Node deactivated");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Event Hub Bus Node shut down");
	}

	@Override
	public void onNewInputJson(String channelName, Map<String, Object> data) {
		// For now, only listening on one channel.

		// Only transmit if we are activated.
		if (isActivated()) {
			Event event = new Event((String) data.get("type"),
					(String) data.get("source"), data.get("data"));
			eventListeners.broadcastEvent(event);
		}
	}

	/**
	 * Publish an event.
	 * 
	 * @param type
	 *            the type of the event
	 * @param source
	 *            the source of the event
	 * @param data
	 *            the data of the event (can be {@code null}
	 */
	private void publishSplitEvent(String type, String source, Object data) {
		if (isActivated()) {
			Map<String, Object> eventData = Maps.newHashMap();
			eventData.put("type", type);
			eventData.put("source", source);
			eventData.put("data", data);
			
			sendOutputJson("events_out", eventData);
		}
	}
}
