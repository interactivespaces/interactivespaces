package interactivespaces.example.comm.xbee.endpoint.iosamples;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.serial.xbee.AtLocalRequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import interactivespaces.util.ByteUtils;

/**
 * An Interactive Spaces Java-based activity which sets up an XBee Radio for
 * analog and digital digital transmission to the coordinator.
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

    // Let IS manage the connection, which eans we don't have to start it or
    // shut it down.
    addManagedResource(xbee);

    xbee.addListener(new XBeeResponseListenerSupport() {

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
    AtLocalRequestXBeeFrame frame1 = xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_D0, 1);
    frame1.add(XBeeApiConstants.IO_FUNCTION_ANALOG);

    frame1.write(xbee);

    AtLocalRequestXBeeFrame frame2 = xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_D2, 2);
    frame2.add(XBeeApiConstants.IO_FUNCTION_DIGITAL_INPUT);

    frame2.write(xbee);

    AtLocalRequestXBeeFrame frame3 = xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_IR, 2);
    frame3.add(0x36);
    frame3.add(0x34);

    frame3.write(xbee);
  }

  @Override
  public void onActivityActivate() {
    getLog().info("Activity interactivespaces.example.comm.xbee.endpoint.iosamples activate");
  }

  @Override
  public void onActivityDeactivate() {
    getLog().info("Activity interactivespaces.example.comm.xbee.endpoint.iosamples deactivate");
  }

  @Override
  public void onActivityShutdown() {
    getLog().info("Activity interactivespaces.example.comm.xbee.endpoint.iosamples shutdown");
  }

  @Override
  public void onActivityCleanup() {
    getLog().info("Activity interactivespaces.example.comm.xbee.endpoint.iosamples cleanup");
  }
}
