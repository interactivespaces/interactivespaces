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


/**
 * A URL reader.
 *
 * <p>
 * All proper resource cleanup is done by instances of this class.
 *
 * @author Keith M. Hughes
 */
public interface UrlReader {

  /**
   * Read the contents of a URL.
   *
   * @param url
   *          the URL to be read
   * @param processor
   *          the processor for cleaning up the results
   * @param <T>
   *        the type of the output of the {@link UrlReaderProcessor}
   *
   * @return the processor results
   */
  <T> T read(String url, UrlReaderProcessor<T> processor);

  /**
   * Get the connection timeout.
   *
   * @return the connection timeout in msecs
   */
  int getConnectTimeout();

  /**
   * Set the connection timeout.
   *
   * @param connectTimeout
   *          the connection timeout in msecs
   */
  void setConnectTimeout(int connectTimeout);
}
