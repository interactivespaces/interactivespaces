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

package interactivespaces.resource.repository;

import interactivespaces.domain.basic.Activity;

/**
 * A repository server for Interactive Spaces resources.
 *
 * @author Keith M. Hughes
 */
public interface ResourceRepositoryServer {

  /**
   * Start the server up.
   */
  void startup();

  /**
   * Shut the server down.
   */
  void shutdown();

  /**
   * Get a full URI for the given resource.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return full URI for the resource with this server.
   */
  String getResourceUri(String category, String name, String version);
}