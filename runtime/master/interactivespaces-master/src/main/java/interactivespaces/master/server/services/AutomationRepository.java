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

import interactivespaces.domain.system.NamedScript;
import interactivespaces.expression.FilterExpression;

import java.util.List;

/**
 * A repository for script objects.
 *
 * @author Keith M. Hughes
 */
public interface AutomationRepository {

  /**
   * Create a new script.
   *
   * <p>
   * The script will be assigned a UUID.
   *
   * @return The new script instance. It will not be saved in the repository.
   */
  NamedScript newNamedScript();

  /**
   * Create a new script from a template.
   *
   * <p>
   * The script will be assigned a UUID.
   *
   * @param template
   *          the template script whose values will be copied in
   *
   * @return the new script instance, it will not be saved in the repository
   */
  NamedScript newNamedScript(NamedScript template);

  /**
   * Get all scripts in the repository.
   *
   * @return all scripts in the repository
   */
  List<NamedScript> getAllNamedScripts();

  /**
   * Get all scripts in the repository that pass a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return all scripts in the repository matching the filter
   */
  List<NamedScript> getNamedScripts(FilterExpression filter);

  /**
   * Get a script by its ID.
   *
   * @param id
   *          The id of the desired script.
   *
   * @return The script with the given id or {@code null} if no such script.
   */
  NamedScript getNamedScriptById(String id);

  /**
   * Save a script in the repository.
   *
   * <p>
   * Is used both to save a new script into the repository for the first time or to update edits to the script.
   *
   * @param script
   *          The script to save.
   *
   * @return The persisted script. use this one going forward.
   */
  NamedScript saveNamedScript(NamedScript script);

  /**
   * Delete a script in the repository.
   *
   * @param script
   *          The script to delete.
   *
   * @return
   */
  void deleteNamedScript(NamedScript script);
}
