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

package interactivespaces.workbench.project.constituent;

import java.io.File;

import interactivespaces.configuration.Configuration;

/**
 * Common base class for project constituents, providing common functionality.
 *
 * @author Trevor Pering
 */
public abstract class BaseProjectConstituent implements ProjectConstituent {

  /**
   * Return the appropriate file path depending on evaluate and default root directory.
   *
   * @param rootDirectory
   *          root directory to use in case of default
   * @param resourceConfig
   *          configuration used for evaluation
   * @param target
   *          target path desired
   *
   * @return appropriate file to use
   */
  protected File getProjectTarget(File rootDirectory, Configuration resourceConfig, String target) {
    String targetPath = resourceConfig.evaluate(target);
    File targetFile = new File(targetPath);
    if (targetFile.isAbsolute()) {
      return targetFile;
    }
    return new File(rootDirectory, targetPath);
  }
}
