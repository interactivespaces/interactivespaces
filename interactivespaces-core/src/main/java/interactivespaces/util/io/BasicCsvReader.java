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

package interactivespaces.util.io;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.data.ColumnSourceHandler;
import interactivespaces.util.data.CsvColumnSource;

import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * A basic CSV reader.
 *
 * @author Keith M. Hughes
 */
public class BasicCsvReader implements CsvReader {

  @Override
  public void process(File source, ColumnSourceHandler handler) {
    process(source, handler, true);
  }

  @Override
  public void process(File source, ColumnSourceHandler handler, boolean skipFirstLine) {
    try {
      processSource(new FileReader(source), handler, skipFirstLine);
    } catch (Exception e) {
      throw new InteractiveSpacesException("Unable to read file " + source.getAbsolutePath(), e);
    }
  }

  @Override
  public void process(String source, ColumnSourceHandler handler) {
    process(source, handler, true);
  }

  @Override
  public void process(String source, ColumnSourceHandler handler, boolean skipFirstLine) {
    try {
      processSource(new StringReader(source), handler, skipFirstLine);
    } catch (Exception e) {
      throw new InteractiveSpacesException("Unable to read source string", e);
    }
  }

  /**
   * Process the CSV source.
   *
   * @param source
   *          the source content
   * @param handler
   *          the handler for each line
   * @param skipFirstLine
   *          {@code true} if should skip the first line
   *
   * @throws Exception
   *           something bad happened
   */
  private void processSource(Reader source, ColumnSourceHandler handler, boolean skipFirstLine) throws Exception {
    CsvColumnSource columnSource = new CsvColumnSource();

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(source);

      if (skipFirstLine) {
        reader.readLine();
      }

      String line = null;
      while ((line = reader.readLine()) != null) {
        columnSource.setLine(line);
        handler.processColumns(columnSource);
      }
    } finally {
      Closeables.closeQuietly(reader);
    }
  }
}
