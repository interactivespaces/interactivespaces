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

import interactivespaces.InteractiveSpacesException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * A URL reader
 *
 * @author Keith M. Hughes
 */
public class UrlReader {

  /**
   * Default value for the connection timeout.
   */
  public static final int CONNECT_TIMEOUT_DEFAULT = 5000;

  /**
   * Timeout for the connection in msecs.
   */
  private int connectTimeout = CONNECT_TIMEOUT_DEFAULT;;

  /**
   * Read the contents of a URL
   *
   * @param url
   *          the URL to be read
   * @param processor
   *          the processor for cleaning up the results
   *
   * @return the processor results
   */
  public <T> T read(String url, UrlReaderProcessor<T> processor) {
    BufferedReader reader = null;
    try {
      URL u = new URL(url);

      URLConnection connection = u.openConnection();
      connection.setConnectTimeout(connectTimeout);

      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      return processor.process(reader);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not process URL contents for %s",
          url), e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // Don't care
        }
      }
    }
  }

  /**
   * Get the connection timeout.
   *
   * @return the connection timeout in msecs
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Set the connection timeout.
   *
   * @param connectTimeout
   *          the connection timeout in msecs
   */
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }
}
