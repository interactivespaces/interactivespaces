/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.util.data.resource;

import interactivespaces.InteractiveSpacesException;

import java.io.File;
import java.io.OutputStream;

/**
 * A resource which can be copied from one location to another.
 *
 * @author Keith M. Hughes
 */
public interface CopyableResource {

  /**
   * Move the resource to the given destination.
   *
   * @param destination
   *          where to write the resource
   *
   * @return {@code true} if there was data to move and the move was successful,
   *         {@code false} if there was no file to move
   *
   * @throws InteractiveSpacesException
   *           something bad happened during the move
   *
   *           TODO(keith): Fix Rockwell code and rename this to copyTo()
   */
  boolean moveTo(File destination);

  /**
   * Move the resource to the given destination.
   *
   * <p>
   * The output stream is not closed by the call, that is the responsibility of
   * the caller.
   *
   * @param destination
   *          where to write the resource
   *
   * @return {@code true} if there was data to move and the move was successful,
   *         {@code false} if there was no data to move
   *
   * @throws InteractiveSpacesException
   *           something bad happened during the move
   */
  boolean copyTo(OutputStream destination);
}
