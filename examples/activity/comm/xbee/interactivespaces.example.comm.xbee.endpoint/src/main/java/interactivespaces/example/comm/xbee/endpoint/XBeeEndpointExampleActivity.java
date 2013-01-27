package interactivespaces.example.comm.xbee.endpoint;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import interactivespaces.util.ByteUtils;

/**
 * A simple Interactive Spaces Java-based activity.
 */
public class XBeeEndpointExampleActivity extends BaseActivity {

	/**
	 * The name of the config property for obtaining the serial port.
	 */
	public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT = "space.hardware.serial.port";

	/**
	 * The XBee endpoint.
	 */
	private XBeeCommunicationEndpoint xbee;

	@Override
	public void onActivitySetup() {
		XBeeCommunicationEndpointService service = getSpaceEnvironment()
				.getServiceRegistry().getRequiredService(
						XBeeCommunicationEndpointService.SERVICE_NAME);

		String portName = getConfiguration().getRequiredPropertyString(
				CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

		xbee = service.newXBeeCommunicationEndpoint(portName, getLog());

		// Let IS manage the connection, which eans we don't have to start it or
		// shut it down.
		addManagedResource(xbee);

		xbee.addListener(new XBeeResponseListenerSupport() {

			@Override
			public void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint,
					RxResponseXBeeFrame response) {
				getLog().info(response);
				getLog().info(ByteUtils.toHexString(response.getReceivedData()));
			}
		});
	}

	@Override
	public void onActivityStartup() {
		getLog().info(
				"Activity interactivespaces.example.comm.xbee.endpoint startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info(
				"Activity interactivespaces.example.comm.xbee.endpoint activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info(
				"Activity interactivespaces.example.comm.xbee.endpoint deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info(
				"Activity interactivespaces.example.comm.xbee.endpoint shutdown");
	}

	@Override
	public void onActivityCleanup() {
		getLog().info(
				"Activity interactivespaces.example.comm.xbee.endpoint cleanup");
	}
}
