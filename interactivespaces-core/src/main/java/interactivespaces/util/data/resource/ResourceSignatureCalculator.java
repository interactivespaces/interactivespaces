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

import java.io.File;
import java.io.InputStream;
import java.net.URI;

/**
 * Calculate signatures for resources.
 *
 * @author Keith M. Hughes
 */
public interface ResourceSignatureCalculator {

  /**
   * The URI scheme for a file.
   */
  String URI_SCHEME_FILE = "file";

  /**
   * Get the signature for a resource.
   *
   * @param resourceFile
   *          the resource file
   *
   * @return the signature for the resource
   */
  String getResourceSignature(File resourceFile);

  /**
   * Get the signature for a resource.
   *
   * @param resourceUri
   *          the URI for the resource
   *
   * @return the signature for the resource, or {@code null} if could not be calculated
   */
  String getResourceSignature(URI resourceUri);

  /**
   * Get the signature for the resource stream.
   *
   * @param inputStream
   *          stream which to digest
   *
   * @return bundle signature for the given stream
   */
  String getResourceSignature(InputStream inputStream);
}
