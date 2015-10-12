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

import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.LiveActivityRuntime;
import interactivespaces.liveactivity.runtime.monitor.PluginFunctionalityDescriptor;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.service.web.HttpResponseCode;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.core.configuration.CoreConfiguration;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.NativeApplicationDescription;
import interactivespaces.util.process.NativeApplicationRunnerCollection;
import interactivespaces.util.process.StandardNativeApplicationRunnerCollection;
import interactivespaces.util.web.CommonMimeTypes;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The runtime monitor plugin that performs screenshots.
 *
 * @author Keith M. Hughes
 */
public class ScreenshotLiveActivityRuntimeMonitorPlugin extends BaseLiveActivityRuntimeMonitorPlugin {

  /**
   * The name of the directory inside the container's tmp folder where screenshots will live.
   */
  public static final String SCREENSHOT_DIRECTORY_NAME = "screenshots";

  /**
   * The prefix of the default executable name for screenshots. It will have the operating system tacked onto the end.
   */
  public static final String SCREENSHOT_EXECUTABLE_PREFIX_DEFAULT = "screenshot.";

  /**
   * The sub folder in the controller library folders where the default screenshot scripts are kept.
   */
  public static final String LIBRARY_NATIVE_FOLDER = "native";

  /**
   * The filename prefix for the screenshot file.
   */
  private static final String SCREENSHOT_FILE_PREFIX = "screenshot-";

  /**
   * The file extension for the screenshot file.
   */
  private static final String SCREENSHOT_FILE_EXTENSION = "png";

  /**
   * The amount of time to wait for a screenshot to finish, in milliseconds.
   */
  private static final int CONFIGURATION_DEFAULT_SCREENSHOT_DELAY = 5000;

  /**
   * The date time format for the timestamp placed in the output filename.
   */
  private static final String SCREENSHOT_FILENAME_DATETIME_FORMAT = "yyyyMMdd-HHmmss";

  /**
   * The formatter for the date portion of a screenshot filename.
   */
  private static final SimpleDateFormat SCREENSHOT_FILENAME_DATETIME_FORMATTER = new SimpleDateFormat(
      SCREENSHOT_FILENAME_DATETIME_FORMAT);

  /**
   * Configuration property giving the location of the application executable relative to the application installation
   * directory.
   */
  public static final String CONFIGURATION_SCREENSHOT_EXECUTABLE_PREFIX =
      "space.activityruntime.monitor.screenshot.executable.";

  /**
   * Configuration property for how long to wait when taking a screenshot.
   */
  public static final String CONFIGURATION_SCREENSHOT_DELAY = "space.activityruntime.screenshot.delay";

  /**
   * URL prefix for screenshots.
   */
  private static final String URL_PREFIX_SCREENSHOT = "/screenshot";

