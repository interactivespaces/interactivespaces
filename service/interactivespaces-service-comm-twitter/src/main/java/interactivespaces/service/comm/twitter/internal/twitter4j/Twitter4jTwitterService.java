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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.service.comm.twitter.TwitterConnection;
import interactivespaces.service.comm.twitter.TwitterService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A {@link TwitterService} using Twitter4j.
 *
 * @author Keith M. Hughes
 */
public class Twitter4jTwitterService implements TwitterService {

  /**
   * Space environment for this service.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * All twitter connections currently live.
   */
  private List<TwitterConnection> connections = Lists.newArrayList();

  /**
   * The metadata for the service.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  public Twitter4jTwitterService() {
    connections = Lists.newArrayList();
    connections = Collections.synchronizedList(connections);
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public String getName() {
    return TwitterService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    // TODO Auto-generated method stub

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
  public TwitterConnection newTwitterConnection(String consumerKey, String consumerSecret,
      String accessToken, String accessTokenSecret) {
    Twitter4jTwitterConnection connection =
        new Twitter4jTwitterConnection(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    connections.add(connection);

    return connection;
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
