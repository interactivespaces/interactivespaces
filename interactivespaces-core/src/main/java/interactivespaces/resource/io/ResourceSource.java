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

package interactivespaces.resource.io;

import java.io.File;

/**
 * A source for a resource.
 *
 * @author Keith M. Hughes
 */
public interface ResourceSource {

  /**
   * Copy the resource to the given file.
   *
   * @param destination
   *          the destination location
   */
  void copyTo(File destination);

  /**
   * Get the location of the resource.
   *
   * <p>
   * The location only has meaning dependent on the implementing class and is used primarily for logging.
   *
   * @return the location of the resource
   */
  String getLocation();
}
