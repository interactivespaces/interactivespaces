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

package interactivespaces.android.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * The Android service which controls Interactive Spaces
 * in the Android environment.
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
	 * The lock for keeping the phone from sleping.
	 */
	private PowerManager.WakeLock wakelock;

	public InteractiveSpacesService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Set to run in foreground
		Log.i("InteractiveSpacesRootActivity", "Notification for InteractiveSpacesService");
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Connecting to IS", System.currentTimeMillis() + 10000);
		// Passing in "null" instead of a PendingIntent means no activity will
		// be launched when the user clicks the notification
		Log.i("InteractiveSpacesRootActivity",
				"Notification.setLatestEventInfo for InteractiveSpacesService");
		notification.setLatestEventInfo(this, "IS Connection",
				"Launches apps from commands received by IS", null);
		Log.i("InteractiveSpacesRootActivity", "startForeground for InteractiveSpacesService");

		List<String> args = new ArrayList<String>();
		InteractiveSpacesFrameworkBootstrap isBootstrap = new InteractiveSpacesFrameworkBootstrap();
		isBootstrap.boot(args, getApplicationContext());
		Log.i("InteractiveSpacesRootActivity",
				"starting Interactive Spaces container");

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"InteractiveSpacesRootActivity");
		wakelock.acquire();

		// 4321 is an arbitrary identification number.
		startForeground(4321, notification);
		Log.i("InteractiveSpacesRootActivity",
				"InteractiveSpacesService should have been moved to the foreground");

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i("InteractiveSpacesRootActivity", "InteractiveSpacesService got onDestroy()");

		wakelock.release();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}