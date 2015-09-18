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

import interactivespaces.activity.Activity;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.activity.configuration.WebServerActivityResourceConfigurator;
import interactivespaces.activity.impl.StatusDetail;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.service.web.WebSocketConnection;
import interactivespaces.service.web.server.HttpDynamicPostRequestHandler;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerService;
import interactivespaces.service.web.server.WebServerWebSocketHandler;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link ActivityComponent} which starts up a web server.
 *
 * @author Keith M. Hughes
 */
public class BasicWebServerActivityComponent extends BaseActivityComponent implements WebServerActivityComponent {

  /**
   * Web server for the app, if needed.
   */
  private WebServer webServer;

  /**
   * Factory for web socket handlers.
   */
  private WebServerWebSocketHandlerFactory webSocketHandlerFactory;

  /**
   * A potential listener for file uploads.
   */
  private HttpFileUploadListener httpFileUploadListener;

  /**
   * List of static content for the web server.
   */
  private final List<StaticContent> staticContent = Lists.newArrayList();

  /**
   * List of dynamic GET content for the web server.
   */
  private final List<DynamicContent<HttpDynamicRequestHandler>> dynamicGetContent = Lists.newArrayList();

  /**
   * List of dynamic POST content for the web server.
   */
  private final List<DynamicContent<HttpDynamicPostRequestHandler>> dynamicPostContent = Lists.newArrayList();

  /**
   * Configurator for this component.
   */
  private WebServerActivityResourceConfigurator configurator = new WebServerActivityResourceConfigurator();

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public String getDescription() {
    return COMPONENT_DESCRIPTION;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

    Activity activity = getComponentContext().getActivity();

    WebServerService webServerService =
        activity.getSpaceEnvironment().getServiceRegistry().getService(WebServerService.SERVICE_NAME);
    webServer = webServerService.newWebServer(activity.getLog());

    configurator.configure(null, activity, webServer);

    for (StaticContent content : staticContent) {
      addStaticContentHandler(content.getUriPrefix(), content.getBaseDir());
    }

    for (DynamicContent<HttpDynamicRequestHandler> content : dynamicGetContent) {
      webServer.addDynamicContentHandler(content.getUriPrefix(), content.isUsePath(), content.getRequestHandler());
    }

    for (DynamicContent<HttpDynamicPostRequestHandler> content : dynamicPostContent) {
      webServer.addDynamicPostRequestHandler(content.getUriPrefix(), content.isUsePath(), content.getRequestHandler());
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
      getLog().info(
          String.format("Web server activity component shut down in %s msecs", System.currentTimeMillis() - start));
    }
  }

  @Override
  public boolean isComponentRunning() {
    // TODO(keith): Anything to check on the web server?
    return true;
  }

  @Override
  public String getWebBaseUrl() {
    return configurator.getWebBaseUrl();
  }

  @Override
  public String getWebContentUrl() {
    return configurator.getWebContentUrl();
  }

  @Override
  public int getWebServerPort() {
    return configurator.getWebServerPort();
  }

  @Override
  public WebServer getWebServer() {
    return webServer;
  }

  @Override
  public String getWebContentPath() {
    return configurator.getWebContentPath();
  }

  @Override
  public File getWebContentBaseDir() {
    return configurator.getWebContentBaseDir();
  }

  @Override
  public WebServerActivityComponent setWebSocketUriPrefix(String webSocketUriPrefix) {
    configurator.setWebSocketUriPrefix(webSocketUriPrefix);

    return this;
  }

  @Override
  public WebServerActivityComponent setWebSocketHandlerFactory(
      WebServerWebSocketHandlerFactory webSocketHandlerFactory) {
    this.webSocketHandlerFactory = webSocketHandlerFactory;

    if (webServer != null) {
      setWebServerWebSocketHandlerFactory();
    }

    return this;
  }

