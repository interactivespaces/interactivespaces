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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.web.HttpResponseCode;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Convenience handler for redirecting directory requests to a designated page.
 *
 * @author Trevor Pering
 */
public class HttpDirectoryRedirectRequestHandler implements HttpDynamicRequestHandler {

  /**
   * Regexp pattern for determining if a target is absolute or not.
   */
  private static final Pattern ABSOLUTE_REDIRECT_PATTERN = Pattern.compile("(/|https?://).*");

  /**
   * Redirection target.
   */
  private final String redirectTarget;

  /**
   * Request prefix for this handler.
   */
  private final String requestPrefix;

  /**
   * The filesystem directory that this is associated with.
   */
  private final File installDirectory;

  /**
   * {@code true} if the redirect target is absolute.
   */
  private final boolean isAbsoluteTarget;

  /**
   * Construct a new instance.
   *
   * @param requestPrefix
   *          request prefix for this handler
   * @param contentDirectory
   *          filesystem directory of served content
   * @param redirectTarget
   *          target for redirection requests
   */
  public HttpDirectoryRedirectRequestHandler(String requestPrefix, File contentDirectory, String redirectTarget) {
    this.requestPrefix = requestPrefix;
    this.installDirectory = contentDirectory;
    this.isAbsoluteTarget = ABSOLUTE_REDIRECT_PATTERN.matcher(redirectTarget).matches();
    this.redirectTarget = redirectTarget;
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) {
    try {
      String fullPath = request.getUri().getPath();
      if (!fullPath.startsWith(requestPrefix)) {
        throw new SimpleInteractiveSpacesException("Bad path prefix: " + fullPath);
      }
      String relPath = fullPath.substring(requestPrefix.length());
      File relFile = new File(relPath);
      File absFile = new File(installDirectory, relFile.getPath());

      if (absFile.isDirectory()) {
        boolean needAdjustedTarget = !isAbsoluteTarget && !fullPath.endsWith("/");
        String effectiveTarget = needAdjustedTarget ? (fullPath + "/" + redirectTarget) : redirectTarget;
        response.setResponseCode(HttpResponseCode.TEMPORARY_REDIRECT);
        response.getContentHeaders().put("Location", effectiveTarget);
      } else {
        response.setResponseCode(HttpResponseCode.NOT_FOUND);
      }
    } catch (Exception e) {
      response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
    }
  }

}
