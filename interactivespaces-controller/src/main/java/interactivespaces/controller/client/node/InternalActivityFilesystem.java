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

package interactivespaces.controller.client.node;

import interactivespaces.activity.ActivityFilesystem;

import java.io.File;

/**
 * An {@link ActivityFilesystem} with some extras needed by the controller.
 *
 * @author Keith M. Hughes
 */
public interface InternalActivityFilesystem extends ActivityFilesystem {

  /**
   * Get the directory which contains internal Interactive Spaces data for the
   * activity.
   *
   * @return the directory which contains Interactive Spaces data for the
   *         activity
   */
  File getInternalDirectory();

  /**
   * Get an Interactive Spaces data file for the activity in the internal
   * Interactive Spaces folder.
   *
   * @param relative
   *          relative path for the file
   *
   * @return the requested file
   */
  File getInternalFile(String relative);
}
