/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.controller.runtime.configuration;

import interactivespaces.configuration.SimplePropertyFileSingleConfigurationStorageManager;
import interactivespaces.configuration.SingleConfigurationStorageManager;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InternalInteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;
import java.util.Map;

/**
 * A standard implementation of a {@link SpaceControllerConfigurationManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceControllerConfigurationManager implements SpaceControllerConfigurationManager {

  /**
   * Folder for the space controller configuration file.
   */
  public static final String CONFIGURATION_FILE_FOLDER = "controller/config";

  /**
   * The file name for the space controller configuration file.
   */
  public static final String CONFIGURATION_FILE_NAME = "spacecontroller.conf";

  /**
   * The controller's space environment.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The configuration storage manager.
   */
  private SingleConfigurationStorageManager configurationStorageManager;

  /**
   * The file support for the manager.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new manager.
   *
   * @param spaceEnvironment
   *          the space environment to be modified
   */
  public StandardSpaceControllerConfigurationManager(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    File configurationFileFolder = new File(CONFIGURATION_FILE_FOLDER);
    fileSupport.directoryExists(configurationFileFolder);
    File configurationFile = new File(configurationFileFolder, CONFIGURATION_FILE_NAME);

    configurationStorageManager =
        new SimplePropertyFileSingleConfigurationStorageManager(false, configurationFile, spaceEnvironment
            .getSystemConfiguration().getExpressionEvaluator());

    ((InternalInteractiveSpacesEnvironment) spaceEnvironment).changeSystemConfigurationTop(configurationStorageManager
        .getConfiguration());

    load();
  }

  @Override
  public void shutdown() {
    // Nothing to do right now.
  }

  @Override
  public void load() {
    configurationStorageManager.load();
  }

  @Override
  public void update(Map<String, String> update) {
    configurationStorageManager.clear().update(update).save();
  }
}
