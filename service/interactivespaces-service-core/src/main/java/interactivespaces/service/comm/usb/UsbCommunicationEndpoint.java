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

package interactivespaces.service.comm.usb;

import interactivespaces.util.resource.ManagedResource;

/**
 * An endpoint for USB bus device communication.
 *
 * @author Keith M. Hughes
 */
public interface UsbCommunicationEndpoint extends ManagedResource {

  /**
   * Set the endpoint index for the device on the port.
   *
   * <p>
   * Must be called before {@link #startup()} and has no effect if called after.
   *
   * @param endpointIndex
   *          endpoint index (first one starts with {@code 0}
   */
  void setEndpointIndex(int endpointIndex);

  /**
   * Create a buffer of the max size required by the USB device.
   *
   * @return the buffer.
   */
  byte[] newBuffer();

  /**
   * Get the max size a read buffer should be according to the device's
   * description.
   *
   * @return the number of bytes for the max sized buffer
   */
  int getReadBufferSize();

  /**
   * Read the next report from the device.
   *
   * @param buffer
   *          the buffer to read the report into
   *
   * @return the number of bytes read
   */
  int readReportSync(byte[] buffer);
}