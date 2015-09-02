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
import interactivespaces.liveactivity.runtime.monitor.LiveActivityRuntimeMonitorPlugin;
import interactivespaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import interactivespaces.logging.ExtendedLog;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.net.NetworkBindSimpleInteractiveSpacesException;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The standard remote live activity runtime monitoring system.
 *
 * @author Keith M. Hughes
 * @author Trevor Pering
 */
public class StandardRemoteLiveActivityRuntimeMonitorService implements RemoteLiveActivityRuntimeMonitorService {

  /**
   * The name of the web server for the monitoring system.
   */
  private static final String WEB_SERVER_NAME = "liveActivityRuntimeInfo";

  /**
   * The live activity runtime being monitored.
   */
  private LiveActivityRuntime liveActivityRuntime;

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The web server for the monitor.
   */
  private WebServer webServer;

  /**
   * The collection of installed plugins.
   */
  private List<LiveActivityRuntimeMonitorPlugin> plugins = Lists.newCopyOnWriteArrayList();

  /**
   * The index plugin.
   */
  private IndexLiveActivityRuntimeMonitorPlugin indexPlugin = new IndexLiveActivityRuntimeMonitorPlugin();

  @Override
  public void startup() {
    spaceEnvironment = liveActivityRuntime.getSpaceEnvironment();

    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    boolean enabledDefault =
        systemConfiguration.getPropertyBoolean(CONFIGURATION_NAME_MONITOR_ENABLE_DEFAULT,
            CONFIGURATION_VALUE_MONITOR_ENABLE_DEFAULT);

    boolean enabled = systemConfiguration.getPropertyBoolean(CONFIGURATION_NAME_MONITOR_ENABLE, enabledDefault);
    if (!enabled) {
      spaceEnvironment.getExtendedLog().warn("Live activity runtime monitor server disabled");
      return;
    }

    try {
      webServer = new NettyWebServer(spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());
      webServer.setServerName(WEB_SERVER_NAME);
      webServer.setPort(systemConfiguration.getPropertyInteger(CONFIGURATION_NAME_WEBSERVER_PORT,
          CONFIGURATION_VALUE_DEFAULT_WEBSERVER_PORT));

      webServer.startup();

      spaceEnvironment.getExtendedLog().info(
          "Live activity runtime monitor server running on port " + webServer.getPort());

      addBasePlugins();
      addPlugin(indexPlugin, false);
    } catch (NetworkBindSimpleInteractiveSpacesException e) {
      handleWebServerStartupError(e, String.format(
          "Remote live activity runtime monitor disabled due to port confict."
              + "The port can be changed using configuration parameter %s", CONFIGURATION_NAME_WEBSERVER_PORT));
    } catch (Throwable e) {
      handleWebServerStartupError(e, "Remote live activity runtime monitor disabled due to error");
    }
  }

  /**
   * Handle a web server startup error.
   *
   * @param e
   *          the exception from the startup error
   * @param message
   *          the message to print
   */
  private void handleWebServerStartupError(Throwable e, String message) {
    spaceEnvironment.getExtendedLog().formatError(e, message);
    webServer = null;
  }

  @Override
  public void shutdown() {
    if (webServer == null) {
      return;
    }

    shutdownPlugin(indexPlugin);

    for (LiveActivityRuntimeMonitorPlugin plugin : plugins) {
      shutdownPlugin(plugin);
    }

    webServer.shutdown();
    webServer = null;
  }

  /**
   * Shut down a plugin.
   *
   * @param plugin
   *          the plugin to shut down
   */
  private void shutdownPlugin(LiveActivityRuntimeMonitorPlugin plugin) {
    try {
      plugin.shutdown();
    } catch (Throwable e) {
      liveActivityRuntime.getSpaceEnvironment().getLog()
          .error(String.format("Could not shut down live activity monitor plugin %s", plugin.getUrlPrefix()), e);
    }
  }

  @Override
  public void addPlugin(LiveActivityRuntimeMonitorPlugin plugin) {
    addPlugin(plugin, true);
  }

  /**
   * Add a plugin to the monitor.
   *
   * @param plugin
   *          the plugin to add
   * @param addToPlugins
   *          {@code true} if should be added to the list of plugins
   */
  private void addPlugin(LiveActivityRuntimeMonitorPlugin plugin, boolean addToPlugins) {
    if (webServer == null) {
      return;
    }

    try {
      plugin.startup(this, webServer);

      if (addToPlugins) {
        plugins.add(plugin);
      }
    } catch (Throwable e) {
      liveActivityRuntime.getSpaceEnvironment().getExtendedLog()
          .formatError(e, "Could not start up live activity monitor plugin %s", plugin.getUrlPrefix());
    }
  }

  @Override
  public List<LiveActivityRuntimeMonitorPlugin> getPlugins() {
    return Lists.newArrayList(plugins);
  }

  @Override
  public void setLiveActivityRuntime(LiveActivityRuntime liveActivityRuntime) {
    this.liveActivityRuntime = liveActivityRuntime;
  }

  @Override
  public LiveActivityRuntime getLiveActivityRuntime() {
    return liveActivityRuntime;
  }

  @Override
  public ExtendedLog getLog() {
    return liveActivityRuntime.getSpaceEnvironment().getExtendedLog();
  }

  /**
   * Add in all plugins that are part of a base monitoring service.
   */
  private void addBasePlugins() {
    addPlugin(new ScreenshotLiveActivityRuntimeMonitorPlugin());
    addPlugin(new RuntimeLiveActivityRuntimeMonitorPlugin());
    addPlugin(new LiveActivityLiveActivityRuntimeMonitorPlugin());
  }
}
