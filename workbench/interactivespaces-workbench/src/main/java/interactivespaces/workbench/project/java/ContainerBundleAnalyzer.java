/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.workbench.project.java;

import interactivespaces.InteractiveSpacesException;

import java.io.File;
import java.util.Set;

/**
 * An analyzer for various needed attributes of a container bundle, such as what its exports are.
 *
 * @author Keith M. Hughes
 */
public interface ContainerBundleAnalyzer {

  /**
   * Analyze a bundle.
   *
   * @param bundle
   *          the bundle to analyze
   *
   * @return the analyzer
   *
   * @throws InteractiveSpacesException
   *           the bundle could not be analyzed
   */
  ContainerBundleAnalyzer analyze(File bundle) throws InteractiveSpacesException;

  /**
   * Get the bundle's Java package exports.
   *
   * @return the set of all exported packages, will be {@code null} if {@link #analyze(File)} fails
   */
  Set<String> getPackageExports();
}
