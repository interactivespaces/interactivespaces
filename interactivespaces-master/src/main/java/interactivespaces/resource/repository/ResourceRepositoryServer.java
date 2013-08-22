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


import interactivespaces.common.ResourceRepositoryUploadChannel;
import interactivespaces.util.data.resource.CopyableResourceListener;

import java.io.OutputStream;

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

  /**
   * Create an output stream for writing a new resource into the repository.
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return stream to use for writing the resource
   */
  OutputStream createResourceOutputStream(String category, String name, String version);

  /**
   * Register an upload listener for the given key.
   * @param listener
   *          Listener to use when an indicated resource is uploaded.
   */
  void registerResourceUploadListener(ResourceRepositoryUploadChannel channel,
      CopyableResourceListener listener);

  /**
   * Remove the upload listener for the given key.
   */
  void removeResourceUploadListener(ResourceRepositoryUploadChannel channel);
}
