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
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.web.WebSocketConnection;
import interactivespaces.service.web.WebSocketHandler;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerService;
import interactivespaces.service.web.server.WebServerWebSocketHandler;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

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
	 * Configuration property giving the port the web server should be started
	 * on.
	 */
	public static final String CONFIGURATION_WEBAPP_WEB_SERVER_PORT = "space.activity.webapp.web.server.port";

	/**
	 * Configuration property giving the websocket URI for the web server on.
	 */
	public static final String CONFIGURATION_WEBAPP_WEB_SERVER_WEBSOCKET_URI = "space.activity.webapp.web.server.websocket.uri";

	/**
	 * Configuration property giving location of the webapp content. Relative
	 * paths give relative to app install directory.
	 */
	public static final String CONFIGURATION_WEBAPP_CONTENT_LOCATION = "space.activity.webapp.content.location";

	/**
	 * Default port to give to the web server.
	 */
	public static final int WEB_SERVER_PORT_DEFAULT = 9000;

	/**
	 * URL for the web activity.
	 */
	private String webContentUrl;

	/**
	 * Port the web server will run on.
	 */
	private int webServerPort;

	/**
	 * Web server for the app, if needed.
	 */
	private WebServer webServer;

	/**
	 * The path to the web content. This is the absolute path portion of the
	 * URL.
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

	@Override
	public String getName() {
		return COMPONENT_NAME;
	}

	@Override
	public void configureComponent(Configuration configuration,
			ActivityComponentContext componentContext) {
		super.configureComponent(configuration, componentContext);

		Activity activity = getComponentContext().getActivity();

		webSocketUriPrefix = configuration
				.getPropertyString(CONFIGURATION_WEBAPP_WEB_SERVER_WEBSOCKET_URI);

		webServerPort = configuration.getPropertyInteger(
				CONFIGURATION_WEBAPP_WEB_SERVER_PORT, WEB_SERVER_PORT_DEFAULT);
		WebServerService webServerService = activity.getSpaceEnvironment()
				.getServiceRegistry().getService(WebServerService.SERVICE_NAME);
		webServer = webServerService.newWebServer(
				String.format("%sWebServer", activity.getName()),
				webServerPort, activity.getLog());

		webContentPath = "/" + activity.getName();
		webContentUrl = "http://localhost:" + webServer.getPort()
				+ webContentPath;

		String contentLocation = configuration
				.getPropertyString(CONFIGURATION_WEBAPP_CONTENT_LOCATION);
		if (contentLocation != null) {
			webContentBaseDir = new File(activity.getActivityFilesystem()
					.getInstallDirectory(), contentLocation);

			webServer
					.addStaticContentHandler(webContentPath, webContentBaseDir);
		}

		for (StaticContent content : staticContent) {
			webServer.addStaticContentHandler(content.getUriPrefix(),
					content.getBaseDir());
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
		getComponentContext().getActivity().getLog()
				.debug("web server component started up");
	}

	@Override
	public void shutdownComponent() {
		if (webServer != null) {
			webServer.shutdown();
			webServer = null;
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
	 *            the prefix for web socket connections (can be {@code null})
	 * 
	 * @return the web server component this method was called on
	 */
	public WebServerActivityComponent setWebSocketUriPrefix(
			String webSocketUriPrefix) {
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
	 *            the webSocketHandlerFactory to set
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
	 *            the HTTP file upload listener to use (can be {@code null})
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
	 *            the URI prefix for this particular content
	 * @param baseDir
	 *            the base directory where the content will be found
	 * 
	 * @return the web server component this method was called on
	 */
	public WebServerActivityComponent addStaticContent(String uriPrefix,
			File baseDir) {
		if (webServer != null) {
			webServer.addStaticContentHandler(uriPrefix, baseDir);
		} else {
			staticContent.add(new StaticContent(uriPrefix, baseDir));
		}

		return this;
	}

	/**
	 * Set the web server web socket handler with the proper wrapped factory.
	 */
	private void setWebServerWebSocketHandlerFactory() {
		webServer.setWebSocketHandlerFactory(webSocketUriPrefix,
				new MyWebServerWebSocketHandlerFactory(webSocketHandlerFactory,
						getComponentContext()));
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

		public MyWebServerWebSocketHandlerFactory(
				WebServerWebSocketHandlerFactory delegate,
				ActivityComponentContext activityComponentContext) {
			this.delegate = delegate;
			this.activityComponentContext = activityComponentContext;
		}

		@Override
		public WebServerWebSocketHandler newWebSocketHandler(
				WebSocketConnection proxy) {
			WebServerWebSocketHandler handlerDelegate = delegate
					.newWebSocketHandler(proxy);
			return new MyWebServerWebSocketHandler(handlerDelegate,
					activityComponentContext);
		}
	}

	/**
	 * A {@link WebSocketHandler} which delegates to a web socket handler but
	 * ensures that the component is running.
	 * 
	 * @author Keith M. Hughes
	 */
	public static class MyWebServerWebSocketHandler implements
			WebServerWebSocketHandler {

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
		 *            the handler that all methods will be delegated to
		 * @param activityComponentContext
		 *            the context in charge of the component
		 */
		public MyWebServerWebSocketHandler(WebServerWebSocketHandler delegate,
				ActivityComponentContext activityComponentContext) {
			this.delegate = delegate;
			this.activityComponentContext = activityComponentContext;
		}

		@Override
		public void onConnect() {
			activityComponentContext.addActivityEventQueueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						delegate.onConnect();
					} catch (Throwable e) {
						activityComponentContext.getActivity().getLog()
								.error("Error during web socket connection", e);
					} finally {
						connected.set(true);
					}
				}
			});
		}

		@Override
		public void onClose() {
			activityComponentContext.addActivityEventQueueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						delegate.onClose();
					} catch (Throwable e) {
						activityComponentContext.getActivity().getLog()
								.error("Error during web socket close", e);
					}
				}
			});
		}

		@Override
		public void onReceive(final Object data) {
			activityComponentContext.addActivityEventQueueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						delegate.onReceive(data);
					} catch (Throwable e) {
						activityComponentContext
								.getActivity()
								.getLog()
								.error("Error during web socket data receive",
										e);
					}
				}
			});
		}

		@Override
		public void sendJson(final Object data) {
			activityComponentContext.addActivityEventQueueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						delegate.sendJson(data);
					} catch (Throwable e) {
						activityComponentContext
								.getActivity()
								.getLog()
								.error("Error during web socket JSON sending",
										e);
					}
				}
			});
		}

		@Override
		public void sendString(final String data) {
			activityComponentContext.addActivityEventQueueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						delegate.sendString(data);
					} catch (Throwable e) {
						activityComponentContext
								.getActivity()
								.getLog()
								.error("Error during web socket string sending",
										e);
					}
				}
			});
		}
	}
}
