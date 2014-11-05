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

package interactivespaces.util.web;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A MIME resolver which uses the file prefix to look up in an internal map.
 *
 * @author Keith M. Hughes
 */
public class MapExtensionMimeResolver implements MimeResolver {

  /**
   * What separates the fix extension from the file name.
   */
  private static final String FILE_EXTENSION_SEPARATOR = ".";

  /**
   * The mapping of extensions to their MIME value.
   */
  private final Map<String, String> extensionsToMime = Maps.newConcurrentMap();

  @Override
  public String resolve(String contentPath) {
    int periodPos = contentPath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
    if (periodPos >= 0) {
      String extension = contentPath.substring(periodPos + 1);

      return extensionsToMime.get(extension);
    } else {
      return null;
    }
  }

  /**
   * Add in a new MIME type.
   *
   * @param extension
   *          the file extension for the MIME type
   * @param mimeType
   *          the MIME type
   *
   * @return this resolver
   */
  public MapExtensionMimeResolver addMimeType(String extension, String mimeType) {
    extensionsToMime.put(extension, mimeType);

    return this;
  }
}
