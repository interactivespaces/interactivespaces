/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.configuration;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.service.web.server.WebServer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

/**
 * Tests for the {@link WebServerActivityResourceConfigurator}.
 *
 * @author Keith M. Hughes
 */
public class WebServerActivityResourceConfiguratorTest {
  private WebServerActivityResourceConfigurator configurator;
  private WebServer webServer;
  private Activity activity;
  private SimpleConfiguration configuration;
  private ActivityFilesystem filesystem;
  private File installDirectory = new File(".").getAbsoluteFile();
  private String activityName = "testActivity";

  @Before
  public void setup() {
    configurator = new WebServerActivityResourceConfigurator();
    webServer = Mockito.mock(WebServer.class);
    activity = Mockito.mock(Activity.class);

    Mockito.when(activity.getName()).thenReturn(activityName);

    filesystem = Mockito.mock(ActivityFilesystem.class);
    Mockito.when(activity.getActivityFilesystem()).thenReturn(filesystem);
    Mockito.when(filesystem.getInstallDirectory()).thenReturn(installDirectory);

    configuration = SimpleConfiguration.newConfiguration();
    Mockito.when(activity.getConfiguration()).thenReturn(configuration);
  }

  /**
   * Test a normal configure.
   */
  @Test
  public void testConfigure() {
    int testPort = WebServerActivityResourceConfigurator.WEB_SERVER_PORT_DEFAULT + 100;
    String webSocketUri = "foo/bar/bletch";
    String initialUrl = "snafu";
    String query = "oorgle";

    configuration.setValue("space.activity.webapp.web.server.port", Integer.toString(testPort));
    configuration.setValue("space.activity.webapp.content.location", "webapp");
    configuration.setValue("space.activity.webapp.web.server.websocket.uri", webSocketUri);
    configuration.setValue("space.activity.webapp.url.initial", initialUrl);
    configuration.setValue("space.activity.webapp.url.query_string", query);

    configurator.configure(null, activity, webServer);

    Mockito.verify(webServer).setPort(testPort);
    Mockito.verify(webServer).addStaticContentHandler("/" + activityName, new File(installDirectory, "webapp"));

    Assert.assertEquals(webSocketUri, configurator.getWebSocketUriPrefix());
    String webContentUrl = "http://localhost:" + testPort + "/" + activityName;
    Assert.assertEquals(webContentUrl, configurator.getWebContentUrl());
    Assert.assertEquals(webContentUrl + "/" + initialUrl + "?" + query, configurator.getWebInitialPage());
  }

  /**
   * Test an https configure.
   */
  @Test
  public void testHttpsConfigure() {
    int testPort = WebServerActivityResourceConfigurator.WEB_SERVER_PORT_DEFAULT + 100;
    String webSocketUri = "foo/bar/bletch";
    String initialUrl = "snafu";
    String query = "oorgle";

    configuration.setValue("space.activity.webapp.web.server.port", Integer.toString(testPort));
    configuration.setValue("space.activity.webapp.content.location", "webapp");
    configuration.setValue("space.activity.webapp.web.server.websocket.uri", webSocketUri);
    configuration.setValue("space.activity.webapp.url.initial", initialUrl);
    configuration.setValue("space.activity.webapp.url.query_string", query);
    configuration.setValue("space.activity.webapp.secure", "true");

    configurator.configure(null, activity, webServer);

    Mockito.verify(webServer).setPort(testPort);
    Mockito.verify(webServer).addStaticContentHandler("/" + activityName, new File(installDirectory, "webapp"));

    Assert.assertEquals(webSocketUri, configurator.getWebSocketUriPrefix());
    String webContentUrl = "https://localhost:" + testPort + "/" + activityName;
    Assert.assertEquals(webContentUrl, configurator.getWebContentUrl());
    Assert.assertEquals(webContentUrl + "/" + initialUrl + "?" + query, configurator.getWebInitialPage());
  }
}