  /**
   * A file comparator that will order by ascending file names.
   */
  public static final Comparator<File> FILE_COMPARATOR_DATE_DESCENDING = new Comparator<File>() {
    @Override
    public int compare(File o1, File o2) {
      // Math because modified times are longs and we need an int. Otherwise would just subtract and return.
      long difference = o2.lastModified() - o1.lastModified();
      if (difference < 0) {
        return -1;
      } else if (difference > 0) {
        return 1;
      } else {
        // If dates are equivalent, then sort by name in increasing order.
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    }
  };

  /**
   * The screenshot executable.
   */
  private String screenshotExecutable;

  /**
   * How long to sleep for screenshots.
   */
  private int screenshotSleepTime;

  /**
   * The live activity runtime being monitored.
   */
  private LiveActivityRuntime liveActivityRuntime;

  /**
   * The folder for keeping screenshots.
   */
  private File screenshotFolder;

  /**
   * The filepath to the screenshot folder with all spaces properly escaped.
   */
  private String escapedSceenshotFolderFilepath;

  /**
   * The functionality descriptors for this plugin.
   */
  private List<PluginFunctionalityDescriptor> functionalityDescriptors = Collections.unmodifiableList(Lists
      .newArrayList(new PluginFunctionalityDescriptor(URL_PREFIX_SCREENSHOT, "Machine Screenshot")));

  /**
   * File support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void onStartup() {
    liveActivityRuntime = getMonitorService().getLiveActivityRuntime();
    InteractiveSpacesEnvironment spaceEnvironment = liveActivityRuntime.getSpaceEnvironment();
    Configuration configuration = spaceEnvironment.getSystemConfiguration();

    String platformOs = configuration.getPropertyString(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_PLATFORM_OS);
    String screenShotExecutableDefault =
        fileSupport.newFile(
            getMonitorService().getLiveActivityRuntime().getSpaceEnvironment().getFilesystem()
                .getLibraryDirectory(LIBRARY_NATIVE_FOLDER), SCREENSHOT_EXECUTABLE_PREFIX_DEFAULT + platformOs)
            .getAbsolutePath();
    screenshotExecutable =
        configuration.getPropertyString(CONFIGURATION_SCREENSHOT_EXECUTABLE_PREFIX + platformOs,
            screenShotExecutableDefault);

    screenshotSleepTime =
        configuration.getPropertyInteger(CONFIGURATION_SCREENSHOT_DELAY, CONFIGURATION_DEFAULT_SCREENSHOT_DELAY);

    if (screenshotExecutable != null) {
      File executableFile = fileSupport.newFile(screenshotExecutable);
      if (!executableFile.canExecute()) {
        executableFile.setExecutable(true);
      }
    }

    screenshotFolder =
        liveActivityRuntime.getSpaceEnvironment().getFilesystem().getTempDirectory(SCREENSHOT_DIRECTORY_NAME);
    escapedSceenshotFolderFilepath = screenshotFolder.getAbsolutePath().replaceAll("\\s", "\\\\$1");
  }

  @Override
  public String getUrlPrefix() {
    return URL_PREFIX_SCREENSHOT;
  }

  @Override
  public List<PluginFunctionalityDescriptor> getFunctionalityDescriptors() {
    return functionalityDescriptors;
  }

  @Override
  protected void onHandleRequest(HttpRequest request, HttpResponse response, String fullPath) throws Exception {
    String requestPath = request.getUri().getPath().substring(URL_PREFIX_SCREENSHOT.length());
    String[] requestPathComponents = requestPath.split(HttpConstants.URL_PATH_COMPONENT_SEPARATOR);

    if (requestPathComponents.length == 1) {
      performScreenshot(response);
    } else {
      // If here, the URL was something like /base/filename where base was the value of the constant
      // URL_PREFIX_SCREENSHOT. The request path above then becomes /filename. The split will have an empty string in
      // the first component of the array and the filename is the second component, hence the 1 below.
      showScreenshot(response, requestPathComponents[1]);
    }
  }

  /**
   * Do the screenshot and then show an index of the directory containing all screenshots.
   *
   * @param response
   *          the response to write out the results on
   *
   * @throws InterruptedException
   *           the thread was interrupted
   * @throws IOException
   *           an IO error happened
   */
  private void performScreenshot(HttpResponse response) throws InterruptedException, IOException {
    if (screenshotExecutable != null) {
      captureScreenshots();

      showDirectoryIndex(response);
    } else {
      reportLackOfScreenshotExecutable(response);
    }
  }

  /**
   * The screenshot executable is not available. Report it.
   *
   * @param response
   *          the HTTP response
   *
   * @throws IOException
   *           something bad happened while writing IO
   */
  private void reportLackOfScreenshotExecutable(HttpResponse response) throws IOException {
    getMonitorService().getLog().warn("Screenshot executable is null in live activity runtime remote monitor");

    response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
    response.setContentType(CommonMimeTypes.MIME_TYPE_TEXT_PLAIN);
    OutputStream outputStream = response.getOutputStream();
    outputStream.write("Screenshot executable not set.".getBytes());
    outputStream.flush();
  }

  /**
   * Show an index of the files in the screenshot directory.
   *
   * @param response
   *          the HTTP response to write the index into
   *
   * @throws IOException
   *           there was an error while writing
   */
  private void showDirectoryIndex(HttpResponse response) throws IOException {
    OutputStream outputStream = response.getOutputStream();

    addCommonPageHeader(outputStream, "Directory listing for screenshots");

    listDirectoryFiles(URL_PREFIX_SCREENSHOT, screenshotFolder, outputStream, FILE_COMPARATOR_DATE_DESCENDING);

    endWebResponse(outputStream, true);
  }

  /**
   * Send a screenshot file contents to the browser.
   *
   * @param response
   *          the HTTP response that the image should be placed in
   * @param screenshotFilename
   *          the name of the screenshot file to show
   *
   * @throws IOException
   *           unable to display the image due to an IO error
   */
  private void showScreenshot(HttpResponse response, String screenshotFilename) throws IOException {
    OutputStream outputStream = response.getOutputStream();

    File screenshotFile = fileSupport.newFile(screenshotFolder, screenshotFilename);
    if (fileSupport.exists(screenshotFile)) {
      response.setResponseCode(HttpResponseCode.OK);
      response.setContentType(CommonMimeTypes.MIME_TYPE_IMAGE_PNG);

      fileSupport.copyFileToStream(screenshotFile, outputStream, false);
      outputStream.flush();
    } else {
      String message = String.format("Screenshot file not found: %s", fileSupport.getAbsolutePath(screenshotFile));
      getMonitorService().getLog().warn(message);

      response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
      response.setContentType(CommonMimeTypes.MIME_TYPE_TEXT_PLAIN);
      outputStream.write(message.getBytes());
      outputStream.flush();
    }
  }

  /**
   * Capture the screenshot.
   *
   * @throws InterruptedException
   *           the thread waiting for the screenshot to complete
   */
  private void captureScreenshots() throws InterruptedException {
    NativeApplicationDescription description = new NativeApplicationDescription();
    description.setExecutablePath(screenshotExecutable);

    Date now = new Date(liveActivityRuntime.getSpaceEnvironment().getTimeProvider().getCurrentTime());
    String screenshotFilenamePrefix = SCREENSHOT_FILE_PREFIX + SCREENSHOT_FILENAME_DATETIME_FORMATTER.format(now);

    description.parseArguments(escapedSceenshotFolderFilepath).addArguments(screenshotFilenamePrefix,
        SCREENSHOT_FILE_EXTENSION);

    NativeApplicationRunnerCollection runnerCollection =
        new StandardNativeApplicationRunnerCollection(liveActivityRuntime.getSpaceEnvironment(), liveActivityRuntime
            .getSpaceEnvironment().getLog());

    try {
      // TODO(keith): Once used, should this runner collection stay running until plugin shutdown?
      runnerCollection.startup();
      runnerCollection.runNativeApplicationRunner(description, screenshotSleepTime);
    } finally {
      runnerCollection.shutdown();
    }
  }
}
