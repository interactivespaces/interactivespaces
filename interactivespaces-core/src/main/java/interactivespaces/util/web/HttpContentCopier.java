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

package interactivespaces.util.web;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.resource.ManagedResource;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Copy content from an HTTP URL to a file.
 *
 * <p>
 * It is safe to have multiple threads copying content.
 *
 * @author Keith M. Hughes
 */
public interface HttpContentCopier extends ManagedResource {

  /**
   * Copy the contents from the source URI to the destination file.
   *
   * <p>
   * This method blocks until the transfer is complete or it fails.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param destination
   *          where to copy the content to
   */
  void copy(String sourceUri, File destination);

  /**
   * Copy content to a remote web server using an HTTP POST.
   *
   * @param destinationUri
   *          URI for the destination server
   * @param source
   *          the content to be copied
   * @param sourceParameterName
   *          the name of the parameter for the file
   * @param params
   *          any parameters which should be included in the post, can be
   *          {@code null}
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  void copyTo(String destinationUri, File source, String sourceParameterName,
      Map<String, String> params);

  /**
   * Copy content to a remote web server using an HTTP POST.
   *
   * @param destinationUri
   *          URI for the destination server
   * @param source
   *          the content to be copied
   * @param sourceFileName
   *          the file name to be given to the source
   * @param sourceParameterName
   *          the name of the parameter for the file
   * @param params
   *          any parameters which should be included in the post, can be
   *          {@code null}
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  void copyTo(String destinationUri, InputStream source, String sourceFileName,
      String sourceParameterName, Map<String, String> params);
}
