package interactivespaces.example.comm.xbee.coordinator;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.AtRemoteResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.TxStatusXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import interactivespaces.service.comm.serial.xbee.internal.TxRequestXBeeFrameImpl;
import interactivespaces.service.comm.serial.xbee.internal.XBeeAddress64Impl;
import interactivespaces.util.ByteUtils;

/**
 * A simple Interactive Spaces Java-based activity.
 */
public class XBeeCoordinatorExampleActivity extends BaseActivity {

	/**
	 * The name of the config property for obtaining the serial port.
	 */
	public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT = "space.hardware.serial.port";

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
		XBeeCommunicationEndpointService service = getSpaceEnvironment()
				.getServiceRegistry().getRequiredService(
						XBeeCommunicationEndpointService.SERVICE_NAME);

		String portName = getConfiguration().getRequiredPropertyString(
				CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

		xbee = service.newXBeeCommunicationEndpoint(portName, getLog());

		xbee.addListener(new XBeeResponseListenerSupport() {

			@Override
			public void onAtLocalXBeeResponse(
					XBeeCommunicationEndpoint endpoint,
					AtLocalResponseXBeeFrame response) {
				getLog().info(response);
				getLog().info(ByteUtils.toHexString(response.getCommandData()));
			}

			@Override
			public void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint,
					RxResponseXBeeFrame response) {
				getLog().info(response);
			}

			@Override
			public void onTxStatusXBeeResponse(
					XBeeCommunicationEndpoint endpoint,
					TxStatusXBeeFrame response) {
				getLog().info(response);
			}

			@Override
			public void onAtRemoteXBeeResponse(
					XBeeCommunicationEndpoint endpoint,
					AtRemoteResponseXBeeFrame response) {
				getLog().info(response);
				getLog().info(ByteUtils.toHexString(response.getCommandData()));
			}
		});

		// Let IS manage the connection, which eans we don't have to start it or
		// shut it down.
		addManagedResource(xbee);
	}

	@Override
	public void onActivityStartup() {
	}

	@Override
	public void onActivityActivate() {
		// Ask the local radio what version of the AP protocol it is using.
		//
		// Using the escape code for the frame ID to make sure escapes are
		// happening properly.
		RequestXBeeFrame atLocalRequest = xbee.newAtLocalRequestXBeeFrame(
				XBeeApiConstants.AT_COMMAND_AP, 0x7d);

		atLocalRequest.write(xbee);
		
		getLog().info("Wrote AT command");
		
		// Send a TX packet to the remote radio. It will have frame ID 3.
		RequestXBeeFrame txRequest = new TxRequestXBeeFrameImpl(
				new XBeeAddress64Impl(getConfiguration()
						.getRequiredPropertyString(
								CONFIGURATION_PROPERTY_XBEE_REMOTE_ADDRESS64)),
				0x03, 0, 0);
		txRequest.add16(1234);

		txRequest.write(xbee);
		
		getLog().info("Wrote TX request");
	}
}
