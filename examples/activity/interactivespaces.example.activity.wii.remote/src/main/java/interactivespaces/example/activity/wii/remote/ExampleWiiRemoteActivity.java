package interactivespaces.example.activity.wii.remote;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.event.simple.EventDividerSampler;
import interactivespaces.hardware.driver.gaming.wii.WiiRemoteDriver;
import interactivespaces.hardware.driver.gaming.wii.WiiRemoteEventListener;
import interactivespaces.util.InteractiveSpacesUtilities;

/**
 * A simple Interactive Spaces activity to demonstrate use of the Wii Remote
 * driver.
 * 
 * <p>
 * You must find out the Bluetooth address of your Wii remote to use this
 * activity.
 * 
 * <p>
 * By default this activity will only have the Wii remote send button events. To
 * get accelerometer and button events, press the remote's plus and minus
 * buttons simultaneously. Pressing the combination again will switch back to
 * button only reporting.
 * 
 * @author Keith M. Hughes
 */
public class ExampleWiiRemoteActivity extends BaseActivity {

	/**
	 * Configuration property for the Bluetooth address for the Wii remote.
	 */
	public static final String CONFIGURATION_PROPERTY_WII_REMOTE_BLUETOOTH_ADDRESS = "activity.example.wii.remote.bluetooth.address";

	/**
	 * The button combination to start the accelerometer reporting.
	 */
	public static final int BUTTON_TOGGLE_ACCELEROMETER = WiiRemoteEventListener.VALUE_BUTTON_PLUS
			| WiiRemoteEventListener.VALUE_BUTTON_MINUS;

	/**
	 * The Wii Remote driver
	 */
	private WiiRemoteDriver driver;

	/**
	 * {@code true} if accelerometer should be used.
	 */
	private boolean useAcclerometer = false;

	/**
	 * {@code true} if accelerometer toggle has been pressed.
	 */
	private boolean accelerometerTogglePressed = false;

	/**
	 * How many accelerometer events are ignored before reporting?
	 * 
	 * <p>
	 * For now, one out of every 10 samples will be processed.
	 */
	private EventDividerSampler accelerometerSampler = new EventDividerSampler(
			10);

	@Override
	public void onActivitySetup() {
		String wiiRemoteBluetoothAddress = getConfiguration()
				.getRequiredPropertyString(
						CONFIGURATION_PROPERTY_WII_REMOTE_BLUETOOTH_ADDRESS);

		driver = new WiiRemoteDriver(wiiRemoteBluetoothAddress);

		WiiRemoteEventListener listener = new WiiRemoteEventListener() {

			@Override
			public void onWiiRemoteButtonEvent(int button) {
				onWiiButton(button);
			}

			@Override
			public void onWiiRemoteButtonAccelerometerEvent(int button,
					double x, double y, double z) {
				onWiiButtonAccelerometer(button, x, y, z);
			}
		};
		driver.addEventListener(listener);

		addDriver(driver);
	}

	@Override
	public void onActivityActivate() {
		// Make the lights dance. Done asynchronously so that we activate
		// quickly.
		getSpaceEnvironment().getExecutorService().submit(new Runnable() {

			@Override
			public void run() {
				for (int light = 0; light <= 3; light++) {
					driver.setLight(light);

					InteractiveSpacesUtilities.delay(1000);
				}
			}
		});
	}

	/**
	 * A Wii button has been pushed. Do something if activated.
	 * 
	 * @param button
	 *            the button which was pushed
	 */
	public void onWiiButton(int button) {
		if (isActivated()) {
			getLog().info("Wii button pressed " + button);

			handleAccelerometerToggle(button);
		}
	}

	/**
	 * A Wii button has been pushed and there is accelerometer data. Do
	 * something if activated.
	 * 
	 * @param button
	 *            the button which was pushed
	 * @param x
	 *            the x component of the accelerometer
	 * @param y
	 *            the y component of the accelerometer
	 * @param z
	 *            the z component of the accelerometer
	 */
	public void onWiiButtonAccelerometer(int button, double x, double y,
			double z) {
		if (isActivated()) {
			if (accelerometerSampler.sample()) {
				getLog().info(
						String.format(
								"Wii button pressed %d and accelerometer %f, %f, %f",
								button, x, y, z));

				handleAccelerometerToggle(button);
			}
		}
	}

	/**
	 * Handle an accelerometer toggle, if any.
	 * 
	 * @param button
	 *            the current button value
	 */
	private void handleAccelerometerToggle(int button) {
		if (button == BUTTON_TOGGLE_ACCELEROMETER) {
			if (!accelerometerTogglePressed) {
				getLog().info("Toggling accelerometer");
				useAcclerometer = !useAcclerometer;

				if (useAcclerometer) {
					accelerometerSampler.reset();
				}

				driver.setAccelerometerReporting(useAcclerometer);

				accelerometerTogglePressed = true;
			}

		} else {
			accelerometerTogglePressed = false;
		}
	}
}
