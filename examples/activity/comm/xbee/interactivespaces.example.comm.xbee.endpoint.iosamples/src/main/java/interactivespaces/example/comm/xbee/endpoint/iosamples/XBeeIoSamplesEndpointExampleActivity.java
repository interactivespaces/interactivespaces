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

package interactivespaces.example.comm.xbee.endpoint.iosamples;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import interactivespaces.util.ByteUtils;

/**
 * An Interactive Spaces Java-based activity which sets up an XBee Radio for
 * analog and digital IO Sample transmission to the coordinator.
 *
 * <p>
 * It will also log any Receive packets it receives.
 *
 * @author Keith M. Hughes
 */
public class XBeeIoSamplesEndpointExampleActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the serial port.
   */
  public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT =
      "space.hardware.serial.port";

  /**
   * The XBee endpoint.
   */
  private XBeeCommunicationEndpoint xbee;

  @Override
  public void onActivitySetup() {
    XBeeCommunicationEndpointService service =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            XBeeCommunicationEndpointService.SERVICE_NAME);

    String portName =
        getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

    xbee = service.newXBeeCommunicationEndpoint(portName, getLog());

    // Let IS manage the connection, whichm eans we don't have to start it or
    // shut it down.
    addManagedResource(xbee);

    xbee.addListener(new XBeeResponseListenerSupport() {

      @Override
      public void onAtLocalXBeeResponse(XBeeCommunicationEndpoint endpoint,
          AtLocalResponseXBeeFrame response) {
        getLog().info(response);
        getLog().info(ByteUtils.toHexString(response.getCommandData()));
        getLog().info(response.isSuccess());
      }

      @Override
      public void
          onRxXBeeResponse(XBeeCommunicationEndpoint endpoint, RxResponseXBeeFrame response) {
        getLog().info(response);
        getLog().info(ByteUtils.toHexString(response.getReceivedData()));
      }
    });
  }

  @Override
  public void onActivityStartup() {
    getLog().info("Activity interactivespaces.example.comm.xbee.endpoint.iosamples startup");

    // Configure DIO0 to be an analog input
    xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_D0)
        .add(XBeeApiConstants.IO_FUNCTION_ANALOG).write(xbee);

    // Configure DIO1 to be a digital input
    xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_D1)
        .add(XBeeApiConstants.IO_FUNCTION_DIGITAL_INPUT).write(xbee);

    // Want a sample every 1000 msec (1 second), which is 03e8 hex
    xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_IR).add16(0x03e8).write(xbee);

    // Since DH and DL are left at 0, samples are only send to coordinator.
    // Set DH and DL to specify a destination.
  }
}
