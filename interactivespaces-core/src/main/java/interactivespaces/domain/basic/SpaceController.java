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

import interactivespaces.domain.PersistedObject;

import java.io.Serializable;
import java.util.Map;

/**
 * A controller node in the space.
 * 
 * @author Keith M. Hughes
 */
public interface SpaceController extends PersistedObject, Serializable {
	/**
	 * @return the hostId
	 */
	String getHostId();

	/**
	 * @param hostId
	 *            the hostId to set
	 */
	void setHostId(String hostId);

	/**
	 * @return the uuid
	 */
	String getUuid();

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	void setUuid(String uuid);

	/**
	 * @return the name
	 */
	String getName();

	/**
	 * @param name
	 *            the name to set
	 */
	void setName(String name);

	/**
	 * @return the description
	 */
	String getDescription();

	/**
	 * @param description
	 *            the description to set
	 */
	void setDescription(String description);

	/**
	 * Set the metadata for the space controller.
	 * 
	 * <p>
	 * This removes the old metadata completely.
	 * 
	 * @param metadata
	 *            the metadata for the space controller (can be {@link null}
	 */
	void setMetadata(Map<String, Object> metadata);

	/**
	 * Get the metadata for the space controller.
	 * 
	 * @return the space controller's meta data
	 */
	Map<String, Object> getMetadata();
}
