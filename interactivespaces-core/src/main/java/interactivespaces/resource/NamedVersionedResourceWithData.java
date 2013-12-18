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

/**
 * A named, versioned resource with some data about the resource.
 *
 * @param <T>
 *          type of the data with the resource
 *
 * @author Keith M. Hughes
 */
public class NamedVersionedResourceWithData<T> extends NamedVersionedResource {

  /**
   * The data associated with the resource.
   */
  private final T data;

  /**
   * Construct a new bundle id.
   *
   * @param name
   *          name of the bundle
   * @param version
   *          version of the bundle
   * @param data
   *          the data associated with the resource
   */
  public NamedVersionedResourceWithData(String name, Version version, T data) {
    super(name, version);

    this.data = data;
  }

  /**
   * get the resource data.
   *
   * @return the resource data
   */
  public T getData() {
    return data;
  }
}
