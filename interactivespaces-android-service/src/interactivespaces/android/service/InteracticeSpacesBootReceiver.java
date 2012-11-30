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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A boot listener so that Interactive Spaces can be launched on Android boot.
 * 
 * @author Keith M. Hughes
 */
public class InteracticeSpacesBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent myIntent) {
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME, "INTENT RECEIVED: "
				+ myIntent.getAction());
		// Detect if the device just booted up, and automatically start the
		// foreground service if so.
		if ((myIntent.getAction())
				.equals("android.intent.action.BOOT_COMPLETED")) {

			Log.i(AndroidLoggingProvider.BASE_LOG_NAME, "onReceive: "
					+ myIntent.getAction());

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);

			if (prefs.getBoolean("PREF_START_ON_BOOT", false)) {
				Log.i(AndroidLoggingProvider.BASE_LOG_NAME, "Autostarting Interactive Spaces controller on boot");
				AndroidInteractiveSpacesEnvironment
						.startInteractiveSpacesService(context);
			}
		}
	}

}