  @Override
  public WebServerActivityComponent setHttpFileUploadListener(HttpFileUploadListener httpFileUploadListener) {
    this.httpFileUploadListener = httpFileUploadListener;

    if (webServer != null) {
      webServer.setHttpFileUploadListener(httpFileUploadListener);
    }

    return this;
  }

  @Override
  public WebServerActivityComponent addStaticContent(String uriPrefix, File baseDir) {
    if (webServer != null) {
      addStaticContentHandler(uriPrefix, baseDir);
    } else {
      staticContent.add(new StaticContent(uriPrefix, baseDir));
    }

    return this;
  }

  /**
   * Add a static content handler that has the access control allow origin header set.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param baseDir
   *          the base directory where the content will be found
   */
  private void addStaticContentHandler(String uriPrefix, File baseDir) {
    Map<String, String> accessControlMap = Collections.singletonMap(HttpConstants.ACCESS_CONTROL_ALLOW_ORIGIN,
        HttpConstants.ACCESS_CONTROL_ORIGIN_WILDCARD);
    Map<String, String> header = configurator.getCrossOriginAllowed()
        ? accessControlMap : HttpConstants.EMPTY_HEADER_MAP;
    webServer.addStaticContentHandler(uriPrefix, baseDir, header);
  }

  @Override
  public WebServerActivityComponent addDynamicContent(String uriPrefix, boolean usePath,
      HttpDynamicRequestHandler handler) {
    if (webServer != null) {
      webServer.addDynamicContentHandler(uriPrefix, usePath, handler);
    } else {
      dynamicGetContent.add(new DynamicContent<HttpDynamicRequestHandler>(handler, uriPrefix, usePath));
    }

    return this;
  }

  @Override
  public WebServerActivityComponent addDynamicPostRequestHandler(String uriPrefix, boolean usePath,
      HttpDynamicPostRequestHandler handler) {
    if (webServer != null) {
      webServer.addDynamicPostRequestHandler(uriPrefix, usePath, handler);
    } else {
      dynamicPostContent.add(new DynamicContent<HttpDynamicPostRequestHandler>(handler, uriPrefix, usePath));
    }

    return this;
  }

