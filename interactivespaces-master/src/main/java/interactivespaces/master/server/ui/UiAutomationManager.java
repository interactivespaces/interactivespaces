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

import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;

import java.util.Set;


/**
 * A manager for ui control of scripts.
 *
 * @author Keith M. Hughes
 */
public interface UiAutomationManager {

	/**
	 * Get a set of all scripting languages which can be used.
	 * 
	 * @return all available scripting languages
	 */
	Set<String> getScriptingLanguages();

	/**
	 * Save a named script in the script repository.
	 * 
	 * <p>
	 * The script will be started if it is scheduled.
	 * 
	 * @param script
	 *            the script
	 * 
	 * @return the script domain object stored in the db
	 */
	NamedScript saveNamedScript(SimpleNamedScript script);

	/**
	 * Save a named script in the script repository.
	 * 
	 * <p>
	 * The script will be started if it is scheduled.
	 * 
	 * @param id
	 * 			ID of the script in the db
	 * @param template
	 *            the script
	 * 
	 * @return the script domain object stored in the db
	 */
	NamedScript updateNamedScript(String id, SimpleNamedScript template);

	/**
	 * Delete a script from the script repository.
	 * 
	 * @param id
	 *            ID of the script
	 * 
	 * @return the script manager which performed the operation.
	 */
	UiAutomationManager deleteNamedScript(String id);

	/**
	 * Run the specified script.
	 * 
	 * @param id
	 *            ID of the script
	 * 
	 * @return the script manager which performed the operation.
	 */
	UiAutomationManager runScript(String id);
}
