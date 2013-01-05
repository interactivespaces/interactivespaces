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

import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * The root Android Activity for controlling Interactive Spaces.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesRootActivity extends Activity {

	/**
	 * Key in the intent extras for error messages.
	 */
	public static final String INTENT_EXTRA_ERROR_MESSAGE = "error.message";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean autostart = prefs.getBoolean("PREF_START_ON_LAUNCH", false);

		ensureUuid(prefs);

		if (autostart) {
			Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
					"Autostarting Interactive Spaces controller on launch");
			AndroidInteractiveSpacesEnvironment
					.startInteractiveSpacesService(this);
		}
	}

	/**
	 * Ensure there is a UUID for the controller.
	 * 
	 * @param prefs
	 *            the shared preferences for the application
	 */
	private void ensureUuid(SharedPreferences prefs) {
		String uuid = prefs.getString(
				AndroidConfigurationProvider.PREF_CONTROLLER_UUID, null);
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			Log.i(AndroidLoggingProvider.BASE_LOG_NAME,
					String.format("No UUID, generated %s", uuid));
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(AndroidConfigurationProvider.PREF_CONTROLLER_UUID,
					uuid);

			// Writing synchronously since want soon.
			editor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateView(this);

		Intent i = getIntent();
		String errorMessage = i.getStringExtra(INTENT_EXTRA_ERROR_MESSAGE);
		if (errorMessage != null && !errorMessage.isEmpty()) {
			i.putExtra(INTENT_EXTRA_ERROR_MESSAGE, (String) null);

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Cannot start Interactive Spaces");
			alert.setMessage(errorMessage);

			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			alert.create().show();
		}

	}

	/**
	 * Update the view
	 */
	public void updateView(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		TextView interactivespacesVersionView = (TextView) findViewById(R.id.myInteractivespacesVersion);
		try {
			PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageInfo(
					"interactivespaces.controller.android", 0);
			interactivespacesVersionView.setText(pi.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextView deviceSerialView = (TextView) findViewById(R.id.myDeviceSerial);
		deviceSerialView.setText(Settings.Secure.getString(
				context.getContentResolver(), Settings.Secure.ANDROID_ID));

		WifiManager wifiMan = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		String macAddr = wifiInf.getMacAddress();

		TextView deviceMacView = (TextView) findViewById(R.id.myDeviceMac);
		deviceMacView.setText(macAddr);

		TextView ipView = (TextView) findViewById(R.id.myDeviceIp);
		String localIpAddress = AndroidInteractiveSpacesEnvironment
				.getLocalIpAddress();
		if (localIpAddress == null) {
			localIpAddress = "Device not connected";
		}
		ipView.setText(localIpAddress);

		TextView controllerHostView = (TextView) findViewById(R.id.myControllerHost);
		controllerHostView.setText(prefs
				.getString(AndroidConfigurationProvider.PREF_CONTROLLER_HOST,
						"?required?"));

		TextView hostIdView = (TextView) findViewById(R.id.myHostId);
		hostIdView.setText(prefs.getString(
				AndroidConfigurationProvider.PREF_HOST_ID, "?required?"));

		TextView controllerUuidView = (TextView) findViewById(R.id.myControllerUuid);
		controllerUuidView.setText(prefs
				.getString(AndroidConfigurationProvider.PREF_CONTROLLER_UUID,
						"?required?"));

		TextView controllerNameView = (TextView) findViewById(R.id.myControllerName);
		controllerNameView.setText(prefs
				.getString(AndroidConfigurationProvider.PREF_CONTROLLER_NAME,
						"?required?"));

		TextView controllerDescriptionView = (TextView) findViewById(R.id.myControllerDescription);
		controllerDescriptionView.setText(prefs.getString(
				AndroidConfigurationProvider.PREF_CONTROLLER_DESCRIPTION,
				"?required?"));

		TextView masterHostView = (TextView) findViewById(R.id.myMasterHost);
		masterHostView.setText(prefs.getString(
				AndroidConfigurationProvider.PREF_MASTER_HOST, "?required?"));

		TextView masterPortView = (TextView) findViewById(R.id.myMasterPort);
		masterPortView.setText(prefs.getString(
				AndroidConfigurationProvider.PREF_MASTER_PORT, "?required?"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);

		return true;
	}

	public boolean onMenuStartController(MenuItem item) {
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME, "Starting controller yowza");

		AndroidInteractiveSpacesEnvironment.startInteractiveSpacesService(this);

		return true;
	}

	public boolean onMenuStopController(MenuItem item) {
		Log.i(AndroidLoggingProvider.BASE_LOG_NAME, "Stopping controller yowza");

		AndroidInteractiveSpacesEnvironment.stopInteractiveSpacesService(this);

		return true;
	}

	public boolean onMenuSettings(MenuItem item) {
		Intent i = new Intent(this, MyFragmentPreferencesActivity.class);
		startActivityForResult(i, 1);

		return true;
	}

}
