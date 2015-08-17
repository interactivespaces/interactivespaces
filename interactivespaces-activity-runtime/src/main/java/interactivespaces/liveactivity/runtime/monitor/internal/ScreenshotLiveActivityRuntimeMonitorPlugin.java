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
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.NativeApplicationDescription;
import interactivespaces.util.process.NativeApplicationRunnerCollection;
import interactivespaces.util.process.StandardNativeApplicationRunnerCollection;

import com.google.common.collect.Lists;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The runtime monitor plugin that performs screenshots.
 *
 * @author Keith M. Hughes
 */
public class ScreenshotLiveActivityRuntimeMonitorPlugin extends BaseLiveActivityRuntimeMonitorPlugin {

  /**
   * The filename prefix for the screenshot file.
   */
  private static final String SCREENSHOT_FILE_PREFIX = "screenshot-";

  /**
   * The file extension for the screenshot file.
   */
  private static final String SCREENSHOT_FILE_EXTENSION = ".png";

  /**
   * The amount of time to wait for a screenshot to finish, in milliseconds.
   */
  private static final int CONFIGURATION_DEFAULT_SCREENSHOT_DELAY = 5000;

  /**
   * The date time format for the timestamp placed in the output filename.
   */
  private static final String FILECOMPONENT_DATETIME_FORMAT = "yyyyMMdd-HHmmss";

  /**
   * Configuration property giving the location of the application executable relative to the application installation
   * directory.
   */
  public static final String CONFIGURATION_SCREENSHOT_EXECUTABLE =
      "space.activityruntime.monitor.screenshot.executable";

  /**
   * Configuration property giving the flags for the application executable relative to the application installation
   * directory.
   */
  public static final String CONFIGURATION_SCREENSHOT_EXECUTABLE_FLAGS =
      "space.activityruntime.monitor.screenshot.executable.flags";

  /**
   * Configuration property for how long to wait when taking a screenshot.
   */
  public static final String CONFIGURATION_SCREENSHOT_DELAY = "space.activityruntime.screenshot.delay";

  /**
   * Prefix for screenshots.
   */
  private static final String WEB_REQUEST_SCREENSHOT_PREFIX = "/screenshot/";

  /**
   * The screenshot executable.
   */
  private String screenshotExecutable;

  /**
   * The executable flags for the screenshot executable. {@code 0} is used for the file for the screenshot.
   */
  private MessageFormat screenshotExecutableFlags;

  /**
   * How long to sleep for screenshots.
   */
  private int screenshotSleepTime;

  /**
   * The live activity runtime being monitored.
   */
  private LiveActivityRuntime liveActivityRuntime;

  /**
   * The functionality descriptors for this plugin.
   */
  private List<PluginFunctionalityDescriptor> functionalityDescriptors = Collections.unmodifiableList(Lists
      .newArrayList(new PluginFunctionalityDescriptor(WEB_REQUEST_SCREENSHOT_PREFIX, "Machine Screenshot")));

  /**
   * File support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void onStartup() {
    liveActivityRuntime = getMonitorService().getLiveActivityRuntime();
    InteractiveSpacesEnvironment spaceEnvironment = liveActivityRuntime.getSpaceEnvironment();
    Configuration configuration = spaceEnvironment.getSystemConfiguration();

    screenshotExecutable = configuration.getPropertyString(CONFIGURATION_SCREENSHOT_EXECUTABLE);

    screenshotSleepTime =
        configuration.getPropertyInteger(CONFIGURATION_SCREENSHOT_DELAY, CONFIGURATION_DEFAULT_SCREENSHOT_DELAY);
    screenshotExecutable = "/usr/bin/gnome-screenshot";
    screenshotExecutableFlags = new MessageFormat("-f {0}");

    if (screenshotExecutable != null) {
      File executableFile = new File(screenshotExecutable);
      if (!executableFile.canExecute()) {
        executableFile.setExecutable(true);
      }
    }
  }

  @Override
  public String getUrlPrefix() {
    return WEB_REQUEST_SCREENSHOT_PREFIX;
  }

  @Override
  public List<PluginFunctionalityDescriptor> getFunctionalityDescriptors() {
    return functionalityDescriptors;
  }

  @Override
  protected void onHandleRequest(HttpRequest request, HttpResponse response, String fullPath) throws Exception {
    File screenshotFile = captureScreenshots();

    fileSupport.copyFileToStream(screenshotFile, response.getOutputStream(), false);
  }

  /**
   * Capture the screenshot.
   *
   * @return the file where the screenshot has been captured
   *
   * @throws InterruptedException
   *           the thread waiting for the screenshot to complete
   */
  private File captureScreenshots() throws InterruptedException {
    NativeApplicationDescription description = new NativeApplicationDescription();
    description.setExecutablePath(screenshotExecutable);

    File parentFolder = liveActivityRuntime.getSpaceEnvironment().getFilesystem().getTempDirectory();

    SimpleDateFormat formatter = new SimpleDateFormat(FILECOMPONENT_DATETIME_FORMAT);
    Date now = new Date(liveActivityRuntime.getSpaceEnvironment().getTimeProvider().getCurrentTime());
    File screenshotFile =
        fileSupport.newFile(parentFolder, SCREENSHOT_FILE_PREFIX + formatter.format(now) + SCREENSHOT_FILE_EXTENSION);

    // Make sure the filename is appropriately escaped for any spaces in the file path.
    String escapedFilepath = screenshotFile.getAbsolutePath().replaceAll("\\s", "\\\\$1");
    description.parseArguments(screenshotExecutableFlags.format(new Object[] { escapedFilepath }));

    NativeApplicationRunnerCollection runnerCollection =
        new StandardNativeApplicationRunnerCollection(liveActivityRuntime.getSpaceEnvironment(), liveActivityRuntime
            .getSpaceEnvironment().getLog());

    try {
      // TODO(keith): Once used, should this runner collection stay running until plugin shutdown?
      runnerCollection.startup();
      runnerCollection.runNativeApplicationRunner(description, screenshotSleepTime);

      return screenshotFile;
    } finally {
      runnerCollection.shutdown();
    }
  }
}
