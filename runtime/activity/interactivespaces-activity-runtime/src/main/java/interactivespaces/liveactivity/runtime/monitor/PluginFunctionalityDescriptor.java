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

/**
 * A descriptor of plugin functionality.
 *
 * @author Keith M. Hughes
 */
public class PluginFunctionalityDescriptor {

  /**
   * URL for the functionality.
   */
  private final String url;

  /**
   * The display name.
   */
  private final String displayName;

  /**
   * Construct a new descriptor.
   *
   * @param url
   *          the URL for the functionality
   * @param displayName
   *          the display name
   */
  public PluginFunctionalityDescriptor(String url, String displayName) {
    this.url = url;
    this.displayName = displayName;
  }

  /**
   * Get the URL for the descriptor.
   *
   * @return the URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Get the display name for the descriptor.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }
}
