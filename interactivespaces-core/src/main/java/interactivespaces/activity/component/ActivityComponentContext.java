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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A context for {@link ActivityComponent} instances to run in.
 * 
 * @author Keith M. Hughes
 */
public class ActivityComponentContext {

	/**
	 * This lock is for components to check whether they should be considered
	 * running.
	 */
	private ReadWriteLock runningLock = new ReentrantReadWriteLock(true);

	/**
	 * {@code true} if the components should be considered running.
	 */
	private boolean running = false;

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
	 * Get a read lock on the running status.
	 * 
	 * <p>
	 * There can be multiple reads simultaneously.
	 * 
	 * <p>
	 * This must be paired with an {@link #unlockReadRunningRead()} call,
	 * preferably in a finally block.
	 * 
	 * @return {@code true} if the components should be considered running.
	 */
	public boolean lockReadRunningRead() {
		runningLock.readLock().lock();

		return running;
	}

	/**
	 * Unlock the read lock on the running status.
	 */
	public void unlockReadRunningRead() {
		runningLock.readLock().unlock();
	}

	/**
	 * Do a write lock on the running status.
	 * 
	 * <p>
	 * There can be only 1 write.
	 * 
	 * <p>
	 * This must be paired with an {@link #unlockRunningSet()} call, preferably
	 * in a finally block.
	 */
	public void lockRunningSet() {
		runningLock.writeLock().lock();
	}

	/**
	 * Unlock the write lock on the running status and set running.
	 * 
	 * @param running
	 * 		the value to set running to
	 */
	public void unlockRunningSet(boolean running) {
		this.running = running;
		runningLock.writeLock().unlock();
	}

	/**
	 * Should the components consider themselves running?
	 * 
	 * @return {@code true} if the components should be considered running
	 */
	public void clearRunning() {
		Lock writeLock = runningLock.writeLock();
		writeLock.lock();
		this.running = false;
		writeLock.unlock();
	}
}
