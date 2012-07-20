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

package interactivespaces.master.server.ui;

import interactivespaces.domain.space.Space;

import java.util.Map;

/**
 * Manager for the ui control of spaces.
 *
 * @author Keith M. Hughes
 */
public interface UiSpaceManager {

	/**
	 * Message key for non-existent spaces.
	 */
	public static final String MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN = "space.domain.space.unknown";

	/**
	 * Delete a space from the space repository.
	 * 
	 * <p>
	 * Does nothing if there is no space with the given ID.
	 * 
	 * @param id
	 * 		ID of the space.
	 */
	void deleteSpace(String id);
	
	/**
	 * Deploy everything needed a space.
	 * 
	 * For details, see {@link interactivespaces.master.server.services.ActiveSpaceManager#deploySpace(Space)}.
	 * 
	 * @param id
	 * 		ID of the space node.
	 */
	void deploySpace(String id);
	
	/**
	 * Configure all components in a space.
	 * 
	 * For details, see {@link interactivespaces.master.server.services.ActiveSpaceManager#configureSpace(Space)}.
	 * 
	 * @param id
	 * 		ID of the space node.
	 */
	void configureSpace(String id);
	
	/**
	 * Get the basic JSON view of a space.
	 * 
	 * @param space
	 * 			the space
	 * 
	 * @return the JSON view data for the space
	 */
	Map<String, Object> getBasicSpaceViewJsonData(Space space);

	/**
	 * Add in the basic space data used in API calls.
	 * 
	 * @param space
	 *            the space to get the data from
	 * @param data
	 *            the JSON data being collected
	 */
	void getBasicSpaceData(Space space, Map<String, Object> data);

	/**
	 * Get the JSON view data for a specific space.
	 * 
	 * @param space
	 *            the space to get the data from
	 * 
	 * @return the map of data for that space
	 */
	Map<String, Object> getSpaceViewJsonData(Space space);

	/**
	 * Modify a space's metadata.
	 * 
	 * <p>
	 * The command map contains a field called command. This field will be one
	 * of
	 * 
	 * <ul>
	 * <li>replace - data contains a map, replace the entire metadata map with
	 * the map</li>
	 * <li>modify - data contains a map, replace just the fields found in the
	 * map with the values found in the map</li>
	 * <li>delete - data contains a list of keys, remove all keys found in data</li>
	 * </ul>
	 * 
	 * @param id
	 *            ID of the activity
	 * @param metadataCommand
	 *            the modification command
	 * 
	 * @return a JSON response object
	 */
	Map<String, Object> updateSpaceMetadata(String id,
			Map<String, Object> metadataCommand);
	
	/**
	 * Start the given space.
	 * 
	 * For details, see {@link interactivespaces.master.server.services.ActiveSpaceManager#startupSpace(Space)}.
	 * 
	 * @param id
	 * 		ID of the space node.
	 */
	void startupSpace(String id);
	
	/**
	 * Shut a given space down. Child spaces will be shut down first.
	 * 
	 * For details, see {@link interactivespaces.master.server.services.ActiveSpaceManager#shutdownSpace(Space)}.
	 * 
	 * @param id
	 * 		ID of the space node.
	 */
	void shutdownSpace(String id);
	
	/**
	 * Activate a space.
	 * 
	 * For details, see {@link interactivespaces.master.server.services.ActiveSpaceManager#activateSpace(Space)}.
	 * 
	 * @param id
	 * 		ID of the space node.
	 */
	void activateSpace(String id);
	
	/**
	 * Deactivate a space.
	 * 
	 * For details, see {@link interactivespaces.master.server.services.ActiveSpaceManager#deactivateSpace(Space)}.
	 * 
	 * @param id
	 * 		ID of the space node.
	 */
	void deactivateSpace(String id);
	
	/**
	 * Get the JSON status for a space.
	 * 
	 * @param id
	 * 		ID of the space
	 * 
	 * @return a JSON response object
	 */
	Map<String, Object> getJsonSpaceStatus(String id);
}
