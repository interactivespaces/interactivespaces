package interactivespaces.example.activity.wii.remote;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.hardware.drivers.gaming.wii.WiiRemoteDriver;
import interactivespaces.hardware.drivers.gaming.wii.WiiRemoteEventListener;
import interactivespaces.util.InteractiveSpacesUtilities;

/**
 * A simple Interactive Spaces activity to demonstrate use of the Wii Remote
 * driver.
 * 
 * @author Keith M. Hughes
 */
public class ExampleWiiRemoteActivity extends BaseActivity {

	/**
	 * The Wii Remote driver
	 */
	private WiiRemoteDriver driver;

	@Override
	public void onActivitySetup() {
		driver = new WiiRemoteDriver("8C56C5D8C5A4");

		WiiRemoteEventListener listener = new WiiRemoteEventListener() {

			@Override
			public void onWiiRemoteButtonEvent(int button) {
				onWiiButton(button);
			}

		};
		driver.addEventListener(listener);
		driver.startup(getSpaceEnvironment());
	}

	@Override
	public void onActivityStartup() {
		getLog().info(
				"Activity interactivespaces.example.activity.wii.remote startup");
	}

	@Override
	public void onActivityActivate() {
		for (int light = 0; light <= 3; light++) {
			driver.setLight(light);

			InteractiveSpacesUtilities.delay(1000);
		}

		InteractiveSpacesUtilities.delay(10000);
	}

	@Override
	public void onActivityCleanup() {
		if (driver != null) {
			driver.shutdown();
			driver = null;
		}
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
		}
	}

}
