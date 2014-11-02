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

package interactivespaces.service.web.server;

import interactivespaces.util.web.MimeResolver;

/**
 * An HTTP request handler for static content.
 *
 * @author Keith M. Hughes
 */
public interface HttpStaticContentRequestHandler {

  /**
   * Set the MIME resolver for the static content handler.
   *
   * @param resolver
   *          the MIME resolver, can be {@code null}
   */
  void setMimeResolver(MimeResolver resolver);

  /**
   * Get the current MIME resolver for the static content handler.
   *
   * @param <T>
   *          the type of the MIME resolver
   *
   * @return the MIME resolver, can be {@code null}
   */
  <T extends MimeResolver> T getMimeResolver();
}
