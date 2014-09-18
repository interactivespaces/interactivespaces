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

package interactivespaces.workbench.project.java;

import java.io.File;
import java.util.List;

/**
 * Create an container bundle from a source.
 *
 * @author Keith M. Hughes
 */
public interface ContainerBundleCreator {

  /**
   * Create a bundle from a given source.
   *
   * @param source
   *          file for the source jar
   * @param output
   *          where the file should be written
   * @param classpath
   *          the classpath for the bundle
   *
   * @throws Exception
   *           something bad happened
   */
  void createBundle(File source, File output, List<File> classpath) throws Exception;
}
