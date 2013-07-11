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

import interactivespaces.InteractiveSpacesException;

import java.io.InputStream;

/**
 * A storage manager for the Interactive Spaces activity repository.
 *
 * @author Keith M. Hughes
 */
public interface ResourceRepositoryStorageManager {

  /**
   * Start the storage manager up.
   */
  void startup();

  /**
   * Shut the storage manager down.
   */
  void shutdown();

  /**
   *
   * Get the base location of the repository.
   *
   * TODO(keith): This must go away, we do not want the HTTP server to have
   * direct access to the file system, but rather look up an activity by its
   * identifying name and version. but then we need a web server handler that
   * can look up file names.
   *
   * @return the base location of the repository
   */
  String getRepositoryBaseLocation();

  /**
   * Get the name the resource has in the repository.
   *
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return the fully qualified name of the resource
   */
  String getRepositoryResourceName(String name, String version);

  /**
   * Does the repository contain a resource?
   *
   * @param name
   *          the name of the resource to be checked
   * @param version
   *          the version of the resource to be checked
   *
   * @return {@code true} if the repository contains the resource
   */
  boolean containsResource(String name, String version);

  /**
   * Stage a resource.
   *
   * @param resourceStream
   *          a stream of the incoming resource
   *
   * @return an opaque handle on the resource, do not make any assumptions on
   *         this handle, it can change
   */
  String stageResource(InputStream resourceStream);

  /**
   * Remove a staged activity from the manager.
   *
   * @param stageHandle
   *          The handle which was returned by
   *          {@link #stageResource(InputStream)}
   */
  void removeStagedReource(String stageHandle);

  /**
   * Get an {@link InputStream} for the description file in the staged activity.
   *
   * @param descriptorFileName
   *          name of the descriptor file
   * @param stageHandle
   *          the handle which was returned by
   *          {@link #stageResource(InputStream)}
   *
   * @return the input stream for the description file for the requested staged
   *         activity
   *
   * @throws InteractiveSpacesException
   *           if the stage handle is invalid or the activity contains no
   *           description file
   */
  InputStream getStagedResourceDescription(String descriptorFileName, String stageHandle);

  /**
   * Add a resource to the repository.
   *
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   * @param stageHandle
   *          the staging handle for the resource
   */
  void addResource(String name, String version, String stageHandle);
}
