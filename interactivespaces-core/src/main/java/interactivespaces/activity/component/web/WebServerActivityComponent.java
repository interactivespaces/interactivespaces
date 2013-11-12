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

package interactivespaces.activity.component.web;

import com.google.common.collect.Lists;

import interactivespaces.activity.Activity;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.web.WebSocketConnection;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerService;
import interactivespaces.service.web.server.WebServerWebSocketHandler;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import interactivespaces.system.InteractiveSpacesEnvironment;
import org.apache.commons.logging.Log;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link ActivityComponent} which starts up a web server.
 *
 * @author Keith M. Hughes
 */
public class WebServerActivityComponent extends BaseActivityComponent {

  /**
   * Name of the component.
   */
  public static final String COMPONENT_NAME = "web.server";

  /**
   * Configuration property giving the port the web server should be started on.
   */
  public static final String CONFIGURATION_WEBAPP_WEB_SERVER_PORT =
      "space.activity.webapp.web.server.port";

  /**
   * Configuration property giving the websocket URI for the web server on.
   */
  public static final String CONFIGURATION_WEBAPP_WEB_SERVER_WEBSOCKET_URI =
      "space.activity.webapp.web.server.websocket.uri";

  /**
   * Configuration property giving location of the webapp content. Relative
   * paths give relative to app install directory.
   */
  public static final String CONFIGURATION_WEBAPP_CONTENT_LOCATION =
      "space.activity.webapp.content.location";

  /**
   * Default port to give to the web server.
   */
  public static final int WEB_SERVER_PORT_DEFAULT = 9000;

  /**
   * Host identifier to use if not specified in configuraiton.
   */
  public static final String WEB_SERVER_DEFAULT_HOST = "localhost";

  /**
   * Default page to use when none specified in configuration.
   */
  public static final String DEFAULT_INITIAL_PAGE = "index.html";

  /**
   * URL for the web activity.
   */
  private String webContentUrl;

  /**
   * URL for the initial content page for this server.
   */
  private String webInitialPage;

  /**
   * Port the web server will run on.
   */
  private int webServerPort;

  /**
   * Web server for the app, if needed.
   */
  private WebServer webServer;

  /**
   * The path to the web content. This is the absolute path portion of the URL.
   */
  private String webContentPath;

  /**
   * The base directory of the web content being served.
   */
  private File webContentBaseDir;

  /**
   * Factory for web socket handlers.
   */
  private WebServerWebSocketHandlerFactory webSocketHandlerFactory;

  /**
   * A potential listener for file uploads.
   */
  private HttpFileUploadListener httpFileUploadListener;

  /**
   * Prefix of the URI for the web socket connections.
   */
  private String webSocketUriPrefix;

  /**
   * List of static content for the web server.
   */
  private List<StaticContent> staticContent = Lists.newArrayList();

  /**
   * List of dynamic content for the web server.
   */
  private List<DynamicContent> dynamicContent = Lists.newArrayList();

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

    Activity activity = getComponentContext().getActivity();

    webSocketUriPrefix =
        configuration.getPropertyString(CONFIGURATION_WEBAPP_WEB_SERVER_WEBSOCKET_URI);

    webServerPort =
        configuration.getPropertyInteger(CONFIGURATION_WEBAPP_WEB_SERVER_PORT,
            WEB_SERVER_PORT_DEFAULT);
    WebServerService webServerService =
        activity.getSpaceEnvironment().getServiceRegistry()
            .getService(WebServerService.SERVICE_NAME);
    webServer =
        webServerService.newWebServer(String.format("%sWebServer", activity.getName()),
            webServerPort, activity.getLog());

    String webServerHost = configuration.getPropertyString(
        InteractiveSpacesEnvironment.CONFIGURATION_HOST_ADDRESS, WEB_SERVER_DEFAULT_HOST);

    webContentPath = "/" + activity.getName();
    webContentUrl = "http://" + webServerHost + ":" + webServer.getPort() + webContentPath;

    webInitialPage = webContentUrl + "/" + configuration.getPropertyString(
        WebBrowserActivityComponent.CONFIGURATION_INITIAL_PAGE, DEFAULT_INITIAL_PAGE);

    String contentLocation = configuration.getPropertyString(CONFIGURATION_WEBAPP_CONTENT_LOCATION);
    if (contentLocation != null) {
      webContentBaseDir =
          new File(activity.getActivityFilesystem().getInstallDirectory(), contentLocation);

      webServer.addStaticContentHandler(webContentPath, webContentBaseDir);
    }

    for (StaticContent content : staticContent) {
      webServer.addStaticContentHandler(content.getUriPrefix(), content.getBaseDir());
    }

    for (DynamicContent content : dynamicContent) {
      webServer.addDynamicContentHandler(content.getUriPrefix(), content.isUsePath(),
          content.getRequestHandler());
    }

    if (webSocketHandlerFactory != null) {
      setWebServerWebSocketHandlerFactory();
    }

