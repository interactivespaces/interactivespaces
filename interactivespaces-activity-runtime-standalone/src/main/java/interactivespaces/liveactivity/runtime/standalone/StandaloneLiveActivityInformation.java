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

package interactivespaces.liveactivity.runtime.standalone;

import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;

import java.io.File;

/**
 * Information about an activity in the filesystem.
 *
 * @author Keith M. Hughes
 */
public class StandaloneLiveActivityInformation {

  /**
   * UUID for the activity.
   */
  private String uuid;

  /**
   * Base file where the activity source is found.
   */
  private File baseSourceDirectory;

  /**
   * The activity runtime directory that will be created by the standalone run.
   */
  private File baseRuntimeActivityDirectory;

  /**
   * The activity runtime file system.
   */
  private InternalLiveActivityFilesystem activityFilesystem;

  /**
   * Construct an information object.
   *
   * @param uuid
   *          UUID for the activity
   * @param baseSourceDirectory
   *          base directory for the source
   */
  public StandaloneLiveActivityInformation(String uuid, File baseSourceDirectory) {
    this.uuid = uuid;
    this.baseSourceDirectory = baseSourceDirectory;
  }

  /**
   * Get the UUID for the activity.
   *
   * @return the UUID for the activity
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Get the base source directory.
   *
   * @return the base source directory
   */
  public File getBaseSourceDirectory() {
    return baseSourceDirectory;
  }

  /**
   * Get the base directory that will be used by the activity runtime for this activity.
   *
   * @return the base directory that will be used by the activity runtime for this activity
   */
  public File getBaseRuntimeActivityDirectory() {
    return baseRuntimeActivityDirectory;
  }

  /**
   * Set the base directory that will be used by the activity runtime for this activity.
   *
   * @param baseRuntimeActivityDirectory
   *          the base directory that will be used by the activity runtime for this activity
   */
  public void setBaseRuntimeActivityDirectory(File baseRuntimeActivityDirectory) {
    this.baseRuntimeActivityDirectory = baseRuntimeActivityDirectory;
  }

  /**
   * Get the activity filesystem.
   *
   * @return the activity filesystem
   */
  public InternalLiveActivityFilesystem getActivityFilesystem() {
    return activityFilesystem;
  }

  /**
   * Set the activity filesystem.
   *
   * @param activityFilesystem
   *          the activity filesystem
   */
  public void setActivityFilesystem(InternalLiveActivityFilesystem activityFilesystem) {
    this.activityFilesystem = activityFilesystem;
  }
}
