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

package interactivespaces.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * A collection of versioned resources where the resources have both a name and
 * a version.
 *
 * @param <T>
 *          the type of resources in the collection
 *
 * @author Keith M. Hughes
 */
public class NamedVersionedResourceCollection<T> {

  /**
   * Construct a generically correct collection.
   *
   * @param <T>
   *          type of the resource in the collection
   *
   * @return a new collection
   */
  public static <T> NamedVersionedResourceCollection<T> newNamedVersionedResourceCollection() {
    return new NamedVersionedResourceCollection<T>();
  }

  /**
   * The resources.
   */
  private final Map<String, VersionedResourceCollection<T>> resources = Maps.newHashMap();

  /**
   * Add in a new resource into the collection.
   *
   * @param name
   *          name of the resource
   * @param version
   *          version of the resource
   * @param resource
   *          the resource
   *
   * @return the old resource that was there, or {@code null} if this is the
   *         first
   */
  public synchronized T addResource(String name, Version version, T resource) {
    VersionedResourceCollection<T> namedResources = resources.get(name);
    if (namedResources == null) {
      namedResources = VersionedResourceCollection.newVersionedResourceCollection();
      resources.put(name, namedResources);
    }

    return namedResources.addResource(version, resource);
  }

  /**
   * Get a named resource with the version requested.
   *
   * @param name
   *          the resource name
   * @param version
   *          the version
   *
   * @return the named resource with the given version, or null if none
   */
  public synchronized T getResource(String name, Version version) {
    VersionedResourceCollection<T> namedResources = resources.get(name);
    if (namedResources != null) {
      return namedResources.getResource(version);
    } else {
      return null;
    }
  }

  /**
   * Get a named resource in the range requested.
   *
   * @param name
   *          the resource name
   * @param range
   *          the range of acceptable versions, can be {@code null}
   *
   * @return the highest versioned resource satisfying the range constraint, the
   *         highest version if the range is {@code null}, or {@code null} if
   *         otherwise
   */
  public synchronized T getResource(String name, VersionRange range) {
    VersionedResourceCollection<T> namedResources = resources.get(name);
    if (namedResources != null) {
      return namedResources.getResource(range);
    } else {
      return null;
    }
  }

  /**
   * Get the highest versioned resource for the given name.
   *
   * @param name
   *          the resource name
   *
   * @return the highest versioned resource, or {@code null} if none
   */
  public synchronized T getHighestResource(String name) {
    VersionedResourceCollection<T> namedResources = resources.get(name);
    if (namedResources != null) {
      return namedResources.getHighestEntry();
    } else {
      return null;
    }
  }

  /**
   * Remove a resource.
   *
   * @param name
   *          the resource name
   * @param version
   *          the version to remove
   *
   * @return the removed resource, or {@code null} if none
   */
  public synchronized T removeResource(String name, Version version) {
    VersionedResourceCollection<T> namedResources = resources.get(name);
    if (namedResources != null) {
      return namedResources.removeResource(version);
    } else {
      return null;
    }
  }

  /**
   * Get a list of all resources in the collection.
   *
   * @return a newly constructed list of all resources
   */
  public List<T> getAllResources() {
    List<T> all = Lists.newArrayList();

    for (VersionedResourceCollection<T> resourceCollection : resources.values()) {
      all.addAll(resourceCollection.getAllResources());
    }

    return all;
  }
}
