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

package interactivespaces.domain.system;

import interactivespaces.domain.PersistedObject;

import java.io.Serializable;

/**
 * A named script which can be scheduled.
 * 
 * @author Keith M. Hughes
 */
public interface NamedScript extends PersistedObject, Serializable {

	/**
	 * Get the descriptive name for the script.
	 * 
	 * @return The descriptive name
	 */
	String getName();

	/**
	 * Set the descriptive name for the script.
	 * 
	 * @param name
	 *            The descriptive name
	 */
	void setName(String name);

	/**
	 * Get the description of the script.
	 * 
	 * @return the description. Can be null.
	 */
	String getDescription();

	/**
	 * Set the description of the script.
	 * 
	 * @param description
	 *            the description. Can be null.
	 */
	void setDescription(String description);

	/**
	 * Get the content of the script.
	 * 
	 * @return the content. Can be null.
	 */
	String getContent();

	/**
	 * Set the content of the script.
	 * 
	 * @param content
	 *            the content. Can be null.
	 */
	void setContent(String content);

	/**
	 * Get the language of the script.
	 * 
	 * @return the language
	 */
	String getLanguage();

	/**
	 * Set the language of the script.
	 * 
	 * @param language
	 *            the language
	 */
	void setLanguage(String language);

	/**
	 * Get the schedule of the script.
	 * 
	 * @return the schedule. Can be null.
	 */
	String getSchedule();

	/**
	 * Set the schedule of the script.
	 * 
	 * @param schedule
	 *            the schedule. Can be null.
	 */
	void setSchedule(String schedule);

	/**
	 * Is the script currently in the scheduler?
	 * 
	 * @return {@code true} if the script is scheduled
	 */
	boolean getScheduled();

	/**
	 * Set whether or not the script should be scheduled with the scheduling
	 * service.
	 * 
	 * <p>
	 * If the script is scheduled, setting this to {@code false} will remove it
	 * from the scheduler.
	 * 
	 * @param scheduled
	 *            {@code true} if the script should be scheduled
	 */
	void setScheduled(boolean scheduled);
}
