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

package interactivespaces.activity.configuration;


/**
 * Useful configuration constants and methods for Interactive Spaces activities.
 *
 * @author Keith M. Hughes
 */
public class ActivityConfiguration {

  /**
   * Configuration property giving the location of the activity executable
   * relative to the activity installation directory.
   */
  public static final String CONFIGURATION_ACTIVITY_EXECUTABLE = "space.activity.executable";

  /**
   * Configuration property giving the flags that a native activity would use to
   * launch.
   */
  public static final String CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS =
      "space.activity.executable.flags";

  /**
   * Configuration property which will give the activity type.
   */
  public static final String CONFIGURATION_ACTIVITY_TYPE = "space.activity.type";

  /**
   * Configuration property which will give the activity's installation directory.
   */
  public static final String CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_INSTALL = "activity.installdir";

  /**
   * Configuration property which will give the activity's log directory.
   */
  public static final String CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_LOG = "activity.logdir";

  /**
   * Configuration property which will give the activity's permanent data directory.
   */
  public static final String CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_DATA = "activity.datadir";

  /**
   * Configuration property which will give the activity's temp data directory.
   */
  public static final String CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_TMP = "activity.tmpdir";
}
