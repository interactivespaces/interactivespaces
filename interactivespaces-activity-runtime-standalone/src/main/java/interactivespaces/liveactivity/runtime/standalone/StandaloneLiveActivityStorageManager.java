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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.LiveActivityStorageManager;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;
import java.util.List;

/**
 * A storage manager that scans a directory tree to find all possible Interactive spaces project folders in the tree.
 *
 * @author Keith M. Hughes
 */
public class StandaloneLiveActivityStorageManager implements LiveActivityStorageManager {

  /**
   * The root project folder for the storage manager.
   */
  private StandaloneLiveActivityInformationCollection liveActivityInformation;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new manager.
   *
   * @param liveActivityInformation
   *          the collection of live activity information
   */
  public StandaloneLiveActivityStorageManager(StandaloneLiveActivityInformationCollection liveActivityInformation) {
    this.liveActivityInformation = liveActivityInformation;
  }

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public List<String> getAllInstalledActivityUuids() {
    return liveActivityInformation.getAllActivityUuids();
  }

  @Override
  public File getBaseActivityLocation(String uuid) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot get base live activity location with UUID %s",
        uuid);

    // This is so the method compiles.
    return null;
  }

  @Override
  public InternalLiveActivityFilesystem getActivityFilesystem(String uuid) {
    return liveActivityInformation.getLiveActivityInformation(uuid).getActivityFilesystem();
  }

  @Override
  public void removeActivityLocation(String uuid) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot delete activities");
  }

  @Override
  public void cleanTmpActivityDataDirectory(String uuid) {
    fileSupport.deleteDirectoryContents(getActivityFilesystem(uuid).getTempDataDirectory());
  }

  @Override
  public void cleanPermanentActivityDataDirectory(String uuid) {
    fileSupport.deleteDirectoryContents(getActivityFilesystem(uuid).getPermanentDataDirectory());
  }
}
