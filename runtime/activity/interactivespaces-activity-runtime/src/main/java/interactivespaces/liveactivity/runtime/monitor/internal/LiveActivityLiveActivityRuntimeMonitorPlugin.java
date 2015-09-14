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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import interactivespaces.liveactivity.runtime.LiveActivityStorageManager;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.monitor.PluginFunctionalityDescriptor;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A runtime monitor plugin for looking at live activity information.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityLiveActivityRuntimeMonitorPlugin extends BaseLiveActivityRuntimeMonitorPlugin {

  /**
   * String formatting string for directory listing for a live activity's file system.
   */
  private static final String STRING_FORMAT_DIRECTORY_LISTING_FOR_LIVE_ACTIVITIES =
      "Directory listing for live activity %s";

  /**
   * The separator between components in a formatted activity name.
   */
  private static final String ACTIVITY_NAME_SEPARATOR = "-";

  /**
   * The activity filesystem section name for the temporary data folder.
   */
  private static final String ACTIVITY_FILESYSTEM_SECTION_TMP = "tmp";

  /**
   * The activity filesystem section name for the permanent data folder.
   */
  private static final String ACTIVITY_FILESYSTEM_SECTION_DATA = "data";

  /**
   * The activity filesystem section name for the log folder.
   */
  private static final String ACTIVITY_FILESYSTEM_SECTION_LOG = "log";

  /**
   * The activity filesystem section name for the internal folder.
   */
  private static final String ACTIVITY_FILESYSTEM_SECTION_INTERNAL = "internal";

  /**
   * The activity filesystem section name for the install folder.
   */
  private static final String ACTIVITY_FILESYSTEM_SECTION_INSTALL = "install";

  /**
   * The web server prefix for the activity list.
   */
  private static final String URL_PREFIX_ACTIVITY = "/activity/";

  /**
   * The prefix for installed activities.
   */
  private static final String URL_PREFIX_ACTIVITY_FILESYSTEM = URL_PREFIX_ACTIVITY + "filesystem/";

  /**
   * The location in request path components for the section.
   */
  private static final int PATH_COMPONENTS_POSITION_SECTION = 1;

  /**
   * The location in the request path components for the file path into the section.
   */
  private static final int PATH_COMPONENTS_POSITION_PATH_BEGIN = 2;

  /**
   * The functionality descriptors for this plugin.
   */
  private List<PluginFunctionalityDescriptor> functionalityDescriptors = Collections.unmodifiableList(Lists
      .newArrayList(new PluginFunctionalityDescriptor(URL_PREFIX_ACTIVITY, "Activities")));

  @Override
  public String getUrlPrefix() {
    return URL_PREFIX_ACTIVITY;
  }

  @Override
  public List<PluginFunctionalityDescriptor> getFunctionalityDescriptors() {
    return functionalityDescriptors;
  }

  @Override
  protected void onHandleRequest(HttpRequest request, HttpResponse response, String fullPath) throws Throwable {
    if (fullPath.equals(getUrlPrefix()) || fullPath.equals(URL_PREFIX_ACTIVITY_FILESYSTEM)) {
      writeActivityListPage(response);
    } else if (fullPath.startsWith(URL_PREFIX_ACTIVITY_FILESYSTEM)) {
      writeActivityInformationPage(request, response);
    } else {
      reportError(response, fullPath, null);
    }
  }

  /**
   * Write out a listing of all live activities in the runtime.
   *
   * @param response
   *          the HTTP response to write on
   *
   * @throws Throwable
   *           an exception happened while processing
   */
  private void writeActivityListPage(HttpResponse response) throws Throwable {
    OutputStream outputStream = startWebResponse(response, false);
    addCommonPageHeader(outputStream, "Activity listing");

    StringBuilder builder = new StringBuilder();
    outputActivityEntries(builder);

    outputStream.write(builder.toString().getBytes());

    endWebResponse(outputStream, false);
  }

  /**
   * Output all activity entries.
   *
   * @param builder
   *          the output builder for the response
   */
  private void outputActivityEntries(StringBuilder builder) {
    Collection<InstalledLiveActivity> activities =
        getMonitorService().getLiveActivityRuntime().getAllInstalledLiveActivities();
    builder.append("<table>");
    for (InstalledLiveActivity activity : activities) {
      writeActivityEntry(builder, activity);
    }
    builder.append("</table>");
  }

  /**
   * Write out an individual activity entry.
   *
   * @param builder
   *          the output builder for the response
   * @param activity
   *          the activity information
   */
  private void writeActivityEntry(StringBuilder builder, InstalledLiveActivity activity) {
    String uuid = activity.getUuid();
    String link = URL_PREFIX_ACTIVITY_FILESYSTEM + uuid;

    String name = getFormattedActivityName(activity);

    builder.append("<tr><td class='uuid'>");
    addLink(builder, link, uuid);
    builder.append("</td> <td class='name'>");
    addLink(builder, link, name);
    builder.append("</td></tr>");
  }

  /**
   * Get a formatted activity name.
   *
   * @param activity
   *          the activity
   *
   * @return the formatted name
   */
  private String getFormattedActivityName(InstalledLiveActivity activity) {
    return activity.getIdentifyingName() + ACTIVITY_NAME_SEPARATOR + activity.getVersion();
  }

  /**
   * Write out the initial page for an activity.
   *
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   *
   * @throws Throwable
   *           an exception happened while processing
   */
  private void writeActivityInformationPage(HttpRequest request, HttpResponse response) throws Throwable {

    String requestPath = request.getUri().getPath().substring(URL_PREFIX_ACTIVITY_FILESYSTEM.length());
    String[] requestPathComponents = requestPath.split(HttpConstants.URL_PATH_COMPONENT_SEPARATOR);

    String uuid = requestPathComponents[0];
    if (requestPathComponents.length == 1) {
      displayAllSectionsListing(response, uuid);
    } else {
      displaySpecificDirectorySection(response, requestPathComponents, uuid);
    }
  }

  /**
   * Display a specific section of the filesystem.
   *
   * @param response
   *          the HTTP response
   * @param requestPathComponents
   *          the components of the request path
   * @param uuid
   *          the UUID of the live activity being examined
   *
   * @throws Throwable
   *           an error happened during processing
   */
  private void displaySpecificDirectorySection(HttpResponse response, String[] requestPathComponents, String uuid)
      throws Throwable {
    InternalLiveActivityFilesystem activityFilesystem = getLiveActivityFilesystem(uuid);
    String section = requestPathComponents[PATH_COMPONENTS_POSITION_SECTION];
    String baseUrl = URL_PREFIX_ACTIVITY_FILESYSTEM + uuid;
    File dir = null;
    switch (section) {
      case ACTIVITY_FILESYSTEM_SECTION_TMP:
        dir = activityFilesystem.getTempDataDirectory();
        break;

      case ACTIVITY_FILESYSTEM_SECTION_DATA:
        dir = activityFilesystem.getPermanentDataDirectory();
        break;

      case ACTIVITY_FILESYSTEM_SECTION_LOG:
        dir = activityFilesystem.getLogDirectory();
        break;

      case ACTIVITY_FILESYSTEM_SECTION_INTERNAL:
        dir = activityFilesystem.getInternalDirectory();
        break;

      case ACTIVITY_FILESYSTEM_SECTION_INSTALL:
        dir = activityFilesystem.getInstallDirectory();
        break;

      default:
        SimpleInteractiveSpacesException.throwFormattedException("Unknown filesystem area %s for live activity %s",
            section, uuid);
    }
    displayFilesystemSection(response, baseUrl, section, requestPathComponents, PATH_COMPONENTS_POSITION_PATH_BEGIN,
        dir);

  }

  /**
   * Get the filesystem for a live activity.
   *
   * @param uuid
   *          the UUID of the live activity
   *
   * @return the filesystem
   *
   * @throws InteractiveSpacesException
   *           there is no live activity with the given UUID or something failed when getting the file system
   */
  private InternalLiveActivityFilesystem getLiveActivityFilesystem(String uuid) throws InteractiveSpacesException {
    LiveActivityStorageManager liveActivityStorageManager =
        getMonitorService().getLiveActivityRuntime().getLiveActivityStorageManager();
    if (liveActivityStorageManager.getAllInstalledActivityUuids().contains(uuid)) {
      return liveActivityStorageManager.getActivityFilesystem(uuid);
    } else {
      throw SimpleInteractiveSpacesException.newFormattedException("There is no installed live activity with UUID %s",
          uuid);
    }
  }

  /**
   * Display the component listing for the live activity.
   *
   * @param response
   *          the HTTP response
   * @param uuid
   *          the UUID of the activity
   *
   * @throws Throwable
   *           an exception happened while processing
   */
  private void displayAllSectionsListing(HttpResponse response, String uuid) throws Throwable {
    OutputStream outputStream = response.getOutputStream();

    addCommonPageHeader(outputStream, String.format(STRING_FORMAT_DIRECTORY_LISTING_FOR_LIVE_ACTIVITIES, uuid));

    String baseUrl = URL_PREFIX_ACTIVITY_FILESYSTEM + uuid + "/";

    StringBuilder builder = new StringBuilder();
    builder.append("<ul>");
    addFilesystemDirectoryEntry(builder, baseUrl, ACTIVITY_FILESYSTEM_SECTION_INSTALL);
    addFilesystemDirectoryEntry(builder, baseUrl, ACTIVITY_FILESYSTEM_SECTION_INTERNAL);
    addFilesystemDirectoryEntry(builder, baseUrl, ACTIVITY_FILESYSTEM_SECTION_LOG);
    addFilesystemDirectoryEntry(builder, baseUrl, ACTIVITY_FILESYSTEM_SECTION_DATA);
    addFilesystemDirectoryEntry(builder, baseUrl, ACTIVITY_FILESYSTEM_SECTION_TMP);
    builder.append("</ul>");
    outputStream.write(builder.toString().getBytes());

    endWebResponse(outputStream, true);
  }
}
