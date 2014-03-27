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
import interactivespaces.util.resource.ManagedResource;

import java.util.Map;
import java.util.Set;

/**
 * A manager for various types of extensions an automations, such as running
 * scripts, scheduling actions, etc.
 *
 * @author Keith M. Hughes
 */
public interface AutomationManager extends ManagedResource {

  /**
   * Get a set of all scripting languages which can be used.
   *
   * @return the set of scripting languages available
   */
  Set<String> getScriptingLanguages();

  /**
   * Get all bindings useful for automation.
   *
   * @return map of binding name to object
   */
  Map<String, Object> getAutomationBindings();

  /**
   * Run the specified script.
   *
   * @param script
   *          the script
   */
  void runScript(NamedScript script);
}
