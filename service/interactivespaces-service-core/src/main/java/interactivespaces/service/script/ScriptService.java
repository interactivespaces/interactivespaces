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

package interactivespaces.service.script;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.SupportedService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Execute scripts in Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public interface ScriptService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "scripting";

  /**
   * A set of empty bindings.
   */
  public static final Map<String, Object> EMPTY_BINDINGS = Collections
      .unmodifiableMap(new HashMap<String, Object>());

  /**
   * Get a set of all languages supported by the script engine.
   *
   * @return
   */
  Set<String> getLanguageNames();

  /**
   * Execute a script in the scripting engine.
   *
   * @param languageName
   *          the name of the language the script is in
   * @param script
   *          the source of the script
   */
  void executeSimpleScript(String languageName, String script);

  /**
   * Execute a script in the scripting engine.
   *
   * @param languageName
   *          the name of the language the script is in
   * @param script
   *          the source of the script
   * @param bindings
   *          any bindings the script should make available
   */
  void executeScript(String languageName, String script, Map<String, Object> bindings);

  /**
   * Execute a script in the scripting engine.
   *
   * @param languageName
   *          the name of the language the script is in
   * @param scriptSource
   *          the source of the script
   * @param bindings
   *          any bindings the script should make available
   */
  void executeScriptByName(String languageName, ScriptSource scriptSource,
      Map<String, Object> bindings);

  /**
   * Execute a script in the scripting engine.
   *
   * @param extension
   *          file extension for the file that had the script
   * @param scriptSource
   *          the source of the script
   * @param bindings
   *          Any bindings the script should make available.
   */
  void executeScriptByExtension(String extension, ScriptSource scriptSource,
      Map<String, Object> bindings);

  /**
   * Execute a script in the scripting engine.
   *
   * @param languageName
   *          the name of the language the script is in
   * @param script
   *          the source of the script
   *
   * @return the script
   *
   * @throws InteractiveSpacesException
   *           if the script could not compile
   */
  Script newSimpleScript(String languageName, String script);

  /**
   * Execute a script in the scripting engine.
   *
   * @param languageName
   *          the name of the language the script is in
   * @param scriptSource
   *          the source of the script
   *
   * @return the script
   *
   * @throws InteractiveSpacesException
   *           if the script could not compile
   */
  Script newScriptByName(String languageName, ScriptSource scriptSource);

  /**
   * Execute a script in the scripting engine.
   *
   * @param extension
   *          file extension for the file that had the script
   * @param scriptSource
   *          the source of the script
   *
   * @return the script
   *
   * @throws InteractiveSpacesException
   *           if the script could not compile
   */
  Script newScriptByExtension(String extension, ScriptSource scriptSource);

  /**
   * Create an {@link Activity} from the scripting language by its name.
   *
   * @param languageName
   *          the name of the scripting language
   * @param objectName
   *          the name of the object being extracted from the script
   * @param scriptSource
   *          the source of the script
   * @param activityFilesystem
   *          file system for the activity
   * @param configuration
   *          configuration for the activity
   * @return
   */
  ActivityScriptWrapper
      getActivityByName(String languageName, String objectName, ScriptSource scriptSource,
          ActivityFilesystem activityFilesystem, Configuration configuration);

  /**
   * Create an {@link Activity} from the scripting language by its file
   * extension.
   *
   * @param extension
   *          the extension of the scripting file
   * @param objectName
   *          the name of the object being extracted from the script
   * @param scriptSource
   *          the source of the script
   * @param activityFilesystem
   *          file system for the activity
   * @param configuration
   *          configuration for the activity
   * @return
   */
  ActivityScriptWrapper
      getActivityByExtension(String extension, String objectName, ScriptSource scriptSource,
          ActivityFilesystem activityFilesystem, Configuration configuration);
}