    if (httpFileUploadListener != null) {
      webServer.setHttpFileUploadListener(httpFileUploadListener);
    }
  }

  @Override
  public void startupComponent() {
    webServer.startup();
    getLog().info("web server component started up");
  }

  @Override
  public void shutdownComponent() {
    long start = System.currentTimeMillis();
    getLog().info("Shutting down web server activity component");

    if (webServer != null) {
      webServer.shutdown();
      webServer = null;
    }

    if (getLog().isInfoEnabled()) {
      getLog().info(String.format("Web server activity component shut down in %s msecs",
          System.currentTimeMillis() - start));
    }
  }

  @Override
  public boolean isComponentRunning() {
    // TODO(keith): Anything to check on the web server?
    return true;
  }

  /**
   * @return the webContentUrl
   */
  public String getWebContentUrl() {
    return webContentUrl;
  }

  /**
   * @return the webServerPort
   */
  public int getWebServerPort() {
    return webServerPort;
  }

  /**
   * @return the webServer
   */
  public WebServer getWebServer() {
    return webServer;
  }

  /**
   * @return the webContentPath
   */
  public String getWebContentPath() {
    return webContentPath;
  }

  /**
   * @return the webContentBaseDir
   */
  public File getWebContentBaseDir() {
    return webContentBaseDir;
  }

  /**
   * Set the web socket handler factory for the web server to use.
   *
   * <p>
   * This can be called either before or after calling
   * {@link WebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param webSocketUriPrefix
   *          the prefix for web socket connections (can be {@code null})
   *
   * @return the web server component this method was called on
   */
  public WebServerActivityComponent setWebSocketUriPrefix(String webSocketUriPrefix) {
    this.webSocketUriPrefix = webSocketUriPrefix;

    return this;
  }

  /**
   * Set the web socket handler factory for the web server to use.
   *
   * <p>
   * This can be called either before or after calling
   * {@link WebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param webSocketHandlerFactory
   *          the webSocketHandlerFactory to set
   *
   * @return the web server component this method was called on
   */
  public WebServerActivityComponent setWebSocketHandlerFactory(
      WebServerWebSocketHandlerFactory webSocketHandlerFactory) {
    this.webSocketHandlerFactory = webSocketHandlerFactory;

    if (webServer != null) {
      setWebServerWebSocketHandlerFactory();
    }

    return this;
  }

  /**
   * Set the HTTP file upload listener for the web server to use.
   *
   * <p>
   * This can be called either before or after calling
   * {@link WebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param httpFileUploadListener
   *          the HTTP file upload listener to use (can be {@code null})
   *
   * @return the web server component this method was called on
   */
  public WebServerActivityComponent setHttpFileUploadListener(
      HttpFileUploadListener httpFileUploadListener) {
    this.httpFileUploadListener = httpFileUploadListener;

    if (webServer != null) {
      webServer.setHttpFileUploadListener(httpFileUploadListener);
    }

    return this;
  }

  /**
   * Add static content for the web server to serve.
   *
   * <p>
   * This can be called either before or after calling
   * {@link WebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param baseDir
   *          the base directory where the content will be found
   *
   * @return the web server component this method was called on
   */
  public WebServerActivityComponent addStaticContent(String uriPrefix, File baseDir) {
    if (webServer != null) {
      webServer.addStaticContentHandler(uriPrefix, baseDir);
    } else {
      staticContent.add(new StaticContent(uriPrefix, baseDir));
    }

    return this;
  }

  /**
   * Add dynamic content for the web server to serve.
   *
   * <p>
   * This can be called either before or after calling
   * {@link WebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param usePath
   *          {@code true} if the path will be used as part of request
   *          processing
   * @param handler
   *          content handler being added
   *
   * @return the web server component this method was called on
   */
  public WebServerActivityComponent addDynamicContent(String uriPrefix, boolean usePath,
      HttpDynamicRequestHandler handler) {
    if (webServer != null) {
      webServer.addDynamicContentHandler(uriPrefix, usePath, handler);
    } else {
      dynamicContent.add(new DynamicContent(handler, uriPrefix, usePath));
    }

    return this;
  }

  /**
   * Set the web server web socket handler with the proper wrapped factory.
   */
  private void setWebServerWebSocketHandlerFactory() {
    webServer.setWebSocketHandlerFactory(webSocketUriPrefix,
        new MyWebServerWebSocketHandlerFactory(webSocketHandlerFactory, getComponentContext()));
  }

  /**
   * Information about static content.
   *
   * @author Keith M. Hughes
   */
  public static class StaticContent {

    /**
     * URI prefix where the content will be referenced from.
     */
    private String uriPrefix;

    /**
     * Base directory where the content is stored.
     */
    private File baseDir;

    /**
     * Create a basic static content object.
     *
     * @param uriPrefix
     *            content prefix
     * @param baseDir
     *            directory path for content
     */
    public StaticContent(String uriPrefix, File baseDir) {
      this.uriPrefix = uriPrefix;
      this.baseDir = baseDir;
    }

    /**
     * @return the uriPrefix
     */
    public String getUriPrefix() {
      return uriPrefix;
    }

    /**
     * @return the baseDir
     */
    public File getBaseDir() {
      return baseDir;
    }
  }

  /**
   * Information about dynamic content.
   *
   * @author Keith M. Hughes
   */
  public static class DynamicContent {

    /**
     * URI prefix where the content will be referenced from.
     */
    private String uriPrefix;

    /**
     * {@code true} if the path will be used for processing.
     */
    private boolean usePath;

    /**
     * The request handler being added.
     */
    private HttpDynamicRequestHandler requestHandler;

    /**
     * Create a dynamic content object.
     *
     * @param requestHandler
     *            dynamic request handler
     * @param uriPrefix
     *            uri prefix that is handled
     * @param usePath
     *            path for handling the content
     */
    public DynamicContent(HttpDynamicRequestHandler requestHandler, String uriPrefix,
        boolean usePath) {
      this.requestHandler = requestHandler;
      this.uriPrefix = uriPrefix;
      this.usePath = usePath;
    }

    /**
     * @return the uriPrefix
     */
    public String getUriPrefix() {
      return uriPrefix;
    }

    /**
     * @return the usePath
     */
    public boolean isUsePath() {
      return usePath;
    }

    /**
     * @return the requestHandler
     */
    public HttpDynamicRequestHandler getRequestHandler() {
      return requestHandler;
    }
  }

  /**
   * A {@link WebServerWebSocketHandlerFactory} which delegates to another web
   * socket handler factory and wraps web socket handler with
   * {@link MyWebServerWebSocketHandler}.
   *
   * @author Keith M. Hughes
   */
  public static class MyWebServerWebSocketHandlerFactory implements
      WebServerWebSocketHandlerFactory {
    /**
     * The factory being delegated to.
     */
    private WebServerWebSocketHandlerFactory delegate;

    /**
     * The component context this factory is part of.
     */
    private ActivityComponentContext activityComponentContext;

    /**
     * Create a simple web socket handler factory.
     *
     * @param delegate
     *            creator factory delegate
     * @param activityComponentContext
     *            context for created socket handlers
     */
    public MyWebServerWebSocketHandlerFactory(WebServerWebSocketHandlerFactory delegate,
        ActivityComponentContext activityComponentContext) {
      this.delegate = delegate;
      this.activityComponentContext = activityComponentContext;
    }

    @Override
    public WebServerWebSocketHandler newWebSocketHandler(WebSocketConnection proxy) {
      WebServerWebSocketHandler handlerDelegate = delegate.newWebSocketHandler(proxy);
      return new MyWebServerWebSocketHandler(handlerDelegate, activityComponentContext);
    }
  }

  /**
   * A {@link WebSocketHandler} which delegates to a web socket handler but
   * ensures that the component is running.
   *
   * @author Keith M. Hughes
   */
  public static class MyWebServerWebSocketHandler implements WebServerWebSocketHandler {

    /**
     * The delegate to be protected.
     */
    private WebServerWebSocketHandler delegate;

    /**
     * The component context this handler is part of.
     */
    private ActivityComponentContext activityComponentContext;

    /**
     * Is the handler connected to the remote endpoint?
     */
    private AtomicBoolean connected = new AtomicBoolean(false);

    /**
     *
     * @param delegate
     *          the handler that all methods will be delegated to
     * @param activityComponentContext
     *          the context in charge of the component
     */
    public MyWebServerWebSocketHandler(WebServerWebSocketHandler delegate,
        ActivityComponentContext activityComponentContext) {
      this.delegate = delegate;
      this.activityComponentContext = activityComponentContext;
    }

    @Override
    public void onConnect() {
      if (!activityComponentContext.canHandlerRun()) {
        return;
      }

      try {
        activityComponentContext.enterHandler();

        delegate.onConnect();
      } catch (Throwable e) {
        activityComponentContext.getActivity().getLog()
            .error("Error during web socket connection", e);
      } finally {
        connected.set(true);

        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void onClose() {
      if (!activityComponentContext.canHandlerRun()) {
        return;
      }

      try {
        activityComponentContext.enterHandler();

        delegate.onClose();
      } catch (Throwable e) {
        activityComponentContext.getActivity().getLog().error("Error during web socket close", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void onReceive(final Object data) {
      if (!activityComponentContext.canHandlerRun()) {
        return;
      }

      try {
        activityComponentContext.enterHandler();

        delegate.onReceive(data);
      } catch (Throwable e) {
        activityComponentContext.getActivity().getLog()
            .error("Error during web socket data receive", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void sendJson(final Object data) {
      try {
        activityComponentContext.enterHandler();

        delegate.sendJson(data);
      } catch (Throwable e) {
        activityComponentContext.getActivity().getLog()
            .error("Error during web socket JSON sending", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void sendString(final String data) {
      try {
        activityComponentContext.enterHandler();

        delegate.sendString(data);
      } catch (Throwable e) {
        activityComponentContext.getActivity().getLog()
            .error("Error during web socket string sending", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }
  }

  @Override
  public String getComponentStatusDetail() {
    return webInitialPage;
  }

  /**
   * Get the logger for this component.
   *
   * @return logger
   */
  private Log getLog() {
    return getComponentContext().getActivity().getLog();
  }

}
