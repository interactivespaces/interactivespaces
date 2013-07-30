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

package interactivespaces.service.script.internal;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.ScriptSource;

/**
 * A factory for scripted {@link Activity} implementations.
 *
 * @author Keith M. Hughes
 */
public interface ActivityScriptFactory {

  /**
   * Initialize the stub
   */
  void initialize();

  /**
   * Get an {@link Activity} from the given filepath.
   *
   * @param objectName
   *          the name of the object to create
   * @param scriptSource
   *          the source of the script to execute
   * @param activityFilesystem
   *          the filesystem for the activity
   * @param configuration
   *          the configuration for the activity
   *
   * @return the activity
   */
  ActivityScriptWrapper getActivity(String objectName, ScriptSource scriptSource,
      ActivityFilesystem activityFilesystem, Configuration configuration);
}