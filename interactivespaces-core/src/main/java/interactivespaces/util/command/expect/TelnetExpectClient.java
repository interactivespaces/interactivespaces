/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.command.expect;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;

import org.apache.commons.logging.Log;
import org.apache.commons.net.telnet.TelnetClient;

import java.net.InetAddress;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An Expect session using Telnet.
 *
 * @author Keith M. Hughes
 */
public class TelnetExpectClient implements NetworkReachableExpectClient {

  /**
   * The telnet client.
   */
  private TelnetClient client;

  /**
   * The expect client to use.
   */
  private Expect expector;

  /**
   * The remote host to attach to.
   */
  private InetAddress targetHost;

  /**
   * The port on the remote host to attach to.
   */
  private int targetPort;

  /**
   * {@code true if logged into the telnet client.

   */
  private boolean loggedIn;

  /**
   * The executor service to use.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The log to use.
   */
  private final Log log;

  /**
   * The target timeout for responses in milliseconds.
   */
  private int targetTimeout = 60000;

  /**
   * Construct a telnet expect client with a standard telnet client.
   *
   * @param executorService
   *          the executor service to use
   * @param log
   *          the log to use
   */
  public TelnetExpectClient(ScheduledExecutorService executorService, Log log) {
    this(new TelnetClient(), executorService, log);
  }

  /**
   * Construct a telnet expect client with the specified telnet client.
   *
   * @param client
   *          the telnet client to use
   * @param executorService
   *          the executor service to use
   * @param log
   *          the log to use
   */
  public TelnetExpectClient(TelnetClient client, ScheduledExecutorService executorService, Log log) {
    this.client = client;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void connect() {
    try {
      log.debug("Starting connection to telnet device");
      if (loggedIn) {
        throw new IllegalStateException("Already connected");
      }

      log.info(String.format("Connecting to telnet service at %s:%d", targetHost, targetPort));

      client.setConnectTimeout(targetTimeout);
      client.connect(targetHost, targetPort);

      expector = new Expect(client.getInputStream(), client.getOutputStream(), executorService, log);
      expector.setDefaultTimeout(targetTimeout);
      expector.setThrowOnError(true);

      loggedIn = true;
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Error while connecting to telnet service at %s:%d",
          targetHost, targetPort), e);
    }
  }

  @Override
  public void disconnect() {
    loggedIn = false;
    try {
      log.info("Disconnecting from teclnet client");
      if (expector != null) {
        expector.close();
      }
    } catch (Exception e) {
      log.warn("Error while closing telnet session", e);
    } finally {
      expector = null;
    }

    try {
      if (client != null) {
        client.disconnect();
      }
    } catch (Exception e) {
      log.warn("Error while closing telnet session", e);
    } finally {
      client = null;
    }
  }

  @Override
  public void expect(String expectedString) throws InteractiveSpacesException {
    checkStatus();

    try {
      expector.expect(expectedString);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Error while expecting telnet command: %s", expectedString), e);
    }
  }

  @Override
  public void sendLn(String content) {
    checkStatus();

    expector.sendLn(content);
  }

  @Override
  public void send(String content) {
    checkStatus();

    expector.send(content);
  }

  private void checkStatus() {
    if (!loggedIn) {
      throw new SimpleInteractiveSpacesException("The telnet client is not logged in");
    }
  }

  @Override
  public InetAddress getTargetHost() {
    return targetHost;
  }

  @Override
  public void setTargetHost(InetAddress targetHost) {
    this.targetHost = targetHost;
  }

  @Override
  public int getTargetPort() {
    return targetPort;
  }

  @Override
  public void setTargetPort(int targetPort) {
    this.targetPort = targetPort;
  }

  @Override
  public int getTargetTimeout() {
    return targetTimeout;
  }

  @Override
  public void setTargetTimeout(int targetTimeout) {
    this.targetTimeout = targetTimeout;
  }
}
