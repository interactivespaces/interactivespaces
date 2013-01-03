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

package interactivespaces.service.comm.serial.bluetooth.jsr82;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.bluetooth.BluetoothCommunicationEndpoint;
import interactivespaces.service.comm.serial.bluetooth.BluetoothCommunicationEndpointService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
public class Jsr82BluetoothCommunicationEndpointService implements BluetoothCommunicationEndpointService {
	
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

	public List<RemoteDevice> getDevices() {
		final List<RemoteDevice> devices = Lists.newArrayList();

		try {
			final CountDownLatch done = new CountDownLatch(1);

			LocalDevice localDevice = LocalDevice.getLocalDevice();

			System.out.println("Address: " + localDevice.getBluetoothAddress());

			System.out.println("Name: " + localDevice.getFriendlyName());

			DiscoveryAgent agent = localDevice.getDiscoveryAgent();

			System.out.println("Starting device inquiry…");

			boolean blah = agent.startInquiry(DiscoveryAgent.GIAC,
					new DiscoveryListener() {

						@Override
						public void deviceDiscovered(RemoteDevice device,
								DeviceClass deviceClass) {
							try {
								System.out.println("Device discovered: "
										+ device.getBluetoothAddress());
								System.out.println(device.getFriendlyName(true));
								devices.add(device);
								if (deviceClass.getMajorDeviceClass() == 1280
										&& deviceClass.getMinorDeviceClass() == 4) {
									System.out.println("Is a Wii remote!!");
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

						@Override
						public void inquiryCompleted(int discType) {
							switch (discType) {

							case DiscoveryListener.INQUIRY_COMPLETED:

								System.out.println("INQUIRY_COMPLETED");

								break;

							case DiscoveryListener.INQUIRY_TERMINATED:

								System.out.println("INQUIRY_TERMINATED");

								break;

							case DiscoveryListener.INQUIRY_ERROR:

								System.out.println("INQUIRY_ERROR");

								break;

							default:

								System.out.println("Unknown Response Code");

								break;

							}
							done.countDown();
						}

						@Override
						public void serviceSearchCompleted(int arg0, int arg1) {
							System.out.println("Service complete");
						}

						@Override
						public void servicesDiscovered(int arg0,
								ServiceRecord[] arg1) {
							System.out.println("Service discovered");
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
		return new Jsr82MultiPortBluetoothCommunicationEndpoint(address, receivePort, sendPort);
	}

	@Override
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
