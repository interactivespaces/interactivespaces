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
import interactivespaces.configuration.Configuration;

import java.util.List;

/**
 * A component which provides functionality to an {@link Activity}.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityComponent {

	/**
	 * Get the name of the component.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Get all dependencies for this component.
	 * 
	 * @return all dependencies
	 */
	List<String> getDependencies();

	/**
	 * Start up the component.
	 * 
	 * @param configuration
	 *            the configuration containing parameters for the web server
	 * @param componentContext
	 *            the context the component is running in
	 * 
	 * @return this component instance
	 */
	void configureComponent(Configuration configuration,
			ActivityComponentContext componentContext);

	/**
	 * Start up the component.
	 */
	void startupComponent();

	/**
	 * Shutdown the component.
	 */
	void shutdownComponent();

	/**
	 * Is the component running?
	 * 
	 * @return {@code true} if the component is running.
	 */
	boolean isComponentRunning();
	
	/**
	 * get the component context the component is running under.
	 * 
	 * @return the component context
	 */
	ActivityComponentContext getComponentContext();
}