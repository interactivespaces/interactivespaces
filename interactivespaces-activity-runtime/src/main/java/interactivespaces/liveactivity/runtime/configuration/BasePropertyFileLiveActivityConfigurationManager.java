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

package interactivespaces.liveactivity.runtime.configuration;

import interactivespaces.configuration.SimplePropertyFileSingleConfigurationStorageManager;
import interactivespaces.configuration.SingleConfigurationStorageManager;
import interactivespaces.evaluation.ExpressionEvaluator;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.io.File;

/**
 * A configuration manager which uses Java property files.
 *
 * @author Keith M. Hughes
 */
public abstract class BasePropertyFileLiveActivityConfigurationManager implements LiveActivityConfigurationManager {

  /**
   * File extension configuration files should have.
   */
  public static final String CONFIGURATION_FILE_EXTENSION = "conf";

  /**
   * The Interactive Spaces environment.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Factory for expression evaluators.
   */
  private final ExpressionEvaluatorFactory expressionEvaluatorFactory;

  /**
   * Construct a new configuration manager.
   *
   * @param expressionEvaluatorFactory
   *          the expression evaluator factory to use
   * @param spaceEnvironment
   *          the space environment to use
   */
  public BasePropertyFileLiveActivityConfigurationManager(ExpressionEvaluatorFactory expressionEvaluatorFactory,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.expressionEvaluatorFactory = expressionEvaluatorFactory;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public StandardLiveActivityConfiguration newLiveActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem) {
    ExpressionEvaluator expressionEvaluator = expressionEvaluatorFactory.newEvaluator();

    File baseActivityConfiguration = getBaseActivityConfiguration(liveActivity, activityFilesystem);
    SingleConfigurationStorageManager baseConfigurationStorageManager =
        newConfiguration(baseActivityConfiguration, true, expressionEvaluator);

    File installedActivityConfiguration = getInstalledActivityConfiguration(liveActivity, activityFilesystem);
    boolean configurationFileRequired = isInstalledActivityConfigurationFileRequired();
    SingleConfigurationStorageManager installedActivityConfigurationStorageManager =
        newConfiguration(installedActivityConfiguration, configurationFileRequired, expressionEvaluator);

    Log log = spaceEnvironment.getLog();
    boolean fileExists = installedActivityConfiguration.exists();
    String absolutePath = installedActivityConfiguration.getAbsolutePath();
    if (fileExists) {
      log.info("Using installed activity configuration file " + absolutePath);
    } else if (configurationFileRequired) {
      log.error("Missing required installed activity configuration file " + absolutePath);
    } else {
      log.warn("Skipping missing installed activity configuration file " + absolutePath);
    }

    StandardLiveActivityConfiguration configuration =
        new StandardLiveActivityConfiguration(baseConfigurationStorageManager,
            installedActivityConfigurationStorageManager, expressionEvaluator,
            spaceEnvironment.getSystemConfiguration());
    expressionEvaluator.setEvaluationEnvironment(configuration);

    return configuration;
  }

  /**
   * Indicate if an activity configuration is required for this instance. Intended to be overridden
   * by sub-classes to provide specific behavior for different running contexts.
   *
   * @return {@code true} if the activity configuration file is required
   */
  protected boolean isInstalledActivityConfigurationFileRequired() {
    return false;
  }

  /**
   * Get the activity's base configuration.
   *
   * @param liveActivity
   *          the live activity
   * @param activityFilesystem
   *          the activity file system
   *
   * @return the file for the activity's base configuration
   */
  protected abstract File getBaseActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem);

  /**
   * Get the activity's install configuration.
   *
   * @param liveActivity
   *          the live activity
   * @param activityFilesystem
   *          the activity file system
   *
   * @return the file for the activity's install configuration
   */
  protected abstract File getInstalledActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem);

  /**
   * Create a new configuration storage manager.
   *
   * @param configurationFile
   *          where the configuration file resides
   * @param required
   *          {@code true} if the configuration is required to run
   * @param expressionEvaluator
   *          expression evaluator to be given to the configuration
   *
   * @return a configuration storage manager
   */
  private SingleConfigurationStorageManager newConfiguration(File configurationFile, boolean required,
      ExpressionEvaluator expressionEvaluator) {
    return new SimplePropertyFileSingleConfigurationStorageManager(required, configurationFile, expressionEvaluator);
  }

  /**
   * Get the configuration file name from the configuration type.
   *
   * @param configType
   *          the type of configuration
   *
   * @return the configuration file name
   */
  protected String getConfigFileName(String configType) {
    return configType + "." + CONFIGURATION_FILE_EXTENSION;
  }
}
