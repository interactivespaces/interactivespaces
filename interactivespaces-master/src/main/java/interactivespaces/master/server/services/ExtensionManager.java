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

package interactivespaces.master.server.services;

import java.util.Map;

/**
 * A manager for extensions to the master.
 * 
 * <p>
 * Startup extension files are sorted by name before they are run.
 * 
 * @author Keith M. Hughes
 */
public interface ExtensionManager {

	/**
	 * Result key for an unknown extension name.
	 */
	public static final String RESULT_KEY_SPACE_MASTER_EXTENSION_UNKNOWN = "space.master.extension.unknown";

	/**
	 * Result key for extension exception.
	 */
	public static final String RESULT_KEY_SPACE_MASTER_EXTENSION_EXCEPTION = "space.master.extension.exception";

	/**
	 * Start the extension master up.
	 */
	void startup();

	/**
	 * Shut the extension master down.
	 */
	void shutdown();

	/**
	 * Does the manager have a given extension?
	 * 
	 * @param extensionName
	 *            the name of the extension
	 * 
	 * @return {@code true} if the manager contains the extension.
	 */
	boolean containsApiExtension(String extensionName);

	/**
	 * Evaluate an API extension.
	 * 
	 * <p>
	 * Will return an error map if the extension doesn't exist.
	 * 
	 * @param extensionName
	 *            the name of the extension
	 * @param args
	 *            the arguments for the extension
	 * 
	 * @return the value from running the extension
	 */
	Map<String, Object> evaluateApiExtension(String extensionName,
			Map<String, Object> args);
}
