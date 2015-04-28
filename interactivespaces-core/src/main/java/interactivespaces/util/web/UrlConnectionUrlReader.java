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

package interactivespaces.util.web;

import interactivespaces.InteractiveSpacesException;

import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link UrlReader} that uses the Java UrlConnection class.
 *
 * @author Keith M. Hughes
 */
public class UrlConnectionUrlReader implements UrlReader {

  /**
   * Default value for the connection timeout.
   */
  public static final int CONNECT_TIMEOUT_DEFAULT = 5000;

  /**
   * Timeout for the connection in msecs.
   */
  private int connectTimeout = CONNECT_TIMEOUT_DEFAULT;;

  @Override
  public <T> T read(String url, UrlReaderProcessor<T> processor) {
    BufferedReader reader = null;
    try {
      URL u = new URL(url);

      URLConnection connection = u.openConnection();
      connection.setConnectTimeout(connectTimeout);

      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      return processor.process(reader);
    } catch (Throwable e) {
      throw new InteractiveSpacesException(String.format("Could not process URL contents for %s",
          url), e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }

  @Override
  public int getConnectTimeout() {
    return connectTimeout;
  }

  @Override
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }
}
