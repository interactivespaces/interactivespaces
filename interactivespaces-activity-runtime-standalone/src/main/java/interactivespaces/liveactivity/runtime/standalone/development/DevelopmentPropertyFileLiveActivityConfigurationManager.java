/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone.development;

import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.configuration.BasePropertyFileLiveActivityConfigurationManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.File;

/**
 * The live activity configuration manager for a development live activity runtime.
 *
 * @author Keith M. Hughes
 */
public class DevelopmentPropertyFileLiveActivityConfigurationManager extends
    BasePropertyFileLiveActivityConfigurationManager {

  /**
   * The activity config type for a standalone activity.
   */
  public static final String CONFIG_TYPE_STANDALONE_ACTIVITY = "activity";

  /**
   * Instance suffix used by this development instance.
   */
  private final String instanceSuffix;

  /**
   * Construct a new configuration manager.
   *
   * @param expressionEvaluatorFactory
   *          the expression evaluator factory to use
   * @param spaceEnvironment
   *          the space environment to use
   * @param instanceSuffix
   *          instance suffix to use for the configuration, or {@code null} for default
   */
  public DevelopmentPropertyFileLiveActivityConfigurationManager(ExpressionEvaluatorFactory expressionEvaluatorFactory,
      InteractiveSpacesEnvironment spaceEnvironment, String instanceSuffix) {
    super(expressionEvaluatorFactory, spaceEnvironment);
    this.instanceSuffix = instanceSuffix == null ? "" : instanceSuffix;
  }

  @Override
  protected File getBaseActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem) {
    return activityFilesystem.getInstallFile(getConfigFileName(CONFIG_TYPE_BASE_ACTIVITY));
  }

  @Override
  protected File getInstalledActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem) {
    return activityFilesystem.getInternalFile(getConfigFileName(CONFIG_TYPE_STANDALONE_ACTIVITY + instanceSuffix));
  }

  @Override
  protected boolean isInstalledActivityConfigurationFileRequired() {
    // If there is an instance suffix then the configuration file is required.
    return !instanceSuffix.isEmpty();
  }
}
