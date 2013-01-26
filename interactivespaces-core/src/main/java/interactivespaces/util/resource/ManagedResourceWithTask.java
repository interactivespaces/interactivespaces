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

package interactivespaces.util.resource;

import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.concurrent.Future;

/**
 * A {@link ManagedResource} paired with a {@link Runnable} task.
 * 
 * The resource is started first then the task. When shutting down, the task is
 * shut down first, then the resource.
 * 
 * @author Keith M. Hughes
 */
public class ManagedResourceWithTask implements ManagedResource {

	/**
	 * The resource being managed.
	 */
	private ManagedResource resource;

	/**
	 * The task to be run.
	 */
	private Runnable task;

	/**
	 * The space environment being run under.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * The future for the task.
	 */
	private Future<?> future;

	public ManagedResourceWithTask(ManagedResource resource, Runnable task,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.resource = resource;
		this.task = task;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public void startup() {
		resource.startup();

		future = spaceEnvironment.getExecutorService().submit(task);
	}

	@Override
	public void shutdown() {
		if (future != null) {
			future.cancel(true);
			future = null;
		}

		resource.shutdown();
		resource = null;
	}
}
