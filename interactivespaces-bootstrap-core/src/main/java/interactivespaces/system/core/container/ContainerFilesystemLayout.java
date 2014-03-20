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

package interactivespaces.system.core.container;

/**
 * The filesystem layout for an Interactive Spaces container.
 *
 * @author Keith M. Hughes
 */
public class ContainerFilesystemLayout {

  /**
   * The folder where Interactive Spaces system files as needed for bootstrap
   * are kept.
   */
  public static final String FOLDER_INTERACTIVESPACES_SYSTEM = "lib/system/java";

  /**
   * Subdirectory which will contain the bootstrap bundles.
   */
  public static final String FOLDER_SYSTEM_BOOTSTRAP = "bootstrap";

  /**
   * Subdirectory which will contain the InteractiveSpaces configs.
   */
  public static final String FOLDER_CONFIG_INTERACTIVESPACES = "config/interactivespaces";

  /**
   * Subdirectory which will contain the local environment.
   */
  public static final String FOLDER_CONFIG_ENVIRONMENT = "config/environment";

  /**
   * Subdirectory which will contain additional bundles for bootstrap.
   */
  public static final String FOLDER_USER_BOOTSTRAP = "startup";
}
