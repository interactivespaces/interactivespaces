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

package interactivespaces.resource.analysis;

import interactivespaces.resource.NamedVersionedResourceCollection;
import interactivespaces.resource.NamedVersionedResourceWithData;

import java.io.File;

/**
 * An analyzer for collections of resources.
 *
 * @author Keith M. Hughes
 */
public interface ResourceAnalyzer {

  /**
   * Get all resources out of a directory and get their name, version, and a
   * reference for obtaining them.
   *
   * @param baseDir
   *          the directory to scan
   *
   * @return the resource collection
   */
  NamedVersionedResourceCollection<NamedVersionedResourceWithData<String>> getResourceCollection(File baseDir);
}
