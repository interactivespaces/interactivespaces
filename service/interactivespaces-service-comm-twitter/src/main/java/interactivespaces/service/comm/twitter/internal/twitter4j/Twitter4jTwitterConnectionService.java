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

package interactivespaces.service.comm.twitter.internal.twitter4j;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.twitter.TwitterConnection;
import interactivespaces.service.comm.twitter.TwitterService;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.Collections;
import java.util.List;

/**
 * A {@link TwitterService} using Twitter4j.
 *
 * @author Keith M. Hughes
 */
public class Twitter4jTwitterConnectionService extends BaseSupportedService implements TwitterService {

  /**
   * All twitter connections currently live.
   */
  private List<TwitterConnection> connections = Lists.newArrayList();

  /**
   * Construct a new twitter service.
   */
  public Twitter4jTwitterConnectionService() {
    connections = Lists.newArrayList();
    connections = Collections.synchronizedList(connections);
  }

  @Override
  public String getName() {
    return TwitterService.SERVICE_NAME;
  }

  @Override
  public void shutdown() {
    for (TwitterConnection conn : connections) {
      if (conn.isConnected()) {
        conn.shutdown();
      }
    }
  }

  @Override
  public TwitterConnection newTwitterConnection(String apiKey, String apiKeySecret, String userAccessToken,
      String userAccessTokenSecret, Log log) {
    Twitter4jTwitterConnection connection =
        new Twitter4jTwitterConnection(apiKey, apiKeySecret, userAccessToken, userAccessTokenSecret, log);
    connections.add(connection);

    return connection;
  }
}
