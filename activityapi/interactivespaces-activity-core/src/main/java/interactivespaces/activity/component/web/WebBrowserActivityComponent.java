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

package interactivespaces.activity.component.web;

import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.configuration.WebActivityConfiguration;

/**
 * An {@link ActivityComponent} which starts up a web browser.
 *
 * @author Keith M. Hughes
 */
public interface WebBrowserActivityComponent extends ActivityComponent {

  /**
   * Name of the component.
   */
  String COMPONENT_NAME = "web.browser";

  /**
   * Configuration property giving the initial page of the web activity.
   *
   * <p>
   * If there is no content location, this should be a fully qualified URL. Otherwise it can be relative to
   * the web activity content location root.
   */
  String CONFIGURATION_INITIAL_PAGE = WebActivityConfiguration.CONFIGURATION_PREFIX_WEBAPP
      + WebActivityConfiguration.CONFIGURATION_SUFFIX_INITIAL_PAGE;

  /**
   * Configuration property of any query string parameter which should be added to the URL given to the web browser.
   */
  String CONFIGURATION_INITIAL_URL_QUERY_STRING = WebActivityConfiguration.CONFIGURATION_PREFIX_WEBAPP
      + WebActivityConfiguration.CONFIGURATION_SUFFIX_INITIAL_URL_QUERY_STRING;

  /**
   * Configuration property saying whether the browser should be started in debug mode or not.
   */
  String CONFIGURATION_BROWSER_DEBUG = "space.activity.webapp.browser.debug";

  /**
   * Configuration property saying whether the browser should be started up when the app starts up.
   */
  String CONFIGURATION_BROWSER_STARTUP = "space.activity.webapp.browser.startup";

  /**
   * Set the base URL that the browser will connect to.
   *
   * @param baseContentUrl
   *          the base URL (can be {@code null}
   */
  void setBaseContentUrl(String baseContentUrl);
}
