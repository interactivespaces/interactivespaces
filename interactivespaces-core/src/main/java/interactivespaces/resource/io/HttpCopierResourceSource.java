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

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.web.HttpContentCopier;

import java.io.File;

/**
 * A resource source that copies from an HTTP copier.
 *
 * @author Keith M. Hughes
 */
public class HttpCopierResourceSource implements ResourceSource {

  /**
   * The URI containing the source to copy.
   */
  private String sourceUri;

  /**
   * The content copier.
   */
  private HttpContentCopier contentCopier;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new source.
   *
   * @param sourceUri
   *          URI for the source
   * @param contentCopier
   *          the content copier to use
   */
  public HttpCopierResourceSource(String sourceUri, HttpContentCopier contentCopier) {
    this.sourceUri = sourceUri;
    this.contentCopier = contentCopier;
  }

  @Override
  public void copyTo(File destination) {
    File intermediate = null;
    try {
      intermediate = fileSupport.createTempFile("interactivespaces-", ".resource");
      contentCopier.copy(sourceUri, intermediate);

      fileSupport.copyFile(intermediate, destination);
    } finally {
      if (intermediate != null && fileSupport.exists(intermediate)) {
        fileSupport.delete(intermediate);
      }
    }
  }

  @Override
  public String getLocation() {
    return sourceUri;
  }
}
