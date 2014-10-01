/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.system.core.configuration;

/**
 * Core configuration properties for the system.
 *
 * @author Keith M. Hughes
 */
public interface CoreConfiguration {

  /**
   * Configuration property containing the Interactive Spaces version.
   */
  String CONFIGURATION_INTERACTIVESPACES_VERSION = "interactivespaces.version";

  /**
   * Property containing the Interactive Spaces root dir. This will be an absolute path.
   */
  String CONFIGURATION_INTERACTIVESPACES_BASE_INSTALL_DIR = "interactivespaces.rootdir";

  /**
   * Property containing the Interactive Spaces runtime location. This will be an absolute path.
   */
  String CONFIGURATION_INTERACTIVESPACES_RUNTIME_DIR = "interactivespaces.runtime";
}
