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

package interactivespaces.comm.serial.bluetooth;

import interactivespaces.comm.CommunicationEndpoint;

/**
 * A communication endpoint for bluetooth.
 * 
 * @author Keith M. Hughes
 */
public interface BluetoothCommunicationEndpoint extends CommunicationEndpoint {

	/**
	 * Connect to the remote endpoint.
	 */
	void connect();

	/**
	 * Shut down the connection.
	 */
	void shutdown();

	/**
	 * Send the given bytes to the connection.
	 * 
	 * @param data
	 *            the bytes to send
	 */
	void write(byte[] data);

	/**
	 * Receive bytes from the connection.
	 * 
	 * @param buffer
	 *            where the bytes read will be
	 * 
	 * @return the number of bytes received, or {@code -1} if nothing more
	 */
	int read(byte[] data);
}
