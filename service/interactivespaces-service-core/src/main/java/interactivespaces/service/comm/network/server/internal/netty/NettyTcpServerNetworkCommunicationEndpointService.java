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

package interactivespaces.service.comm.network.server.internal.netty;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointService;

import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.nio.charset.Charset;

/**
 * A Netty based {@link TcpServerNetworkCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class NettyTcpServerNetworkCommunicationEndpointService extends BaseSupportedService implements
    TcpServerNetworkCommunicationEndpointService {

  @Override
  public String getName() {
    return TcpServerNetworkCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public TcpServerNetworkCommunicationEndpoint<String> newStringServer(byte[][] delimiters, Charset charset,
      int serverPort, Log log) {
    int length = delimiters.length;
    ChannelBuffer[] delimiterBuffers = new ChannelBuffer[length];
    for (int i = 0; i < length; i++) {
      delimiterBuffers[i] = ChannelBuffers.wrappedBuffer(delimiters[i]);
    }

    return new NettyStringTcpServerNetworkCommunicationEndpoint(delimiterBuffers, charset, serverPort,
        getSpaceEnvironment().getExecutorService(), log);
  }
}
