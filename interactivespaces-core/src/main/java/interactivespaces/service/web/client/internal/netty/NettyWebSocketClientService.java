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

package interactivespaces.service.web.client.internal.netty;

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.web.WebSocketHandler;
import interactivespaces.service.web.client.WebSocketClient;
import interactivespaces.service.web.client.WebSocketClientService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * A {@link WebSocketClientService} based on Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyWebSocketClientService implements WebSocketClientService {

  /**
   * Space environment for this service.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The metadata for the service.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public String getName() {
    return WebSocketClientService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public WebSocketClient newWebSocketClient(String uri, WebSocketHandler handler, Log log) {
    try {
      URI u = new URI(uri);

      return new NettyWebSocketClient(u, handler, spaceEnvironment.getExecutorService(), log);
    } catch (URISyntaxException e) {
      throw new InteractiveSpacesException(String.format("Bad URI syntax for web socket URI: %s",
          uri), e);
    }
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    // TODO Auto-generated method stub

  }
}
