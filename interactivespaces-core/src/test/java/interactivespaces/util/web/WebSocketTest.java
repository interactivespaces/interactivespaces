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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.service.web.WebSocketConnection;
import interactivespaces.service.web.WebSocketHandler;
import interactivespaces.service.web.client.internal.netty.NettyWebSocketClient;
import interactivespaces.service.web.server.WebServerWebSocketHandler;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;
import interactivespaces.service.web.server.WebServerWebSocketHandlerSupport;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A test for web socket connections.
 *
 * <p>
 * This test does both the web socket server and client sides.
 *
 * @author Keith M. Hughes
 */
public class WebSocketTest {

  private Log log;
  private ScheduledExecutorService threadPool;

  private WebSocketConnection serverConnection;

  @Before
  public void setup() {
    log = new Jdk14Logger("goober"); // Mockito.mock(Log.class);

    threadPool = Executors.newScheduledThreadPool(100);
  }

  @After
  public void cleanup() {
    threadPool.shutdown();
  }

  @Test
  public void testWebSocketCommunication() throws Exception {
    final String dataKey = "message";

    final CountDownLatch clientOpenning = new CountDownLatch(1);
    final CountDownLatch clientClosing = new CountDownLatch(1);

    final AtomicBoolean onConnectCalledServer = new AtomicBoolean(false);
    final AtomicBoolean onCloseCalledServer = new AtomicBoolean(false);

    final AtomicReference<WebServerWebSocketHandler> serverHandler =
        new AtomicReference<WebServerWebSocketHandler>();

    int port = 8082;
    String webSocketUriPrefix = "websockettest";

    URI uri = new URI(String.format("ws://127.0.0.1:%d/%s", port, webSocketUriPrefix));

    final List<Integer> serverReceivedList = Lists.newArrayList();
    final List<Integer> clientReceivedList = Lists.newArrayList();
    List<Integer> serverSentList = Lists.newArrayList();
    List<Integer> clientSentList = Lists.newArrayList();
    Random random = new Random(System.currentTimeMillis());

    for (int i = 0; i < 100; i++) {
      clientSentList.add(random.nextInt());
      serverSentList.add(random.nextInt());
    }

    NettyWebServer server = new NettyWebServer("test-server", port, threadPool, log);
    server.setWebSocketHandlerFactory(webSocketUriPrefix, new WebServerWebSocketHandlerFactory() {

      @Override
      public WebServerWebSocketHandler newWebSocketHandler(WebSocketConnection connection) {
        WebServerWebSocketHandler handler = new WebServerWebSocketHandlerSupport(connection) {

          @Override
          public void onReceive(Object data) {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = (Map<String, Object>) data;

            serverReceivedList.add((Integer) d.get(dataKey));
          }

          @Override
          public void onConnect() {
            onConnectCalledServer.set(true);
          }

          @Override
          public void onClose() {
            onCloseCalledServer.set(true);
          }
        };

        serverHandler.set(handler);

        return handler;
      }
    });
    server.startup();

    Thread.sleep(2000);

    WebSocketHandler clientHandler = new WebSocketHandler() {

      @Override
      public void onConnect() {
        clientOpenning.countDown();
      }

      @Override
      public void onClose() {
        clientClosing.countDown();
      }

      @Override
      public void onReceive(Object data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> d = (Map<String, Object>) data;

        clientReceivedList.add((Integer) d.get(dataKey));
      }
    };

    NettyWebSocketClient client = new NettyWebSocketClient(uri, clientHandler, threadPool, log);
    client.startup();

    Assert.assertTrue(clientOpenning.await(10, TimeUnit.SECONDS));

    Assert.assertTrue(client.isOpen());

    Map<String, Object> data = Maps.newHashMap();
    for (Integer i : clientSentList) {
      data.put("message", i);
      client.writeDataAsJson(data);
    }

    for (Integer i : serverSentList) {
      data.put("message", i);
      serverHandler.get().sendJson(data);
    }

    client.ping();

    client.shutdown();

    Assert.assertTrue(clientClosing.await(10, TimeUnit.SECONDS));

    server.shutdown();

    Assert.assertEquals(clientSentList, serverReceivedList);
    Assert.assertEquals(serverSentList, clientReceivedList);
    Assert.assertTrue(onConnectCalledServer.get());
    Assert.assertTrue(onCloseCalledServer.get());
  }
}
