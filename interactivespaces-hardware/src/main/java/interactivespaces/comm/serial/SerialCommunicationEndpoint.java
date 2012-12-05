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

package interactivespaces.comm.serial;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * An endpoint for serial communication.
 * 
 * @author Keith M. Hughes
 */
public interface SerialCommunicationEndpoint {

	/**
	 * Start the endpoint up.
	 */
	void connect();

	/**
	 * Shut down all open serial ports managed by this endpoint.
	 * 
	 * <p>
	 * The node is left running.
	 */
	void shutdown();

	/**
	 * Get the input stream associated with the port.
	 * 
	 * @return the input stream
	 */
	InputStream getInputStream();

	/**
	 * Get the input stream associated with the port.
	 * 
	 * @return the output stream
	 */
	OutputStream getOutputStream();

	/**
	 * Set the baud rate of the connection.
	 * 
	 * @param baud
	 *            the baud rate
	 * 
	 * @return the endpoint
	 */
	SerialCommunicationEndpoint setBaud(int baud);

	/**
	 * Set the number of data bits for the connection.
	 * 
	 * @param dataBits
	 *            the number of data bits
	 * 
	 * @return the endpoint
	 */
	SerialCommunicationEndpoint setDataBits(int dataBits);

	/**
	 * Set the number of stop bits for the connection.
	 * 
	 * @param stopBits
	 *            the number of stop bits
	 * 
	 * @return the endpoint
	 */
	SerialCommunicationEndpoint setStopBits(int stopBits);

	/**
	 * Set the type of parity for the connection.
	 * 
	 * @param parity
	 *            the type of parity
	 * 
	 * @return the endpoint
	 */
	SerialCommunicationEndpoint setParity(Parity parity);
	
	/**
	 * Types of parity.
	 *
	 * @author Keith M. Hughes
	 */
	public enum Parity {
		NONE,
		EVEN,
		ODD,
		SPACE,
		MARK
	}
}