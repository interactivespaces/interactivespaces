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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.binary.NativeBrowserRunner;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.configuration.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * An {@link ActivityComponent} which starts up a web browser.
 *
 * @author Keith M. Hughes
 */
public class WebBrowserActivityComponent extends BaseActivityComponent {

  /**
   * Name of the component.
   */
  public static final String COMPONENT_NAME = "web.browser";

  /**
   * Dependencies for the component.
   */
  public static final List<String> COMPONENT_DEPENDENCIES = Collections.unmodifiableList(Lists
      .newArrayList(WebServerActivityComponent.COMPONENT_NAME));

  /**
   * Configuration property giving the initial page of the web activity.
   *
   * <p>
   * If there is no content location, this should be a fully qualified URL.
   * Otherwise it can be relative to
   * {@link #CONFIGURATION_WEBAPP_CONTENT_LOCATION}.
   */
  public static final String CONFIGURATION_INITIAL_PAGE = "space.activity.webapp.url.initial";

  /**
   * Configuration property of any query string parameter which should be added
   * to the URL given to the web browser.
   */
  public static final String CONFIGURATION_INITIAL_URL_QUERY_STRING =
      "space.activity.webapp.url.query_string";

  /**
   * Configuration property saying whether the browser should be started in
   * debug mode or not.
   */
  public static final String CONFIGURATION_BROWSER_DEBUG = "space.activity.webapp.browser.debug";

  /**
   * Configuration property saying whether the browser should be started up when
   * the app starts up.
   */
  public static final String CONFIGURATION_BROWSER_STARTUP =
      "space.activity.webapp.browser.startup";

  /**
   * List of allowed prefixes for external URLs.
   */
  public static final List<String> EXTERNAL_URL_PREFIXES = Collections.unmodifiableList(Lists
      .newArrayList("http:", "https:"));

  /**
   * List of disallowed prefixes for external URLs.
   */
  public static final List<String> DISALLOWED_URL_PREFIXES = Collections.unmodifiableList(Lists
      .newArrayList("file:"));

  /**
   * The browser runner.
   */
  private NativeBrowserRunner browserRunner;

  /**
   * The base of the content URL (minus the opening page and query string).
   */
  private String baseContentUrl;

  /**
   * The full initial URL for the browser.
   */
  private String initialUrl;

  /**
   * {@code true} if the browser should be started in debug mode.
   */
  private boolean browserDebug;

  /**
   * {@code true} if the browser should be started up.
   */
  private boolean browserStartup;

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public List<String> getDependencies() {
    return COMPONENT_DEPENDENCIES;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

    WebServerActivityComponent webServer =
        componentContext.getActivityComponent(WebServerActivityComponent.COMPONENT_NAME);

    StringBuilder initialUrlBuilder = new StringBuilder();

    String configurationInitialPage =
        configuration.getRequiredPropertyString(CONFIGURATION_INITIAL_PAGE);

    configurationInitialPage = configurationInitialPage.trim();
    if (isDisallowedPrefix(configurationInitialPage)) {
      throw new InteractiveSpacesException(String.format(
          "The initial page %s starts with an illegal prefix", configurationInitialPage));
    }

    if (isExternalPrefix(configurationInitialPage)) {
      initialUrlBuilder.append(configurationInitialPage);
    } else {
      if (webServer == null) {
        throw new InteractiveSpacesException(String.format(
            "No activity component of type %s found", WebServerActivityComponent.COMPONENT_NAME));
      }

      initialUrlBuilder.append(webServer.getWebContentUrl()).append('/')
          .append(configurationInitialPage);
    }

    String queryString = configuration.getPropertyString(CONFIGURATION_INITIAL_URL_QUERY_STRING);
    if (queryString != null) {
      initialUrlBuilder.append("?").append(queryString);
    }

    initialUrl = initialUrlBuilder.toString();

    browserDebug = configuration.getPropertyBoolean(CONFIGURATION_BROWSER_DEBUG, false);

    browserStartup = configuration.getPropertyBoolean(CONFIGURATION_BROWSER_STARTUP, true);
  }

  @Override
  public void startupComponent() {
    if (browserStartup) {
      browserRunner = new NativeBrowserRunner(getComponentContext().getActivity());
      browserRunner.startup(initialUrl, browserDebug);
    }
  }

  @Override
  public void shutdownComponent() {
    if (browserRunner != null) {
      browserRunner.shutdown();
      browserRunner = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    // TODO(keith): Anything to check on the browser?
    return browserRunner != null && browserRunner.isRunning();
  }

  /**
   * Does the initial page have a disallowed prefix?
   *
   * @return {@code true} if disallowed
   */
  private boolean isDisallowedPrefix(String initialPage) {
    for (String prefix : DISALLOWED_URL_PREFIXES) {
      if (initialPage.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Does the initial page have an external prefix?
   *
   * @return {@code true} if external
   */
  private boolean isExternalPrefix(String initialPage) {
    for (String prefix : EXTERNAL_URL_PREFIXES) {
      if (initialPage.startsWith(prefix)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Set the base URL that the browser will connect to.
   *
   * @param baseContentUrl
   *          the base URL (can be {@code null}
   */
  public void setBaseContentUrl(String baseContentUrl) {
    this.baseContentUrl = baseContentUrl;
  }
}
