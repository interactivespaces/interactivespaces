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

package interactivespaces.master.resource.deployment;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Access to the repository for features.
 *
 * @author Keith M. Hughes
 */
public interface FeatureRepository {

  /**
   * Start the feature repository up.
   */
  void startup();

  /**
   * Shut the feature repository down.
   */
  void shutdown();

  /**
   * Get all features which are available.
   *
   * @return
   */
  List<Feature> getAllFeatures();

  /**
   * Get a feature.
   *
   * @param id
   *          ID of the feature.
   *
   * @return The feature, or null if no feature with that ID.
   */
  Feature getFeature(String id);

  /**
   * Get an input stream which will get a bundle.
   *
   * <p>
   * The client is responsible for closing the stream.
   *
   * @param bundleName
   *          Name of a bundle.
   *
   * @return A stream for the feature with the given ID, or null if no feature
   *         with the given ID.
   */
  InputStream getFeatureBundleStream(String bundleName);

  /**
   * Get a file for the bundle.
   *
   * TODO(keith): Figure out how to use a stream in the Netty handler.
   *
   * @param bundleName
   * @return
   */
  File getFeatureFile(String bundleName);
}
