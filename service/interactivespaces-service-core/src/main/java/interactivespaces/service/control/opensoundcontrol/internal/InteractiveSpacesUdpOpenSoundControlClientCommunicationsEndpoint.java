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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointListener;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpoint;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientResponseMethod;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlConstants;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlIncomingMessage;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlOutgoingMessage;

import org.apache.commons.logging.Log;

import java.net.InetSocketAddress;

/**
 * An Interactive Spaces implementation of an {@link OpenSoundControlClientCommunicationEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint implements
    OpenSoundControlClientCommunicationEndpoint {

  /**
   * Address of the remote socket.
   */
  private InetSocketAddress remoteAddress;

  /**
   * Communication endpoint for talking to the OSC server.
   */
  private UdpClientNetworkCommunicationEndpoint udpClientEndpoint;

  /**
   * The dispatcher for handling incoming OSC messages.
   */
  private final OpenSoundControlMethodDispatcher<OpenSoundControlIncomingMessage> dispatcher;

  /**
   * The message parser.
   */
  private final InteractiveSpacesOpenSoundControlMessageParser messageParser =
      new InteractiveSpacesOpenSoundControlMessageParser();

  /**
   * Log for the connection.
   */
  private Log log;

  /**
   * Construct a new client.
   *
   * @param remoteAddress
   *          the remote address to be contacted
   * @param udpClientEndpoint
   *          the UDP client endpoint
   * @param log
   *          the logger
   */
  public InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint(InetSocketAddress remoteAddress,
      UdpClientNetworkCommunicationEndpoint udpClientEndpoint, Log log) {
    this.remoteAddress = remoteAddress;
    this.udpClientEndpoint = udpClientEndpoint;
    this.log = log;

    udpClientEndpoint.addListener(new UdpClientNetworkCommunicationEndpointListener() {
      @Override
      public void onUdpResponse(UdpClientNetworkCommunicationEndpoint endpoint, byte[] response,
          InetSocketAddress remoteAddress) {
        handleIncomingOpenSoundControlMessage(remoteAddress, response);
      }
    });

    dispatcher = new OpenSoundControlMethodDispatcher<OpenSoundControlIncomingMessage>(log);
  }

  @Override
  public void startup() {
    udpClientEndpoint.startup();
  }

  @Override
  public void shutdown() {
    udpClientEndpoint.shutdown();
  }

  @Override
  public String getRemoteHost() {
    return remoteAddress.getHostName();
  }

  @Override
  public int getRemotePort() {
    return remoteAddress.getPort();
  }

  @Override
  public OpenSoundControlOutgoingMessage newRequestMessageWithTypes(String address, String types) {
    return new InteractiveSpacesOscOutgoingMessage(udpClientEndpoint.newDynamicWriteableUdpPacket(), remoteAddress,
        address, types);
  }

  @Override
  public OpenSoundControlOutgoingMessage newRequestMessageWithTypes(String address, byte... types) {
    return new InteractiveSpacesOscOutgoingMessage(udpClientEndpoint.newDynamicWriteableUdpPacket(), remoteAddress,
        address, types);
  }
  @Override
  public OpenSoundControlOutgoingMessage sendRequestMessage(String address, Object... arguments) {
    OpenSoundControlOutgoingMessage message = newRequestMessage(address,  arguments);

    message.send();

    return message;
  }

  @Override
  public OpenSoundControlOutgoingMessage newRequestMessage(String address, Object... arguments) {
    byte[] types = null;
    if (arguments != null && arguments.length > 0) {
      types = new byte[arguments.length];

      for (int arg = 0; arg < arguments.length; arg++) {
        Object argument = arguments[arg];
        if (argument instanceof String) {
          types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_STRING;
        } else if (argument instanceof Integer) {
          types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT32;
        } else if (argument instanceof Long) {
          types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT64;
        } else if (argument instanceof Float) {
          types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT32;
        } else if (argument instanceof Double) {
          types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT64;
        } else if (argument instanceof byte[]) {
          types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_BLOB;
        }
      }
    }

    InteractiveSpacesOscOutgoingMessage message =
        new InteractiveSpacesOscOutgoingMessage(udpClientEndpoint.newDynamicWriteableUdpPacket(), remoteAddress,
            address, types);

    if (arguments != null && arguments.length > 0) {
      for (int arg = 0; arg < arguments.length; arg++) {
        Object argument = arguments[arg];
        if (argument instanceof String) {
          message.addString((String) argument);
        } else if (argument instanceof Integer) {
          message.addInt((Integer) argument);
        } else if (argument instanceof Long) {
          message.addLong((Long) argument);
        } else if (argument instanceof Float) {
          message.addFloat((Float) argument);
        } else if (argument instanceof Double) {
          message.addDouble((Double) argument);
        } else if (argument instanceof byte[]) {
          message.addBlob((byte[]) argument);
        } else {
          throw new SimpleInteractiveSpacesException(String.format(
              "Unsupported data type in Open Sound Control message %s", argument.getClass().getName()));
        }
      }
    }

    return message;
  }

  @Override
  public void registerResponseMethod(String oscAddress, OpenSoundControlClientResponseMethod handler) {
    dispatcher.addMethod(oscAddress, handler);
  }

  @Override
  public void unregisterResponseMethod(String oscAddress, OpenSoundControlClientResponseMethod handler) {
    dispatcher.removeMethod(oscAddress, handler);
  }

  @Override
  public void registerUnknownMessageResponseMethod(OpenSoundControlClientResponseMethod method) {
    dispatcher.addUnknownMessageMethod(method);
  }

  @Override
  public void unregisterUnknownMessageResponseMethod(OpenSoundControlClientResponseMethod method) {
    dispatcher.removeUnknownMessageMethod(method);
  }

  @Override
  public String toString() {
    return "InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint [remoteHost=" + getRemoteHost()
        + ", remotePort=" + getRemotePort() + "]";
  }

  /**
   * Handle an incoming response message.
   *
   * @param senderAddress
   *          address of the server that sent the response
   * @param response
   *          the response from the server
   */
  private void handleIncomingOpenSoundControlMessage(InetSocketAddress senderAddress, byte[] response) {
    try {
      dispatcher.handleIncomingMessage(messageParser.parseMessage(senderAddress, response));
    } catch (Throwable e) {
      log.error("Error while handling incoming Open Sound Control message", e);
    }
  }

}
