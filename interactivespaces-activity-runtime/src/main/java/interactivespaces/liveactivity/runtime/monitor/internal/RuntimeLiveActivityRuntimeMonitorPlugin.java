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

package interactivespaces.liveactivity.runtime.monitor.internal;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.liveactivity.runtime.monitor.PluginFunctionalityDescriptor;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.system.InteractiveSpacesFilesystem;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * The runtime monitor plugin for looking at the runtime itself.
 *
 * @author Keith M. Hughes
 */
public class RuntimeLiveActivityRuntimeMonitorPlugin extends BaseLiveActivityRuntimeMonitorPlugin {

  /**
   * The runtime filesystem section name for the temporary data folder.
   */
  private static final String RUNTIME_FILESYSTEM_SECTION_TMP = "tmp";

  /**
   * The runtime filesystem section name for the permanent data folder.
   */
  private static final String RUNTIME_FILESYSTEM_SECTION_DATA = "data";

  /**
   * The runtime filesystem section name for the log folder.
   */
  private static final String RUNTIME_FILESYSTEM_SECTION_LOG = "log";

  /**
   * The runtime filesystem section name for the library folder.
   */
  private static final String RUNTIME_FILESYSTEM_SECTION_LIBRARY = "library";

  /**
   * The URL prefix for accessing the runtime.
   */
  private static final String URL_PREFIX_RUNTIME = "/runtime/";

  /**
   * The URL prefix for accessing the runtime.
   */
  private static final String URL_PREFIX_RUNTIME_FILESYSTEM = URL_PREFIX_RUNTIME + "filesystem";

  /**
   * The position in the request path components for the section.
   */
  private static final int PATH_COMPONENTS_POSITION_SECTION = 1;

  /**
   * The position in the request path components for the beginning of the file path relative to the root directory of
   * the section.
   */
  private static final int PATH_COMPONENTS_POSITION_PATH_BEGIN = 2;

  /**
   * The functionality descriptors for this plugin.
   */
  private List<PluginFunctionalityDescriptor> functionalityDescriptors = Collections.unmodifiableList(Lists
      .newArrayList(new PluginFunctionalityDescriptor(URL_PREFIX_RUNTIME_FILESYSTEM, "Runtime Filesystem")));

  @Override
  public String getUrlPrefix() {
    return URL_PREFIX_RUNTIME;
  }

  @Override
  public List<PluginFunctionalityDescriptor> getFunctionalityDescriptors() {
    return functionalityDescriptors;
  }

  @Override
  protected void onHandleRequest(HttpRequest request, HttpResponse response, String fullPath) throws Throwable {
    if (fullPath.startsWith(URL_PREFIX_RUNTIME_FILESYSTEM)) {
      writeRuntimeInformationPage(request, response);
    } else {
      reportError(response, fullPath, null);
    }
  }

  /**
   * Write out the initial page for the runtime.
   *
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   *
   * @throws Throwable
   *           an exception happened while processing
   */
  private void writeRuntimeInformationPage(HttpRequest request, HttpResponse response) throws Throwable {

    String requestPath = request.getUri().getPath().substring(URL_PREFIX_RUNTIME_FILESYSTEM.length());
    String[] requestPathComponents = requestPath.split("/");

    if (requestPathComponents.length == 1) {
      displayAllSectionsListing(response);
    } else {
      displaySpecificDirectorySection(response, requestPathComponents);
    }
  }

  /**
   * Display a specific section of the filesystem.
   *
   * @param response
   *          the HTTP response
   * @param requestPathComponents
   *          the components of the request path
   *
   * @throws Throwable
   *           an error happened during processing
   */
  private void displaySpecificDirectorySection(HttpResponse response, String[] requestPathComponents) throws Throwable {
    InteractiveSpacesFilesystem filesystem =
        getMonitorService().getLiveActivityRuntime().getSpaceEnvironment().getFilesystem();
    File dir = null;
    String section = requestPathComponents[PATH_COMPONENTS_POSITION_SECTION];
    switch (section) {
      case RUNTIME_FILESYSTEM_SECTION_TMP:
        dir = filesystem.getTempDirectory();
        break;

      case RUNTIME_FILESYSTEM_SECTION_DATA:
        dir = filesystem.getDataDirectory();
        break;

      case RUNTIME_FILESYSTEM_SECTION_LOG:
        dir = filesystem.getLogsDirectory();
        break;

      case RUNTIME_FILESYSTEM_SECTION_LIBRARY:
        dir = filesystem.getLibraryDirectory();
        break;

      default:
        SimpleInteractiveSpacesException.throwFormattedException(
            "Unknown filesystem area %s for live activity runtime", section);
    }

    displayFilesystemSection(response, URL_PREFIX_RUNTIME_FILESYSTEM, section, requestPathComponents,
        PATH_COMPONENTS_POSITION_PATH_BEGIN, dir);

  }

  /**
   * Display the component listing for the live activity.
   *
   * @param response
   *          the HTTP response
   *
   * @throws Throwable
   *           an exception happened while processing
   */
  private void displayAllSectionsListing(HttpResponse response) throws Throwable {
    OutputStream outputStream = response.getOutputStream();

    addCommonPageHeader(outputStream, "Directory listing for live activity runtime");

    String baseUrl = URL_PREFIX_RUNTIME_FILESYSTEM + "/";

    StringBuilder builder = new StringBuilder();
    builder.append("<ul>");
    addFilesystemDirectoryEntry(builder, baseUrl, RUNTIME_FILESYSTEM_SECTION_LOG);
    addFilesystemDirectoryEntry(builder, baseUrl, RUNTIME_FILESYSTEM_SECTION_DATA);
    addFilesystemDirectoryEntry(builder, baseUrl, RUNTIME_FILESYSTEM_SECTION_TMP);
    addFilesystemDirectoryEntry(builder, baseUrl, RUNTIME_FILESYSTEM_SECTION_LIBRARY);
    builder.append("</ul>");
    outputStream.write(builder.toString().getBytes());

    endWebResponse(outputStream, true);
  }
}
