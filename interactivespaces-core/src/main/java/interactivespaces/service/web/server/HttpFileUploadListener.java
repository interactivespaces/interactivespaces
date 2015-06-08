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

package interactivespaces.service.web.server;

/**
 * A listener for new file uploads.
 *
 * @author Keith M. Hughes
 *
 * @deprecated Use {@link HttpDynamicPostRequestHandler}
 */
@Deprecated
public interface HttpFileUploadListener {

  /**
   * A new file has been uploaded.
   *
   * @param fileUpload
   *          the file which has just been uploaded
   */
  void handleHttpFileUpload(HttpFileUpload fileUpload);
}
