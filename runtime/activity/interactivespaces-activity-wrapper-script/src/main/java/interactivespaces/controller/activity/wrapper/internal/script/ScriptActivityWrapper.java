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

package interactivespaces.controller.activity.wrapper.internal.script;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.activity.wrapper.BaseActivityWrapper;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.FileScriptSource;
import interactivespaces.service.script.ScriptService;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;

/**
 * An {@link ActivityWrapper} for scripted activities.
 *
 * TODO(keith): Eventually retire this class and use scripting class directly.
 *
 * @author Keith M. Hughes
 */
public class ScriptActivityWrapper extends BaseActivityWrapper {

  /**
   * The separator between a file name and it's extension.
   */
  private static final char EXTENSION_SEPARATOR = '.';

  /**
   * Wrapper around the script.
   */
  private ActivityScriptWrapper scriptWrapper;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new wrapper.
   *
   * @param scriptFile
   *          the script file for the wrapper
   * @param scriptService
   *          the scripting service
   * @param activityFilesystem
   *          the file system for the activity
   * @param configuration
   *          the configuration for the activity
   */
  public ScriptActivityWrapper(File scriptFile, ScriptService scriptService, ActivityFilesystem activityFilesystem,
      Configuration configuration) {
    if (!fileSupport.exists(scriptFile)) {
      SimpleInteractiveSpacesException.throwFormattedException("Script file %s does not exist",
          scriptFile.getAbsolutePath());
    }

    String filename = scriptFile.getName();
    int periodPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
    String extension = filename.substring(periodPos + 1);
    String baseName = filename.substring(0, periodPos);

    scriptWrapper =
        scriptService.getActivityByExtension(extension, baseName, new FileScriptSource(scriptFile),
            activityFilesystem, configuration);
  }

  @Override
  public Activity newInstance() {
    return scriptWrapper.newInstance();
  }

  @Override
  public ActivityExecutionContext newExecutionContext() {
    return scriptWrapper.newExecutionContext();
  }
}
