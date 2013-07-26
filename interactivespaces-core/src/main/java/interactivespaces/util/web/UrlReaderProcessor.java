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

package interactivespaces.util.web;

import java.io.BufferedReader;

/**
 * A processor for a {@link UrlReader}. This callback class processes the
 * content of the reader.
 *
 * @author Keith M. Hughes
 */
public interface UrlReaderProcessor<T> {

  /**
   * Process the contents of the reader.
   *
   * @param reader
   *          the reader with the URL contents
   *
   * @return the result from the reader
   *
   * @throws Exception
   *           something bad happened
   */
  T process(BufferedReader reader) throws Exception;
}
