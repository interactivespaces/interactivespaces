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

package interactivespaces.domain.basic;

import java.io.Serializable;

/**
 * A dependency needed by an {@link Activity}.
 * 
 * @author Keith M. Hughes
 */
public interface ActivityDependency extends Serializable {

	/**
	 * Get the activity which has the dependency.
	 */
	Activity getActivity();

	/**
	 * Set the activity which has the dependency.
	 * 
	 * @param activity
	 *            the activity which has the dependency
	 */
	void setActivity(Activity activity);

	/**
	 * Get the name of the dependency.
	 * 
	 * @return The name of the dependency.
	 */
	String getName();

	/**
	 * Set the name of the dependency.
	 * 
	 * @param name
	 *            he name of the dependency
	 */
	void setName(String name);

	/**
	 * Get the minimum version necessary for the activity.
	 * 
	 * @return
	 */
	String getMinimumVersion();

	/**
	 * Set the minimum version necessary for the activity.
	 * 
	 * @param minimumVersion
	 * 			the minimum version 
	 */
	void setMinimumVersion(String minimumVersion);

	/**
	 * Get the maximum version necessary for the activity.
	 * 
	 * @return
	 */
	String getMaximumVersion();

	/**
	 * Set the maximum version necessary for the activity.
	 * 
	 * @param versionMaximum
	 * 			the maximum version 
	 */
	void setMaximumVersion(String versionMaximum);

	/**
	 * Is the dependency required?
	 * 
	 * @return {@code true} if the dependency is required
	 */
	boolean isRequired();

	/**
	 * Set if the dependency is required.
	 * 
	 * @param required
	 *            {@code true} if the dependency is required
	 */
	void setRequired(boolean required);
}