  /**
   * Set the web server web socket handler with the proper wrapped factory.
   */
  private void setWebServerWebSocketHandlerFactory() {
    webServer.setWebSocketHandlerFactory(configurator.getWebSocketUriPrefix(), new MyWebServerWebSocketHandlerFactory(
        webSocketHandlerFactory, this));
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
    private final String uriPrefix;

    /**
     * Base directory where the content is stored.
     */
    private final File baseDir;

    /**
     * Create a basic static content object.
     *
     * @param uriPrefix
     *          content prefix
     * @param baseDir
     *          the base directory for content
     */
    public StaticContent(String uriPrefix, File baseDir) {
      this.uriPrefix = uriPrefix;
      this.baseDir = baseDir;
    }

    /**
     * Get the URI prefix.
     *
     * @return the uriPrefix
     */
    public String getUriPrefix() {
      return uriPrefix;
    }

    /**
     * Get the base directory for content.
     *
     * @return the base directory
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
  public static class DynamicContent<T> {

    /**
     * The request handler.
     */
    private final T requestHandler;

    /**
     * URI prefix where the content will be referenced from.
     */
    private final String uriPrefix;

    /**
     * {@code true} if the path will be used for processing.
     */
    private final boolean usePath;

    /**
     * Create a dynamic content object.
     *
     * @param requestHandler
     *          dynamic request handler
     * @param uriPrefix
     *          uri prefix that is handled
     * @param usePath
     *          path for handling the content
     */
    public DynamicContent(T requestHandler, String uriPrefix, boolean usePath) {
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
     * Get the request handler.
     *
     * @return the request andler
     */
    public T getRequestHandler() {
      return requestHandler;
    }
  }

  /**
   * A {@link WebServerWebSocketHandlerFactory} which delegates to another web socket handler factory and wraps web
   * socket handler with {@link MyWebServerWebSocketHandler}.
   *
   * @author Keith M. Hughes
   */
  public static class MyWebServerWebSocketHandlerFactory implements WebServerWebSocketHandlerFactory {
    /**
     * The factory being delegated to.
     */
    private final WebServerWebSocketHandlerFactory delegate;

    /**
     * The component context this factory is part of.
     */
    private final BasicWebServerActivityComponent activityComponent;

    /**
     * Create a simple web socket handler factory.
     *
     * @param delegate
     *          creator factory delegate
     * @param activityComponent
     *          hosting component
     */
    public MyWebServerWebSocketHandlerFactory(WebServerWebSocketHandlerFactory delegate,
        BasicWebServerActivityComponent activityComponent) {
      this.delegate = delegate;
      this.activityComponent = activityComponent;
    }

    @Override
    public WebServerWebSocketHandler newWebSocketHandler(WebSocketConnection proxy) {
      WebServerWebSocketHandler handlerDelegate = delegate.newWebSocketHandler(proxy);
      return new MyWebServerWebSocketHandler(handlerDelegate, activityComponent);
    }
  }

  @Override
  public String getComponentStatusDetail() {
    return String.format(StatusDetail.LINK_FORMAT, configurator.getWebInitialPage());
  }

  /**
   * Get the logger for this component.
   *
   * @return the logger
   */
  private Log getLog() {
    return getComponentContext().getActivity().getLog();
  }

  /**
   * A {@link WebSocketHandler} which delegates to a web socket handler but ensures that the component is running.
   *
   * @author Keith M. Hughes
   */
  public static class MyWebServerWebSocketHandler implements WebServerWebSocketHandler {

    /**
     * The delegate to be protected.
     */
    private final WebServerWebSocketHandler delegate;

    /**
     * The component this handler is for.
     */
    private final BasicWebServerActivityComponent activityComponent;

    /**
     * Is the handler connected to the remote endpoint?
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * Construct a handler.
     *
     * @param delegate
     *          the handler that all methods will be delegated to
     * @param activityComponent
     *          the component being handled
     */
    public MyWebServerWebSocketHandler(WebServerWebSocketHandler delegate,
        BasicWebServerActivityComponent activityComponent) {
      this.delegate = delegate;
      this.activityComponent = activityComponent;
    }

    @Override
    public void onConnect() {
      ActivityComponentContext activityComponentContext = activityComponent.getComponentContext();
      if (!activityComponentContext.canHandlerRun()) {
        return;
      }

      try {
        activityComponentContext.enterHandler();

        delegate.onConnect();
      } catch (Throwable e) {
        activityComponent.handleError("Error during web socket connection", e);
      } finally {
        connected.set(true);

        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void onClose() {
      ActivityComponentContext activityComponentContext = activityComponent.getComponentContext();
      if (!activityComponentContext.canHandlerRun()) {
        return;
      }

      try {
        activityComponentContext.enterHandler();

        delegate.onClose();
      } catch (Throwable e) {
        activityComponent.handleError("Error during web socket close", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void onReceive(final Object data) {
      ActivityComponentContext activityComponentContext = activityComponent.getComponentContext();
      if (!activityComponentContext.canHandlerRun()) {
        return;
      }

      try {
        activityComponentContext.enterHandler();

        delegate.onReceive(data);
      } catch (Throwable e) {
        activityComponent.handleError("Error during web socket data receive", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void sendJson(final Object data) {
      ActivityComponentContext activityComponentContext = activityComponent.getComponentContext();
      try {
        activityComponentContext.enterHandler();

        delegate.sendJson(data);
      } catch (Throwable e) {
        activityComponent.handleError("Error during web socket JSON sending", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }

    @Override
    public void sendString(final String data) {
      ActivityComponentContext activityComponentContext = activityComponent.getComponentContext();
      try {
        activityComponentContext.enterHandler();

        delegate.sendString(data);
      } catch (Throwable e) {
        activityComponent.handleError("Error during web socket string sending", e);
      } finally {
        activityComponentContext.exitHandler();
      }
    }
  }
}
