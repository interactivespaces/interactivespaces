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

package interactivespaces.controller.android;

import java.util.Map;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Android service which controls Interactive Spaces in the Android
 * environment.
 * 
 * <ul>
 * <li>Starts Interactive Spaces</li>
 * <li>Keeps device from sleeping</li>
 * </ul>
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesService extends Service {

	/**
	 * The lock for keeping the phone from sleeping.
	 */
	private PowerManager.WakeLock wakelock;

	/**
	 * The interactive spaces bootstrap container.
	 */
	private InteractiveSpacesFrameworkAndroidBootstrap isBootstrap;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Map<String, String> initialProperties = null;
		try {
			initialProperties = AndroidConfigurationProvider
					.getInitialProperties(PreferenceManager
							.getDefaultSharedPreferences(this));

		} catch (RuntimeException e) {
			Intent i = new Intent(this, InteractiveSpacesRootActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			i.putExtra(InteractiveSpacesRootActivity.INTENT_EXTRA_ERROR_MESSAGE

			, e.getMessage());
			getApplication().startActivity(i);

			stopSelf();

			return START_NOT_STICKY;
		}

		// Set to run in foreground
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
				"Start command received for Interactive Spaces Controller");
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Starting up Interactive Spaces Controller",
				System.currentTimeMillis() + 10000);
		// Passing in "null" instead of a PendingIntent means no activity will
		// be launched when the user clicks the notification
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
				"Notification.setLatestEventInfo for Interactive Spaces Controller");
		notification.setLatestEventInfo(this, "Interactive Spaces Controller",
				"The Interactive Spaces Controller is active", null);
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
				"startForeground for Interactive Spaces Controller");

		isBootstrap = new InteractiveSpacesFrameworkAndroidBootstrap();
		isBootstrap.startup(initialProperties, getApplicationContext());
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
				"starting Interactive Spaces container");

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"InteractiveSpacesRootActivity");
		wakelock.acquire();

		// 4321 is an arbitrary identification number.
		startForeground(4321, notification);
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
				"InteractiveSpacesService should have been moved to the foreground");

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
				"InteractiveSpacesService got onDestroy()");

		if (wakelock != null) {
			wakelock.release();
			wakelock = null;
		}

		if (isBootstrap != null) {
			isBootstrap.shutdown();
		}

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}