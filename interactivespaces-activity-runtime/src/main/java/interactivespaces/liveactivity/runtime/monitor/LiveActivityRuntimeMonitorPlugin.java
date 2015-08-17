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

package interactivespaces.liveactivity.runtime.monitor;

import interactivespaces.service.web.server.WebServer;

import java.util.List;

/**
 * A monitor for placing in the live activity runtime monitoring system.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityRuntimeMonitorPlugin {

  /**
   * Start up the plugin.
   *
   * @param monitorService
   *          the monitor service this plugin is part of
   * @param webServer
   *          the web server for the plugin
   */
  void startup(RemoteLiveActivityRuntimeMonitorService monitorService, WebServer webServer);

  /**
   * shut the plugin down.
   */
  void shutdown();

  /**
   * Get the URL prefix for the plugin.
   *
   * @return the URL prefix
   */
  String getUrlPrefix();

  /**
   * Get the functionality descriptors for the plugin.
   *
   * @return the functionality descriptors
   */
  List<PluginFunctionalityDescriptor> getFunctionalityDescriptors();
}
