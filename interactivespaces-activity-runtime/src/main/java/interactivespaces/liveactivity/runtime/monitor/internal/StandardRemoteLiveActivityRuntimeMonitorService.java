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
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Simple activity to debug controllers remotely.
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

  @Override
  public void startup() {
    spaceEnvironment = liveActivityRuntime.getSpaceEnvironment();

    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    boolean enabled = systemConfiguration
        .getPropertyBoolean(CONFIGURE_MONITOR_ENABLE, CONFIGURE_MONITOR_ENABLE_DEFAULT);

    if (!enabled) {
      spaceEnvironment.getLog().warn("Live activity runtime monitor server disabled");
      return;
    }

    webServer = new NettyWebServer(spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());
    webServer.setServerName(WEB_SERVER_NAME);
    webServer.setPort(systemConfiguration.getPropertyInteger(CONFIGURATION_WEBSERVER_PORT,
        CONFIGURATION_VALUE_DEFAULT_WEBSERVER_PORT));

    webServer.startup();

    spaceEnvironment.getLog().info("Live activity runtime monitor server running on port " + webServer.getPort());

    addBasePlugins();
  }

  @Override
  public void shutdown() {
    if (webServer == null) {
      return;
    }

    for (LiveActivityRuntimeMonitorPlugin plugin : plugins) {
      try {
        plugin.shutdown();
      } catch (Throwable e) {
        liveActivityRuntime.getSpaceEnvironment().getLog()
            .error(String.format("Could not shut down live activity monitor plugin %s", plugin.getUrlPrefix()), e);
      }
    }
    webServer.shutdown();
  }

  @Override
  public void addPlugin(LiveActivityRuntimeMonitorPlugin plugin) {
    if (webServer == null) {
      return;
    }

    try {
      plugin.startup(this, webServer);
      plugins.add(plugin);
    } catch (Throwable e) {
      liveActivityRuntime.getSpaceEnvironment().getLog()
          .error(String.format("Could not start up live activity monitor plugin %s", plugin.getUrlPrefix()), e);
    }
  }

  @Override
  public void setLiveActivityRuntime(LiveActivityRuntime liveActivityRuntime) {
    this.liveActivityRuntime = liveActivityRuntime;
  }

  @Override
  public LiveActivityRuntime getLiveActivityRuntime() {
    return liveActivityRuntime;
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
