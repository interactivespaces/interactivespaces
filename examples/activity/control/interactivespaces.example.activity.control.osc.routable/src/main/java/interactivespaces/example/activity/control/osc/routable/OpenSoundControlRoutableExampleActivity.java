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

package interactivespaces.example.activity.control.osc.routable;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpoint;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpointService;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientPacket;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlConstants;

import java.util.Map;

/**
 * An Interactive Spaces Java-based activity which controls an OSC device from a
 * route.
 *
 * @author Keith M. Hughes
 */
public class OpenSoundControlRoutableExampleActivity extends BaseRoutableRosActivity {

  /**
   * Configuration property giving the host of the OSC server.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_SERVER_HOST =
      "space.activity.osc.server.host";

  /**
   * Configuration property giving the port of the OSC server.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_SERVER_PORT =
      "space.activity.osc.server.port";

  /**
   * Configuration property giving the OSC address to be written to.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_ADDRESS = "osc.address";

  /**
   * Configuration property giving the maximum of the analog signal coming in.
   */
  public static final String CONFIGURATION_PROPERTY_ANALOG_MAX = "analog.max";

  /**
   * Configuration property giving the multipler for frequencies to be sent to
   * the OSC.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_SIGNAL_MULTIPLER = "osc.signal.multiplier";

  /**
   * Configuration property giving the base frequency for the OSC signal.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_FREQUENCY_BASE = "osc.frequency.base";

  /**
   * Message field that gives the analog signal.
   */
  public static final String MESSAGE_FIELD_ANALOG = "analog";

  /**
   * The communication endpoint for speaking to the OSC service.
   */
  private OpenSoundControlClientCommunicationEndpoint controlEndpoint;

  /**
   * The multiplier for the signal being sent to the OSC address.
   */
  private float signalMultiplier;

  /**
   * The base frequency for the OSC oscillator.
   */
  private float frequencyBase;

  /**
   * OSC address the packets are sent to
   */
  private String oscAddress;

  @Override
  public void onActivitySetup() {

    OpenSoundControlClientCommunicationEndpointService endpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            OpenSoundControlClientCommunicationEndpointService.SERVICE_NAME);

    String remoteHost =
        getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_OSC_SERVER_HOST);
    int remotePort =
        getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_OSC_SERVER_PORT);

    controlEndpoint = endpointService.newUdpEndpoint(remoteHost, remotePort, getLog());
    addManagedResource(controlEndpoint);

    frequencyBase =
        getConfiguration().getRequiredPropertyDouble(CONFIGURATION_PROPERTY_OSC_FREQUENCY_BASE)
            .floatValue();
    signalMultiplier =
        getConfiguration().getRequiredPropertyDouble(CONFIGURATION_PROPERTY_OSC_SIGNAL_MULTIPLER)
            .floatValue()
            / getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_ANALOG_MAX);

    oscAddress = getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_OSC_ADDRESS);
  }

  @Override
  public void onActivityActivate() {
    sendOscPacket(256);
  }

  @Override
  public void onActivityDeactivate() {
    sendOscPacket(512);
  }

  @Override
  public void onNewInputJson(String channelName, Map<String, Object> message) {
    getLog().info("Got message on input channel " + channelName);
    int analog = (Integer) message.get(MESSAGE_FIELD_ANALOG);

    if (isActivated()) {
      sendOscPacket(analog);
    }
  }

  /**
   * Send an OSC packet.
   *
   * @param analog
   *          the analog signal to send
   */
  private void sendOscPacket(int analog) {
    OpenSoundControlClientPacket packet = controlEndpoint.newPacket(oscAddress, OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT32);
    packet.writeFloat(analog * signalMultiplier + frequencyBase);
    packet.write();
  }
}
