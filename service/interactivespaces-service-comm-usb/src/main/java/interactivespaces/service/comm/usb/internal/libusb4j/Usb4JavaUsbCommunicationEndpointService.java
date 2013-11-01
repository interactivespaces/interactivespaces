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
import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.usb.UsbCommunicationEndpoint;
import interactivespaces.service.comm.usb.UsbCommunicationEndpointService;

import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

/**
 *
 * A {@link UsbCommunicationEndpointService} using Usb4Java.
 *
 * @author Keith M. Hughes
 */
public class Usb4JavaUsbCommunicationEndpointService extends BaseSupportedService implements
    UsbCommunicationEndpointService {

  /**
   * The root USB hub for the host computer
   */
  private UsbHub rootHub;

  @Override
  public String getName() {
    return UsbCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    try {
      System.setProperty("javax.usb.services", "de.ailis.usb4java.Services");
      UsbServices services = UsbHostManager.getUsbServices();
      rootHub = services.getRootUsbHub();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not obtain the USB Root Hub", e);
    }
  }

  @Override
  public UsbCommunicationEndpoint newEndpoint(String vendor, String product) {
    UsbDevice device =
        findDevice(Integer.valueOf(vendor, 16), Integer.valueOf(product, 16), rootHub);

    if (device != null) {
      return new Usb4JavaUsbCommunicationEndpoint(device);
    } else {
      return null;
    }
  }

  /**
   * Find the specified device.
   *
   * <p>
   * This method will look at child devices of hubs in effort to find the
   * specified device.
   *
   * @param vendor
   *          the vendor ID of the requested device
   * @param product
   *          the product ID of the requested device
   * @param currDevice
   *          the current device being examined
   *
   * @return the requested device or {@code null} if not found
   */
  private UsbDevice findDevice(int vendor, int product, UsbDevice currDevice) {
    if (currDevice.isUsbHub()) {
      UsbHub hub = (UsbHub) currDevice;
      @SuppressWarnings("unchecked")
      List<UsbDevice> attachedUsbDevices = hub.getAttachedUsbDevices();
      for (UsbDevice child : attachedUsbDevices) {
        UsbDevice possible = findDevice(vendor, product, child);
        if (possible != null) {
          return possible;
        }
      }
    } else {
      UsbDeviceDescriptor desc = currDevice.getUsbDeviceDescriptor();
      if ((desc.idVendor() & 0xffff) == vendor && (desc.idProduct() & 0xffff) == product) {
        return currDevice;
      }
    }

    // Nothing matched at this level
    return null;
  }
}
