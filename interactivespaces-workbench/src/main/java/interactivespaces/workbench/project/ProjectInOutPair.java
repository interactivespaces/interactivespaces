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

package interactivespaces.workbench.project;

import interactivespaces.SimpleInteractiveSpacesException;

/**
 * A simple input/output pair specification.
 *
 * @author Trevor Pering
 */
public class ProjectInOutPair {

  /**
   * The input path.
   */
  private final String inputPath;

  /**
   * Output path.
   */
  private final String outputPath;

  /**
   * Create a new file template.
   *
   * @param outputPath
   *          output path
   * @param inputPath
   *          input path
   */
  public ProjectInOutPair(String outputPath, String inputPath) {
    this.outputPath = outputPath;
    this.inputPath = inputPath;
  }

  /**
   * @return file input template path
   */
  public String getInputPath() {
    return inputPath;
  }

  /**
   * @return output path
   */
  public String getOutputPath() {
    return outputPath;
  }

  /**
   * Create a new instance from an input string specification.
   *
   * @param input
   *          input string
   *
   * @return instance constructed from input string
   */
  public static ProjectInOutPair fromString(String input) {
    String[] parts = input.split(",");
    if (parts.length > 2) {
      throw new SimpleInteractiveSpacesException("Too many parts in template spec " + input);
    }
    return new ProjectInOutPair(parts[0], parts[1]);
  }
}
