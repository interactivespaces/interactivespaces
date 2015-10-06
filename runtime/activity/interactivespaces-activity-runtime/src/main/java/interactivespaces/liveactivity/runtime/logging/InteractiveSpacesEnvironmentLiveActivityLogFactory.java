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

package interactivespaces.liveactivity.runtime.logging;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import org.apache.commons.logging.Log;

/**
 * A {@link LiveActivityLogFactory} which uses the {@link InteractiveSpacesEnvironment} to get a logger.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesEnvironmentLiveActivityLogFactory implements LiveActivityLogFactory {

  /**
   * Prefix to be affixed to the logger name.
   */
  private static final String ACTIVITY_LOG_PREFIX = "activity";

  /**
   * The name of the file for the per-activity log.
   */
  private static final String ACTIVITY_LOG_FILENAME = "activity.log";

  /**
   * The Interactive Spaces environment this is being run in.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The file support to be used by the factory.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create a new activity log factory for the given space environment.
   *
   * @param spaceEnvironment
   *          environment to define logging context
   */
  public InteractiveSpacesEnvironmentLiveActivityLogFactory(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public Log
      createLogger(InstalledLiveActivity installedActivity, String level, ActivityFilesystem activityFilesystem) {
    return spaceEnvironment.getLog(ACTIVITY_LOG_PREFIX + "." + installedActivity.getUuid(), level, fileSupport
        .newFile(activityFilesystem.getLogDirectory(), ACTIVITY_LOG_FILENAME).getAbsolutePath());
  }

  @Override
  public void releaseLog(Log activityLog) {
    spaceEnvironment.releaseLog(activityLog);
  }
}
