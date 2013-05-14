/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.comm.usb.internal.libusb4j;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.usb.UsbCommunicationEndpoint;

import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbPipe;
import javax.usb.util.UsbUtil;

/**
 * A {UsbCommunicationEndpoint} which uses USB4Java.
 * 
 * @author Keith M. Hughes
 */
public class Usb4JavaUsbCommunicationEndpoint implements UsbCommunicationEndpoint {

	/**
	 * A USB interface policy that always claims a device, no matter what.
	 */
	private static UsbInterfacePolicy FORCE_CLAIM_POLICY = new UsbInterfacePolicy() {

		@Override
		public boolean forceClaim(UsbInterface arg0) {
			return true;
		}

	};

	/**
	 * The USB device for this endpoint.
	 */
	private UsbDevice device;

	/**
	 * The size of the read buffer, in bytes.
	 */
	private int readBufferSize;

	/**
	 * The USB interface being used.
	 */
	private UsbInterface usbInterface;

	/**
	 * The USB pip for communication.
	 */
	private UsbPipe usbPipe;
	
	/**
	 * Which endpoint from the device to use.
	 */
	private int endpointIndex;

	public Usb4JavaUsbCommunicationEndpoint(UsbDevice device) {
		this.device = device;
	}

	@Override
	public void setEndpointIndex(int endpointIndex) {
		this.endpointIndex = endpointIndex;
	}

	@Override
	public void startup() {
		try {
			UsbConfiguration conf = device.getActiveUsbConfiguration();
			@SuppressWarnings("unchecked")
			List<UsbInterface> interfaces = (List<UsbInterface>) conf
					.getUsbInterfaces();
			System.out.println("Got interfaces " + interfaces.size());
			for (UsbInterface i : interfaces) {
				i.claim(FORCE_CLAIM_POLICY);
				usbInterface = i;

				try {
					@SuppressWarnings("unchecked")
					List<UsbEndpoint> endpoints = (List<UsbEndpoint>) usbInterface
							.getUsbEndpoints();
					UsbEndpoint endpoint = endpoints.get(endpointIndex);

					usbPipe = endpoint.getUsbPipe();
					usbPipe.open();

					readBufferSize = UsbUtil.unsignedInt(usbPipe
							.getUsbEndpoint().getUsbEndpointDescriptor()
							.wMaxPacketSize());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		try {
			if (usbInterface != null) {
				usbInterface.release();
			}
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not shut USB interface down", e);
		}
	}

	@Override
	public byte[] newBuffer() {
		return new byte[readBufferSize];
	}

	@Override
	public int getReadBufferSize() {
		return readBufferSize;
	}
	
	@Override
	public int readReportSync(byte[] buffer) {
		try {
			return usbPipe.syncSubmit(buffer);
		} catch (Exception e) {
			throw new InteractiveSpacesException("Could not read USB device", e);
		}
	}
}
