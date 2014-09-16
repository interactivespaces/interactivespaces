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

package interactivespaces.util.io;

import interactivespaces.util.data.ColumnSourceHandler;

import java.io.File;

/**
 * Read CSV (comma separated value) data sources.
 *
 * @author Keith M. Hughes
 */
public interface CsvReader {

  /**
   * Read through the contents of the file and process each line from the file using the supplied
   * {@link ColumnSourceHandler}.
   *
   * <p>
   * The first line is skipped.
   *
   * @param source
   *          the file to read
   * @param handler
   *          the handler which will process each line
   */
  void process(File source, ColumnSourceHandler handler);

  /**
   * Read through the contents of the file and process each line from the file using the supplied
   * {@link ColumnSourceHandler}.
   *
   * @param source
   *          the file to read
   * @param handler
   *          the handler which will process each line
   * @param skipFirstLine
   *          {@code true} if should skip the first line
   */
  void process(File source, ColumnSourceHandler handler, boolean skipFirstLine);

  /**
   * Read through the contents of the file and process each line from the file using the supplied
   * {@link ColumnSourceHandler}.
   *
   * <p>
   * The first line is skipped.
   *
   * @param source
   *          the file to read
   * @param handler
   *          the handler which will process each line
   */
  void process(String source, ColumnSourceHandler handler);

  /**
   * Read through the contents of the file and process each line from the file using the supplied
   * {@link ColumnSourceHandler}.
   *
   * @param source
   *          the file to read
   * @param handler
   *          the handler which will process each line
   * @param skipFirstLine
   *          {@code true} if should skip the first line
   */
  void process(String source, ColumnSourceHandler handler, boolean skipFirstLine);

}