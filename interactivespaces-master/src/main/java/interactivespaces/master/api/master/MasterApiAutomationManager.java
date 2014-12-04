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

package interactivespaces.master.api.master;

import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;

import java.util.Map;
import java.util.Set;

/**
 * A Master API manager for control of scripts and other master automation.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiAutomationManager {

  /**
   * The schedule type for a script which have a repeat schedule.
   */
  String SCHEDULE_TYPE_REPEAT = "repeat:";

  /**
   * The schedule type for a script which will run only once at a given time.
   */
  String SCHEDULE_TYPE_ONCE = "once:";

  /**
   * Message key for non-existent named scripts.
   */
  String MESSAGE_SPACE_DOMAIN_NAMEDSCRIPT_UNKNOWN = "space.domain.namedscript.unknown";

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
   *          the script
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
   *          ID of the script in the db
   * @param template
   *          the script
   *
   * @return the script domain object stored in the db
   */
  NamedScript updateNamedScript(String id, SimpleNamedScript template);

  /**
   * Delete a script from the script repository.
   *
   * @param id
   *          ID of the script
   *
   * @return the API response
   */
  Map<String, Object> deleteNamedScript(String id);

  /**
   * Run the specified script.
   *
   * @param id
   *          ID of the script
   *
   * @return the API response
   */
  Map<String, Object> runNamedScript(String id);

  /**
   * Get all named scripts that meet a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return the master API message for all scripts that meet the filter
   */
  Map<String, Object> getNamedScriptsByFilter(String filter);

  /**
   * Get the view of a named script.
   *
   * @param id
   *          ID for the named script
   *
   * @return the master API message for the named script view
   */
  Map<String, Object> getNamedScriptView(String id);

  /**
   * Modify a named script's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the map</li>
   * <li>modify - data contains a map, replace just the fields found in the map with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in data</li>
   * </ul>
   *
   * @param id
   *          ID of the named script
   * @param metadataCommandObj
   *          the modification command
   *
   * @return the master API response
   */
  Map<String, Object> updateNamedScriptMetadata(String id, Object metadataCommandObj);
}
