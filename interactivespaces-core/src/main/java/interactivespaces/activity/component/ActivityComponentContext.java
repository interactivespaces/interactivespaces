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

package interactivespaces.activity.component;

import interactivespaces.activity.Activity;
import interactivespaces.util.concurrency.AcceptingPriorityEventQueue;

/**
 * A context for {@link ActivityComponent} instances to run in.
 * 
 * @author Keith M. Hughes
 */
public class ActivityComponentContext {

	/**
	 * The activity the components are running for.
	 */
	private Activity activity;

	/**
	 * All components running in the current context.
	 */
	private ActivityComponentCollection components;

	/**
	 * Factory for new components.
	 */
	private ActivityComponentFactory componentFactory;

	/**
	 * The event queue for the activity.
	 */
	private AcceptingPriorityEventQueue eventQueue;

	/**
	 * @param activity
	 *            the activity which will use this context
	 * @param components
	 *            the component collection for this context
	 * @param componentFactory
	 *            the factory for any new components that will be needed
	 */
	public ActivityComponentContext(Activity activity,
			ActivityComponentCollection components,
			ActivityComponentFactory componentFactory) {
		this.activity = activity;
		this.components = components;
		this.componentFactory = componentFactory;

		eventQueue = new AcceptingPriorityEventQueue(
				activity.getSpaceEnvironment(), activity.getLog());
	}

	/**
	 * Get an activity component from the collection.
	 * 
	 * @param name
	 *            name of the component
	 * 
	 * @return the component with the given name.
	 */
	public <T extends ActivityComponent> T getActivityComponent(String name) {
		return components.getActivityComponent(name);
	}

	/**
	 * Get the activity which is running the components
	 * 
	 * @return the activity
	 */
	public Activity getActivity() {
		return activity;
	}

	/**
	 * Get the component factory for this context.
	 * 
	 * @return
	 */
	public ActivityComponentFactory getComponentFactory() {
		return componentFactory;
	}

	/**
	 * Set whether or not the event activity queue is accepting or not.
	 * 
	 * @param accepting
	 */
	public void setActivityEventQueueAccepting(boolean accepting) {
		eventQueue.setAccepting(accepting);
	}

	/**
	 * Start the event queue running.
	 */
	public void startupEventQueue() {
		eventQueue.startup();
	}

	/**
	 * Shut the event queue down. Also stops the event queue from accepting.
	 * 
	 * <p>
	 * This is safe to call even if the event queue never started,
	 */
	public void shutdownEventQueue() {
		eventQueue.stopAcceptingAndShutdown();
	}

	/**
	 * Is the activity queue still running?
	 * 
	 * @return {@code true} if running
	 */
	public boolean isActivityEventQueueRunning() {
		return eventQueue.isRunning();
	}

	/**
	 * Add in an event into the event queue with the default priority
	 * 
	 * @param event
	 *            the event to add
	 */
	public void addActivityEventQueueEvent(Runnable event) {
		eventQueue.addEvent(event);
	}

	/**
	 * Add in an event with a given priority.
	 * 
	 * @param event
	 *            the event to add
	 * @param priority
	 *            the priority for the event, lower values are done first
	 */
	public void addActivityEventQueueEvent(Runnable event, int priority) {
		eventQueue.addEvent(event, priority);
	}
}
