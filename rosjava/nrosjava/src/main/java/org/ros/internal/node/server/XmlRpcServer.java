/*
 * Copyright (C) 2011 Google Inc.
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

package org.ros.internal.node.server;

import org.apache.commons.logging.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.system.Process;
import org.ros.internal.xmlrpc.webserver.NettyXmlRpcWebServer;
import org.ros.log.RosLogFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base class for an XML-RPC server.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
public class XmlRpcServer {

  /**
   * The logger for the server.
   */
  private static final Log LOG = RosLogFactory.getLog(XmlRpcServer.class);

  /**
   * The RPC web server.
   */
  private final NettyXmlRpcWebServer server;

  /**
   * The advertising address for the server.
   */
  private final AdvertiseAddress advertiseAddress;

  /**
   * The countdown latch for detecting when the server has fully started up.
   */
  private final CountDownLatch startLatch;

  /**
   * The executor service for the server.
   */
  private final ScheduledExecutorService executorService;

  /**
   * Construct a new server.
   *
   * @param bindAddress
   *          the address to bind the server to
   * @param advertiseAddress
   *          the address to be used for advertising the server
   * @param executorService
   *          the threadpool for the server
   */
  public XmlRpcServer(BindAddress bindAddress, AdvertiseAddress advertiseAddress,
      ScheduledExecutorService executorService) {
    InetSocketAddress address = bindAddress.toInetSocketAddress();
    server = new NettyXmlRpcWebServer(address.getPort(), address.getAddress(), executorService, LOG);
    this.executorService = executorService;
    this.advertiseAddress = advertiseAddress;
    this.advertiseAddress.setPortCallable(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return server.getPort();
      }
    });
    startLatch = new CountDownLatch(1);
  }

  /**
   * Start up the remote calling server.
   *
   * @param instanceClass
   *          the class of the remoting server
   * @param instance
   *          an instance of the remoting server class
   * @param <T>
   *          the type of the RPC endpoint
   */
  public <T extends org.ros.internal.node.xmlrpc.XmlRpcEndpoint> void start(Class<T> instanceClass, T instance) {
    org.apache.xmlrpc.server.XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
    PropertyHandlerMapping phm = new PropertyHandlerMapping();
    phm.setRequestProcessorFactoryFactory(new NodeRequestProcessorFactoryFactory<T>(instance));
    try {
      phm.addHandler("", instanceClass);
    } catch (XmlRpcException e) {
      throw new RosRuntimeException(e);
    }
    xmlRpcServer.setHandlerMapping(phm);
    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
    serverConfig.setEnabledForExtensions(false);
    serverConfig.setContentLengthOptional(false);

    server.start();

    if (LOG.isDebugEnabled()) {
      LOG.debug("XmlRPCServer Bound to: " + getUri());
    }
    startLatch.countDown();
  }

  /**
   * Shut the remote call server down.
   */
  public void shutdown() {
    server.shutdown();
  }

  /**
   * Get the URI of the server.
   *
   * @return the {@link URI} of the server
   */
  public URI getUri() {
    return advertiseAddress.toUri("http");
  }

  /**
   * Get the socket address of the advertising address.
   *
   * @return the socket address
   */
  public InetSocketAddress getAddress() {
    return advertiseAddress.toInetSocketAddress();
  }

  /**
   * Get the advertising address for the server.
   *
   * @return the advertising address
   */
  public AdvertiseAddress getAdvertiseAddress() {
    return advertiseAddress;
  }

  /**
   * Wait for the start of the server.
   *
   * <p>
   * There is no time limit on this wait.
   *
   * @throws InterruptedException
   *           the thread got interrupted
   */
  public void awaitStart() throws InterruptedException {
    startLatch.await();
  }

  /**
   * Wait for the startup of the server.
   *
   * @param timeout
   *          the amount of time to wait for the server to start
   * @param unit
   *          the time units for the wait time
   *
   * @return {@code true} if the startup happened within the wait time
   *
   * @throws InterruptedException
   *           the thread got interrupted
   */
  public boolean awaitStart(long timeout, TimeUnit unit) throws InterruptedException {
    return startLatch.await(timeout, unit);
  }

  /**
   * get the PID of the RPC server process.
   *
   * @return PID of node process if available, throws {@link UnsupportedOperationException} otherwise
   */
  public int getPid() {
    return Process.getPid();
  }

  /**
   * Get the executor service for the server.
   *
   * @return the executor service
   */
  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }
}
