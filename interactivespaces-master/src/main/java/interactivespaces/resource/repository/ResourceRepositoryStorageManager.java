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
import interactivespaces.resource.NamedVersionedResourceCollection;
import interactivespaces.resource.NamedVersionedResourceWithData;
import interactivespaces.resource.Version;
import interactivespaces.util.resource.ManagedResource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A storage manager for the Interactive Spaces activity repository.
 *
 * @author Keith M. Hughes
 */
public interface ResourceRepositoryStorageManager extends ManagedResource {

  /**
   * Resource category for bundle resources.
   */
  String RESOURCE_CATEGORY_CONTAINER_BUNDLE = "bundle";

  /**
   * Resource category for activities.
   */
  String RESOURCE_CATEGORY_ACTIVITY = "activity";

  /**
   * Resource category for data.
   */
  String RESOURCE_CATEGORY_DATA = "data";

  /**
   * Get the name the resource has in the repository.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return the fully qualified name of the resource
   */
  String getRepositoryResourceName(String category, String name, Version version);

  /**
   * Does the repository contain a resource?
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource to be checked
   * @param version
   *          the version of the resource to be checked
   *
   * @return {@code true} if the repository contains the resource
   */
  boolean containsResource(String category, String name, Version version);

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
  InputStream getStagedResourceDescription(String descriptorFileName, String stageHandle)
      throws InteractiveSpacesException;

  /**
   * Get an {@link InputStream} for the entire activity bundle.
   *
   * @param stageHandle
   *          staging handle for which to get the data stream
   *
   * @return the input stream for the activity bundle
   *
   * @throws InteractiveSpacesException
   *           if the bundle can not be accessed, or the handle is invalid
   */
  InputStream getStagedResourceBundle(String stageHandle) throws InteractiveSpacesException;

  /**
   * Add a resource to the repository.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   * @param stageHandle
   *          the staging handle for the resource
   */
  void commitResource(String category, String name, Version version, String stageHandle);

  /**
   * Get a stream for a given resource.
   *
   * <p>
   * Closing the stream is the responsibility of the caller.
   *
   * @param category
   *          the category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return the input stream for the resource, or {@code null} if no such
   *         resource
   */
  InputStream getResourceStream(String category, String name, Version version);

  /**
   * Create an output stream for writing a new resource into the repository.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return stream to use for writing the resource
   */
  OutputStream newResourceOutputStream(String category, String name, Version version);

  /**
   * Get all the resources available in a give category.
   *
   * @param category
   *          the category
   *
   * @return all resources in the given category
   */
  NamedVersionedResourceCollection<NamedVersionedResourceWithData<String>> getAllResources(String category);

  /**
   * Get the base location for files from a give category.
   *
   * @param category
   *          the category
   *
   * @return the base location
   */
  File getBaseLocation(String category);
}
