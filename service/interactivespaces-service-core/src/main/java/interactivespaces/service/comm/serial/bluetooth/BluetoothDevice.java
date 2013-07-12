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

package interactivespaces.service.comm.serial.bluetooth;

/**
 * Information about a bluetooth device.
 *
 * @author Keith M. Hughes
 */
public class BluetoothDevice {

  /**
   * Bluetooth address of the device.
   */
  private String address;

  /**
   * Friendly name of the device.
   *
   */
  private String friendlyName;

  /**
   * The major class for the device.
   */
  private int majorDeviceClass;

  /**
   * The minor class for the device.
   */
  private int minorDeviceClass;

  public BluetoothDevice(String address, String friendlyName, int majorDeviceClass,
      int minorDeviceClass) {
    this.address = address;
    this.friendlyName = friendlyName;
    this.majorDeviceClass = majorDeviceClass;
    this.minorDeviceClass = minorDeviceClass;
  }

  /**
   * Get the bluetooth address of the device.
   *
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Get the friendly name of the bluetooth device.
   *
   * @return the friendlyName
   */
  public String getFriendlyName() {
    return friendlyName;
  }

  /**
   * Get the major class of the device.
   *
   * @return the majorDeviceClass
   */
  public int getMajorDeviceClass() {
    return majorDeviceClass;
  }

  /**
   * Get the minor class of the device.
   *
   * @return the minorDeviceClass
   */
  public int getMinorDeviceClass() {
    return minorDeviceClass;
  }

  @Override
  public String toString() {
    return "BluetoothDevice [address=" + address + ", friendlyName=" + friendlyName
        + ", majorDeviceClass=" + majorDeviceClass + ", minorDeviceClass=" + minorDeviceClass + "]";
  }
}
