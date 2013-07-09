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

package interactivespaces.util.data.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * A data resource.
 *
 * @author Keith M. Hughes
 */
public class LocatableResource {

  /**
   * Name of the resource.
   */
  private String name;

  /**
   * URI for the location of the resource
   */
  private String locationUri;

  /**
   * Signature for the resource.
   */
  private String signature;

  /**
   * Metadata for the resource.
   */
  private Map<String, String> metadata;

  public LocatableResource(String name, String locationUri, String signature) {
    this(name, locationUri, signature, new HashMap<String, String>());
  }

  public LocatableResource(String name, String locationUri, String signature, Map<String, String> metadata) {
    this.name = name;
    this.locationUri = locationUri;
    this.signature = signature;
    this.metadata = metadata;
  }

  /**
   * Get the name of the resource.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the URI for the location of the resource.
   *
   * @return the location URI
   */
  public String getLocationUri() {
    return locationUri;
  }

  /**
   * Get the signature for the resource, e.g. message digest.
   *
   * @return the signature
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Get the metadata for the resource.
   *
   * @return the metadata
   */
  public Map<String, String> getMetadata() {
    return metadata;
  }
}
