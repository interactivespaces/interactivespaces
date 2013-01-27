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

package interactivespaces.service.comm.serial.xbee;

/**
 * A response frame for an XBee remote AT request.
 * 
 * @author Keith M. Hughes
 */
public interface AtRemoteResponseXBeeFrame {

	/**
	 * @return the frameId
	 */
	int getFrameId();

	/**
	 * Get the 64 bit address of the sender of the AT Remote response.
	 * 
	 * @return the sender's 64 bit address
	 */
	XBeeAddress64 getAddress64();

	/**
	 * Get the 16 bit address of the sender of the AT Remote response.
	 * 
	 * @return the sender's 16 bit address
	 */
	XBeeAddress16 getAddress16();

	/**
	 * @return the atCommand
	 */
	byte[] getAtCommand();

	/**
	 * Get the AT command as a string.
	 * 
	 * @return the AT command
	 */
	String getAtCommandString();

	/**
	 * @return the commandStatus
	 */
	int getCommandStatus();

	/**
	 * Was the command successful?
	 * 
	 * @return {@code true} if successful
	 */
	boolean isSuccess();

	/**
	 * @return the commandData
	 */
	byte[] getCommandData();
}