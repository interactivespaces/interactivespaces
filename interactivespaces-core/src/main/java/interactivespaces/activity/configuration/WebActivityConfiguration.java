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

/**
 * Common constants for configuring various web activity resources.
 *
 * @author Keith M. Hughes
 */
public class WebActivityConfiguration {

  /**
   * Prefix for web app configuration properties.
   */
  public static final String CONFIGURATION_PREFIX_WEBAPP = "space.activity.webapp";

  /**
   * Configuration property giving the initial page of the web activity.
   *
   * <p>
   * If there is no content location, this should be a fully qualified URL. Otherwise it can be relative to
   * {@link WebServerActivityResourceConfigurator.CONFIGURATION_WEBAPP_CONTENT_LOCATION} in a web server component.
   */
  public static final String CONFIGURATION_SUFFIX_INITIAL_PAGE = ".url.initial";

  /**
   * Default page to use when none specified in configuration.
   */
  public static final String DEFAULT_INITIAL_PAGE = "index.html";

  /**
   * Configuration property of any query string parameter which should be added to the URL given to the web browser.
   */
  public static final String CONFIGURATION_SUFFIX_INITIAL_URL_QUERY_STRING = ".url.query_string";


  /**
   * Separator for web path elements.
   */
  public static final char WEB_PATH_SEPARATOR = '/';

  /**
   * Separator for a web url query string.
   */
  public static final char WEB_QUERY_STRING_SEPARATOR = '?';
}
