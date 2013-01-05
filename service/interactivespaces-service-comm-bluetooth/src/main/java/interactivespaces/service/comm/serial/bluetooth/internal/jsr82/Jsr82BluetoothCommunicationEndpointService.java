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

package interactivespaces.service.comm.serial.bluetooth.internal.jsr82;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.bluetooth.BluetoothCommunicationEndpoint;
import interactivespaces.service.comm.serial.bluetooth.BluetoothCommunicationEndpointService;
import interactivespaces.service.comm.serial.bluetooth.BluetoothDevice;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import com.google.common.collect.Lists;
import com.intel.bluetooth.BlueCoveConfigProperties;

/**
 * A bluetooth connection service using Jsr82.
 * 
 * @author Keith M. Hughes
 * @since Dec 21, 2012
 */
public class Jsr82BluetoothCommunicationEndpointService implements
		BluetoothCommunicationEndpointService {

	/**
	 * Space environment for this service.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		System.setProperty(
				BlueCoveConfigProperties.PROPERTY_JSR_82_PSM_MINIMUM_OFF,
				"true");
	}

	@Override
	public void shutdown() {
		// Nothing to do for now
	}

	@Override
	public BluetoothDevice getLocalBluetoothInformation() {
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();

			String bluetoothAddress = localDevice.getBluetoothAddress();
			String friendlyName = localDevice.getFriendlyName();

			DeviceClass dc = localDevice.getDeviceClass();

			return new BluetoothDevice(bluetoothAddress, friendlyName,
					dc.getMajorDeviceClass(), dc.getMinorDeviceClass());
		} catch (BluetoothStateException e) {
			throw new InteractiveSpacesException(
					"Could not obtain local bluetooth information", e);
		}
	}

	@Override
	public List<BluetoothDevice> discoverRemoteDevices() {
		final List<BluetoothDevice> devices = Lists.newArrayList();

		try {
			final CountDownLatch done = new CountDownLatch(1);

			LocalDevice localDevice = LocalDevice.getLocalDevice();

			DiscoveryAgent agent = localDevice.getDiscoveryAgent();

			spaceEnvironment.getLog().info("Starting device inquiry");

			boolean started = agent.startInquiry(DiscoveryAgent.GIAC,
					new DiscoveryListener() {

						@Override
						public void deviceDiscovered(RemoteDevice device,
								DeviceClass deviceClass) {
							try {
								devices.add(new BluetoothDevice(device
										.getBluetoothAddress(), device
										.getFriendlyName(true), deviceClass
										.getMajorDeviceClass(), deviceClass
										.getMinorDeviceClass()));
							} catch (IOException e) {
								spaceEnvironment
										.getLog()
										.error("Error during bluetooth device discover",
												e);
							}

						}

						@Override
						public void inquiryCompleted(int discType) {
							switch (discType) {

							case DiscoveryListener.INQUIRY_COMPLETED:

								spaceEnvironment.getLog().info(
										"Bluetooth discover inquiry completed");

								break;

							case DiscoveryListener.INQUIRY_TERMINATED:

								spaceEnvironment
										.getLog()
										.info("Bluetooth discover inquiry terminated");

								break;

							case DiscoveryListener.INQUIRY_ERROR:

								spaceEnvironment.getLog().error(
										"Bluetooth discover inquiry error");

								break;

							default:

								spaceEnvironment
										.getLog()
										.warn("Bluetooth discover inquiry unknown response code");

								break;

							}
							done.countDown();
						}

						@Override
						public void serviceSearchCompleted(int arg0, int arg1) {
							spaceEnvironment.getLog().info(
									"Bluetooth service search completed.");
						}

						@Override
						public void servicesDiscovered(int arg0,
								ServiceRecord[] arg1) {
							spaceEnvironment.getLog().info(
									"Bluetooth service discovered.");
						}
					});

			done.await();

			return devices;
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Error during bluetooth discovery", e);
		}
	}

	@Override
	public BluetoothCommunicationEndpoint newDualEndpoint(String address,
			int receivePort, int sendPort) {
		return new Jsr82MultiPortBluetoothCommunicationEndpoint(address,
				receivePort, sendPort);
	}

	@Override
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
