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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A connection of methods to control the Interactive Spaces service and
 * controller.
 * 
 * @author Keith M. Hughes
 */
public class AndroidInteractiveSpacesEnvironment {

	/**
	 * Start up the Interactive Spaces service if necessary.
	 * 
	 * <p>
	 * Does nothing if the service is already running.
	 * 
	 * @param context
	 *            context of whoever is trying to start the service
	 */
	public static void startInteractiveSpacesService(Context context) {
		// Immediately start the foreground service
		Intent serviceIntent = new Intent(context,
				InteractiveSpacesService.class);
		context.startService(serviceIntent);
	}

	/**
	 * Stop the Interactive Spaces service.
	 * 
	 * <p>
	 * This does nothing if the service isn't running.
	 */
	public static void stopInteractiveSpacesService(Context context) {
		// Immediately stop the foreground service
		Intent serviceIntent = new Intent(context,
				InteractiveSpacesService.class);
		context.stopService(serviceIntent);
	}

	/**
	 * Discover the IP address of the Android device.
	 * 
	 * @return The IP address, if connected to the network, or {@code null}.
	 */
	public static String getLocalIpAddress() {
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

		return null;
	}

}
