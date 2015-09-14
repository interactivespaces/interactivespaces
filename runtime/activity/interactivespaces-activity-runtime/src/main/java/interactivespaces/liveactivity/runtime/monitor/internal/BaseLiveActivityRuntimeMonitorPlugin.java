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

import interactivespaces.controller.SpaceController;
import interactivespaces.liveactivity.runtime.monitor.LiveActivityRuntimeMonitorPlugin;
import interactivespaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.service.web.HttpResponseCode;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * A base implementation of a runtime monitor plugin.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseLiveActivityRuntimeMonitorPlugin implements LiveActivityRuntimeMonitorPlugin {

  /**
   * The title for the web page for the remote monitoring.
   */
  private static final String MONITOR_WEB_PAGE_TITLE = "Interactive Spaces Live Activity Runtime Remote Monitoring";

  /**
   * The directory entry for going up one level in the directory hierarchy.
   */
  private static final String DIRECTORY_ENTRY_UP_ONE_LEVEL = "..";

  /**
   * The monitor service hosting the plugin.
   */
  private RemoteLiveActivityRuntimeMonitorService monitorService;

  /**
   * A joiner for URLs.
   */
  private Joiner urlJoiner = Joiner.on(HttpConstants.URL_PATH_COMPONENT_SEPARATOR);

  /**
   * A joiner for file paths.
   */
  private Joiner filePathJoiner = Joiner.on(File.separatorChar);

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Get the monitor service hosting the plugin.
   *
   * @return the monitor service
   */
  public RemoteLiveActivityRuntimeMonitorService getMonitorService() {
    return monitorService;
  }

  @Override
  public void startup(RemoteLiveActivityRuntimeMonitorService monitorService, WebServer webServer) {
    this.monitorService = monitorService;

    webServer.addDynamicContentHandler(getUrlPrefix(), false, new HttpDynamicRequestHandler() {
      @Override
      public void handle(HttpRequest request, HttpResponse response) {
        handleRequest(request, response);
      }
    });

    onStartup();
  }

  /**
   * Handle the request, reporting any errors.
   *
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   */
  private void handleRequest(HttpRequest request, HttpResponse response) {
    String fullPath = request.getUri().getPath();
    try {
      onHandleRequest(request, response, fullPath);
    } catch (Throwable e) {
      reportError(response, fullPath, e);
    }
  }

  @Override
  public void shutdown() {
    // Default is do nothing.
  }

  /**
   * Handle the activities response.
   *
   * <p>
   * This method should allow exceptions to be thrown, they will be caught later.
   *
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   * @param fullRequestPath
   *          the full request path
   *
   * @throws Throwable
   *           an exception happened while processing the request
   */
  protected abstract void onHandleRequest(HttpRequest request, HttpResponse response, String fullRequestPath)
      throws Throwable;

  /**
   * Do any extra startup.
   */
  protected void onStartup() {
    // Default is do nothing
  }

  /**
   * An error has happened, report it.
   *
   * @param response
   *          the HTTP response
   * @param fullPath
   *          the full URL path
   * @param e
   *          any exception that happened, can be {@code null}
   */
  protected void reportError(HttpResponse response, String fullPath, Throwable e) {
    getMonitorService().getLiveActivityRuntime().getSpaceEnvironment().getLog()
        .error("Error processing " + fullPath, e);
    response.setResponseCode(HttpResponseCode.BAD_REQUEST);
  }

  /**
   * Start a web response.
   *
   * @param response
   *          the HTTP response
   * @param isPlain
   *          {@code true} if the response is plain text
   *
   * @return the output stream for the response
   *
   * @throws IOException
   *           an error occurred while beginning the response
   */
  protected OutputStream startWebResponse(HttpResponse response, boolean isPlain) throws IOException {
    OutputStream outputStream = response.getOutputStream();

    if (isPlain) {
      response.setContentType("text/plain");
    } else {
      StringBuilder output = new StringBuilder();

      output.append("<!DOCTYPE html><title>").append(MONITOR_WEB_PAGE_TITLE).append("</title>").append("</head>")
          .append("<body>");

      outputStream.write(output.toString().getBytes());
    }

    return outputStream;
  }

  /**
   * Add in the common page header for HTML pages.
   *
   * @param outputStream
   *          the response output stream
   * @param subHeader
   *          the subheader for the page
   *
   * @throws IOException
   *           something bad happened
   */
  protected void addCommonPageHeader(OutputStream outputStream, String subHeader) throws IOException {
    String controllerName =
        monitorService.getLiveActivityRuntime().getSpaceEnvironment().getSystemConfiguration()
            .getPropertyString(SpaceController.CONFIGURATION_CONTROLLER_NAME);
    outputStream.write(String.format(
        "<h1 class='controller-header'><a href='/'>Interactive Spaces Live Activity Runtime: %s</a></h1>",
        controllerName).getBytes());
    if (subHeader != null) {
      outputStream.write(String.format("<h2>%s</h2>", subHeader).getBytes());
    }
  }

  /**
   * End a web response.
   *
   * @param outputStream
   *          the output stream for the response
   * @param isHtml
   *          {@code true} if the response is HTML
   *
   * @throws IOException
   *           something bad happened
   */
  protected void endWebResponse(OutputStream outputStream, boolean isHtml) throws IOException {
    if (!isHtml) {
      StringBuilder output = new StringBuilder();

      output.append("</body>");
      output.append("</html>");

      outputStream.write(output.toString().getBytes());
    }
  }

  /**
   * Display the contents of a file.
   *
   * @param response
   *          the HTTP response
   * @param initialPath
   *          the initial URL path for that triggered finding the file contents
   * @param rootDirectory
   *          the root directory that contains the contents
   * @param pathComponents
   *          the individual steps from the root directory to the file
   *
   * @throws IOException
   *           something bad happened while displaying the contents
   */
  protected void displayFileContents(HttpResponse response, String initialPath, File rootDirectory,
      List<String> pathComponents) throws IOException {
    File file = rootDirectory;
    String completeUrlPath = initialPath;
    boolean isSubDir = false;
    if (!pathComponents.isEmpty()) {
      file = fileSupport.newFile(rootDirectory, filePathJoiner.join(pathComponents));
      completeUrlPath = initialPath + HttpConstants.URL_PATH_COMPONENT_SEPARATOR + urlJoiner.join(pathComponents);
      isSubDir = true;
    }

    boolean isDirectoryListing = fileSupport.isDirectory(file);

    OutputStream outputStream = startWebResponse(response, !isDirectoryListing);
    if (isDirectoryListing) {
      addCommonPageHeader(outputStream, String.format("Directory listing for %s", completeUrlPath));

      if (isSubDir) {
        writeDirectoryEntry(
            outputStream,
            initialPath + HttpConstants.URL_PATH_COMPONENT_SEPARATOR
                + urlJoiner.join(pathComponents.subList(0, pathComponents.size() - 1)), DIRECTORY_ENTRY_UP_ONE_LEVEL);
      }

      listDirectoryFiles(completeUrlPath, file, outputStream);
    } else {
      fileSupport.copyFileToStream(file, outputStream, false);
    }
    endWebResponse(outputStream, !isDirectoryListing);
  }

  /**
   * List all files in a directory.
   *
   * @param completeUrlPath
   *          the URL path to the directory
   * @param directory
   *          the directory to get the contents from
   * @param outputStream
   *          the HTTP response output stream
   *
   * @throws IOException
   *           an issue happened while writing the response
   */
  protected void listDirectoryFiles(String completeUrlPath, File directory, OutputStream outputStream)
      throws IOException {
    File[] files = fileSupport.listFiles(directory);
    if (files == null) {
      files = new File[0];
    }
    Arrays.sort(files);
    for (File file : files) {
      String name = fileSupport.getName(file);
      writeDirectoryEntry(outputStream, completeUrlPath + HttpConstants.URL_PATH_COMPONENT_SEPARATOR + name,
          (file.isDirectory()) ? name + File.separator : name);
    }
  }

  /**
   * Write a directory entry into the output stream.
   *
   * @param outputStream
   *          the HTTP response output stream
   * @param url
   *          the URL for the link
   * @param displayName
   *          the display name for the link
   *
   * @throws IOException
   *           an issue happened while writing the response
   */
  private void writeDirectoryEntry(OutputStream outputStream, String url, String displayName) throws IOException {
    String line = String.format("<a class='directory-entry' href='%s'>%s</a><br>", url, displayName);
    outputStream.write(line.getBytes());
  }

  /**
   * Add in a link to a builder.
   *
   * @param builder
   *          the builder for the HTML fragment
   * @param url
   *          the URL for the link
   * @param text
   *          the text for the link
   */
  protected void addLink(StringBuilder builder, String url, String text) {
    builder.append("<a href='").append(url).append("'>").append(text).append("</a>");
  }

  /**
   * Add in an entry for a specific section of a live activity.
   *
   * @param builder
   *          the response builder
   * @param baseUrl
   *          the base URL for the activity
   * @param section
   *          the section to give
   */
  protected void addFilesystemDirectoryEntry(StringBuilder builder, String baseUrl, String section) {
    builder.append("<li>");
    addLink(builder, baseUrl + section, section);
    builder.append("</li>");
  }

  /**
   * Display a section of a file system.
   *
   * @param response
   *          the HTTP response
   * @param baseUrl
   *          the base URL for all requests
   * @param section
   *          the section of the file system
   * @param requestPathComponents
   *          the components of the request path
   * @param pathBegin
   *          the location of the path relative to the file section in the request path components
   * @param rootSectionDirectory
   *          the root directory for the section
   *
   * @throws Throwable
   *           an exception happened while processing
   */
  protected void displayFilesystemSection(HttpResponse response, String baseUrl, String section,
      String[] requestPathComponents, int pathBegin, File rootSectionDirectory) throws Throwable {
    int length = requestPathComponents.length;
    List<String> pathComponents = Lists.newArrayList();
    for (int i = pathBegin; i < length; i++) {
      pathComponents.add(requestPathComponents[i]);
    }

    displayFileContents(response, baseUrl + HttpConstants.URL_PATH_COMPONENT_SEPARATOR + section,
        rootSectionDirectory, pathComponents);
  }
}
