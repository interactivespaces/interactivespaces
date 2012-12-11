package interactivespaces.example.activity.android.accelerometer;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.androidos.AndroidOsService;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * A Interactive Spaces Android-based activity which reads the accelerometer.
 */
public class AccelerometerAndroidActivity extends BaseActivity {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private SensorEventListener accelerometerEventListener;

	@Override
	public void onActivitySetup() {
		getLog().info(
				"Activity interactivespaces.example.activity.android.accelerometer setup");

		AndroidOsService androidService = getSpaceEnvironment()
				.getServiceRegistry().getService(AndroidOsService.SERVICE_NAME);
		sensorManager = (SensorManager) androidService
				.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accelerometerEventListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				onAccelerometerEvent(event);
			}
		};
		sensorManager.registerListener(accelerometerEventListener,
				accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onActivityStartup() {
		getLog().info(
				"Activity interactivespaces.example.activity.android.accelerometer startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info(
				"Activity interactivespaces.example.activity.android.accelerometer activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info(
				"Activity interactivespaces.example.activity.android.accelerometer deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info(
				"Activity interactivespaces.example.activity.android.accelerometer shutdown");
	}

	@Override
	public void onActivityCleanup() {
		getLog().info(
				"Activity interactivespaces.example.activity.android.accelerometer cleanup");
		sensorManager.unregisterListener(accelerometerEventListener);
	}

	/**
	 * An accelerometer event has happened.
	 * 
	 * @param event
	 *            the accelerometer event
	 */
	private void onAccelerometerEvent(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		getLog().info(String.format("Accelerometer event %f %f %f", x, y, z));
	}

}
