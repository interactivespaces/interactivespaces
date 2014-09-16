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
import java.nio.charset.Charset;
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
   * See {@link #getContentAsString(String, Charset)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   *
   * @return the content
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  String getContentAsString(String sourceUri) throws InteractiveSpacesException;

  /**
   * Get the content of the source URI and return as a string.
   *
   * <p>
   * This method blocks until the transfer is complete or it fails.
   *
   * <p>
   * This method will fail if there are not enough connections available and blocking until a connection becomes ready
   * is not enabled.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param charset
   *          the charset the content will be in
   *
   * @return the content
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  String getContentAsString(String sourceUri, Charset charset) throws InteractiveSpacesException;

  /**
   * Copy the contents from the source URI to the destination file.
   *
   * <p>
   * This method blocks until the transfer is complete or it fails.
   *
   * <p>
   * This method will fail if there are not enough connections available and blocking until a connection becomes ready
   * is not enabled.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param destination
   *          where to copy the content to
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  void copy(String sourceUri, File destination) throws InteractiveSpacesException;

  /**
   * Copy content to a remote web server using an HTTP POST.
   *
   * <p>
   * This method will fail if there are not enough connections available and blocking until a connection becomes ready
   * is not enabled.
   *
   * @param destinationUri
   *          URI for the destination server
   * @param source
   *          the content to be copied
   * @param sourceParameterName
   *          the name of the parameter for the file
   * @param params
   *          any parameters which should be included in the post, can be {@code null}
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  void copyTo(String destinationUri, File source, String sourceParameterName, Map<String, String> params)
      throws InteractiveSpacesException;

  /**
   * Copy content to a remote web server using an HTTP POST.
   *
   * <p>
   * This method will fail if there are not enough connections available and blocking until a connection becomes ready
   * is not enabled.
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
   *          any parameters which should be included in the post, can be {@code null}
   *
   * @throws InteractiveSpacesException
   *           if transfer was not successful
   */
  void copyTo(String destinationUri, InputStream source, String sourceFileName, String sourceParameterName,
      Map<String, String> params) throws InteractiveSpacesException;

  /**
   * Get the total number of simultaneous connections allowed.
   *
   * @return the total number of connections, or {@code 0} if there is no limit
   */
  int getTotalConnectionsAllowed();
}
