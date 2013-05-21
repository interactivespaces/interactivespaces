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

package interactivespaces.util.data.persist;

import java.util.Map;

/**
 * A service which supports the persistence of maps to disk.
 * 
 * <p>
 * This service is meant for low throughput usage. Readers are blocked when
 * writing, and writing is blocked if there are readers. Locking is fair, which
 * means a writer won't be blocked from writing if new readers keep coming in.
 * 
 * <p>
 * Map names should consist only of characters, digits, and underscores, and not
 * start with a digit.
 * 
 * @author Keith M. Hughes
 */
public interface SimpleMapPersister {

	/**
	 * Get the requested map.
	 * 
	 * <p>
	 * This will block if there is an active write taking place.
	 * 
	 * @param name
	 *            the name of the map to get
	 * 
	 * @return the map or {@code null} if there is no map with the given name
	 */
	Map<String, Object> getMap(String name);

	/**
	 * Set the map for the given name to be the map supplied.
	 * 
	 * <p>
	 * The new map will overwrite the old map.
	 * 
	 * <p>
	 * This will block if there is an active read taking place.
	 * 
	 * @param name
	 *            the name of the map
	 * @param map
	 *            the map to be saved
	 */
	void putMap(String name, Map<String, Object> map);

	/**
	 * Remove the requested map.
	 * 
	 * <p>
	 * This will block if there is an active read taking place.
	 * 
	 * @param name
	 *            the name of the map to remove
	 * 
	 * @return {@code true} if the map is removed, {@code false} if it didn't
	 *         exist
	 */
	boolean removeMap(String name);
}
