/*
 * Copyright (C) 2015 Google Inc.
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

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;

/**
 * A dependency provider for a single file.
 *
 * @author Keith M. Hughes
 */
public class FileProjectDependencyProvider implements ProjectDependencyProvider {

  /**
   * The file providing the dependency.
   */
  private File file;

  /**
   * The file support to use for expansions.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new provider.
   *
   * @param file
   *          the file providing the dependency
   */
  public FileProjectDependencyProvider(File file) {
    this.file = file;
  }

  @Override
  public void placeContents(File destination) {
    fileSupport.unzip(file, destination);
  }
}
