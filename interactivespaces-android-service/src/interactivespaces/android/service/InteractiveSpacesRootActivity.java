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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		AndroidInteractiveSpacesEnvironment.startInteractiveSpacesService(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateView(this);
	}

	/**
	 * Update the view
	 */
	public void updateView(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

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
		ipView.setText(getLocalIpAddress());

		TextView controllerHostView = (TextView) findViewById(R.id.myControllerHost);
		controllerHostView
				.setText(prefs.getString("PREF_CONTROLLER_HOST", "?"));

		TextView masterHostView = (TextView) findViewById(R.id.myMasterHost);
		masterHostView.setText(prefs.getString("PREF_MASTER_HOST", "?"));

		TextView masterPortView = (TextView) findViewById(R.id.myMasterPort);
		masterPortView.setText(prefs.getString("PREF_MASTER_PORT", "?"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);

		return true;
	}

	public boolean onMenuStartController(MenuItem item) {
		Log.i("IS", "Starting controller yowza");

		return true;
	}

	public boolean onMenuSettings(MenuItem item) {
		Intent i = new Intent(this, MyFragmentPreferencesActivity.class);
		startActivityForResult(i, 1);

		return true;
	}

	public String getLocalIpAddress() {
		try {
			String ipv4;
			List<NetworkInterface> nilist = Collections.list(NetworkInterface
					.getNetworkInterfaces());
			for (NetworkInterface ni : nilist) {
				List<InetAddress> ialist = Collections.list(ni
						.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ipv4 = address
									.getHostAddress())) {
						return ipv4;
					}
				}

			}

		} catch (SocketException ex) {
			Log.e("interactivespaces", ex.toString());
		}
		return "Device IP Unknown";
	}

}
