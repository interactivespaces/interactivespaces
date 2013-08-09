package interactivespaces.example.comm.xbee.coordinator.iosamples;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.AtRemoteResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxIoSampleXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.TxStatusXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeAddress64;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import interactivespaces.util.ByteUtils;
import interactivespaces.util.InteractiveSpacesUtilities;

/**
 * A simple Interactive Spaces Java-based activity which listens for events from
 * an XBee radio configured as a Coordinator.
 *
 * <p>
 * This example sends AP T commands to the remote radio to set DIO0 to be an
 * analog input, DIO1 to be a digital input, and the sample rate to be 1 second.
 *
 * @author Keith M. Hughes
 */
public class XBeeIoSamplesCoordinatorExampleActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the serial port.
   */
  public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT =
      "space.hardware.serial.port";

  /**
   * The name of the config property for obtaining the 64 bit address of the
   * endpoint radio.
   */
  public static final String CONFIGURATION_PROPERTY_XBEE_REMOTE_ADDRESS64 = "xbee.remote.address64";

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

    xbee.addListener(new XBeeResponseListenerSupport() {

      @Override
      public void onAtLocalXBeeResponse(XBeeCommunicationEndpoint endpoint,
          AtLocalResponseXBeeFrame response) {
        getLog().info(response);
        getLog().info(ByteUtils.toHexString(response.getCommandData()));
      }

      @Override
      public void
          onRxXBeeResponse(XBeeCommunicationEndpoint endpoint, RxResponseXBeeFrame response) {
        getLog().info(response);
      }

      @Override
      public void onTxStatusXBeeResponse(XBeeCommunicationEndpoint endpoint,
          TxStatusXBeeFrame response) {
        getLog().info(response);
      }

      @Override
      public void onAtRemoteXBeeResponse(XBeeCommunicationEndpoint endpoint,
          AtRemoteResponseXBeeFrame response) {
        getLog().info(response);
        getLog().info(ByteUtils.toHexString(response.getCommandData()));
      }

      @Override
      public void onRxIoSampleXBeeResponse(XBeeCommunicationEndpoint endpoint,
          RxIoSampleXBeeFrame response) {
        getLog().info(response);
      }
    });

    // Let IS manage the connection, which means we don't have to start it or
    // shut it down.
    addManagedResource(xbee);
  }

  @Override
  public void onActivityActivate() {
    // Ask the local radio what version of the AP protocol it is using.
    //
    // Using the escape code for the frame ID to make sure escapes are
    // happening properly.
    xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_AP, 0x7d).write(xbee);

    getLog().info("Wrote AT command");

    // Send a TX packet to the remote radio. It will have frame ID 3.
    XBeeAddress64 remoteAddress =
        xbee.newXBeeAddress64(getConfiguration().getRequiredPropertyString(
            CONFIGURATION_PROPERTY_XBEE_REMOTE_ADDRESS64));
    xbee.newTxRequestXBeeFrame(remoteAddress, 0x03, 0, 0).add16(1234).write(xbee);

    getLog().info("Wrote TX request");

    InteractiveSpacesUtilities.delay(1000);

    // Configure DIO0 to be an analog input
    xbee.newAtRemoteRequestXBeeFrame(remoteAddress, XBeeApiConstants.AT_COMMAND_D0, 0)
        .add(XBeeApiConstants.IO_FUNCTION_ANALOG).write(xbee);

    InteractiveSpacesUtilities.delay(1000);

    // Configure DIO1 to be a digital input
    xbee.newAtRemoteRequestXBeeFrame(remoteAddress, XBeeApiConstants.AT_COMMAND_D1, 0)
        .add(XBeeApiConstants.IO_FUNCTION_DIGITAL_INPUT).write(xbee);

    InteractiveSpacesUtilities.delay(1000);

    // Want a sample every 1000 msec (1 second), which is 03e8 hex
    xbee.newAtRemoteRequestXBeeFrame(remoteAddress, XBeeApiConstants.AT_COMMAND_IR, 0)
        .add16(0x03e8).write(xbee);

    // Since DH and DL are left at 0, samples are only send to coordinator.
    // Set DH and DL to specify a destination.

  }
}
