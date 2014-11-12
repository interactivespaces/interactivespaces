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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.UdpServerRequest;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlServerCommunicationEndpoint;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlServerRequestMethod;
import interactivespaces.service.control.opensoundcontrol.RespondableOpenSoundControlIncomingMessage;

import org.apache.commons.logging.Log;

/**
 * A Open Sound Control server endpoint implementation by those crazy folks at Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOpenSoundControlServerCommunicationEndpoint implements
    OpenSoundControlServerCommunicationEndpoint {

  /**
   * The dispatcher for handling incoming OSC messages.
   */
  private final OpenSoundControlMethodDispatcher<RespondableOpenSoundControlIncomingMessage> dispatcher;

  /**
   * The communication endpoint for speaking with the DMX controller.
   */
  private final UdpServerNetworkCommunicationEndpoint udpServer;

  /**
   * The message parser.
   */
  private final InteractiveSpacesOpenSoundControlMessageParser messageParser =
      new InteractiveSpacesOpenSoundControlMessageParser();

  /**
   * Log for the endpoint.
   */
  private final Log log;

  /**
   * Construct a new endpoint.
   *
   * @param udpServer
   *          the UDP server endpoint
   * @param log
   *          the logger
   */
  public InteractiveSpacesOpenSoundControlServerCommunicationEndpoint(UdpServerNetworkCommunicationEndpoint udpServer,
      Log log) {
    this.udpServer = udpServer;
    this.log = log;

    udpServer.addListener(new UdpServerNetworkCommunicationEndpointListener() {

      @Override
      public void onUdpRequest(UdpServerNetworkCommunicationEndpoint serverEndpoint, UdpServerRequest serverRequest) {
        handleServerRequest(serverRequest);
      }
    });

    dispatcher = new OpenSoundControlMethodDispatcher<RespondableOpenSoundControlIncomingMessage>(log);
  }

  @Override
  public void startup() {
    log.info("Starting up Open Sound Control Server");
    udpServer.startup();
  }

  @Override
  public void shutdown() {
    log.info("Shutting down Open Sound Control Server");
    udpServer.shutdown();
  }

  @Override
  public int getServerPort() {
    return udpServer.getServerPort();
  }

  @Override
  public void registerMethod(String oscAddress, OpenSoundControlServerRequestMethod method) {
    dispatcher.addMethod(oscAddress, method);
  }

  @Override
  public void unregisterMethod(String oscAddress, OpenSoundControlServerRequestMethod method) {
    dispatcher.removeMethod(oscAddress, method);
  }

  @Override
  public void registerUnknownMessageMethod(OpenSoundControlServerRequestMethod method) {
    dispatcher.addUnknownMessageMethod(method);
  }

  @Override
  public void unregisterUnknownMessageMethod(OpenSoundControlServerRequestMethod method) {
    dispatcher.removeUnknownMessageMethod(method);
  }

  @Override
  public String toString() {
    return "InteractiveSpacesOpenSoundControlServerCommunicationEndpoint [serverPort=" + getServerPort() + "]";
  }

  /**
   * Handle a request to the server.
   *
   * @param serverRequest
   *          the server request
   */
  private void handleServerRequest(UdpServerRequest serverRequest) {
    try {
      dispatcher.handleIncomingMessage(messageParser.parseRespondableMessage(serverRequest));
    } catch (Throwable e) {
      log.error("Error while handling incoming Open Sound Control message", e);
    }
  }
}
