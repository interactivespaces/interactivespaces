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

package interactivespaces.service.comm.serial.xbee.internal;

/**
 * A response frame for a remote AT request.
 * 
 * @author Keith M. Hughes
 */
public class AtRemoteResponseXBeeFrame {

	/**
	 * The frame ID for the response.
	 */
	private int frameId;

	/**
	 * The 64 bit address for the response
	 */
	private XBeeAddress64 address64;

	/**
	 * The 16 bit address for the response
	 */
	private XBeeAddress16 address16;

	/**
	 * The AT command.
	 */
	private byte[] atCommand;
	
	/**
	 * The AT command as a string.
	 */
	private String atCommandString;

	/**
	 * The status of the command.
	 */
	private int commandStatus;

	/**
	 * Data from the command.
	 */
	private byte[] commandData;

	public AtRemoteResponseXBeeFrame(int frameId, XBeeAddress64 address64,
			XBeeAddress16 address16, byte[] atCommand, int commandStatus,
			byte[] commandData) {
		this.frameId = frameId;
		this.address64 = address64;
		this.address16 = address16;
		this.atCommand = atCommand;
		this.commandStatus = commandStatus;
		this.commandData = commandData;

		atCommandString = new String(atCommand);
	}

	/**
	 * @return the frameId
	 */
	public int getFrameId() {
		return frameId;
	}

	/**
	 * Get the 64 bit address of the sender of the AT Remote response.
	 * 
	 * @return the sender's 64 bit address
	 */
	public XBeeAddress64 getAddress64() {
		return address64;
	}

	/**
	 * Get the 16 bit address of the sender of the AT Remote response.
	 * 
	 * @return the sender's 16 bit address
	 */
	public XBeeAddress16 getAddress16() {
		return address16;
	}

	/**
	 * @return the atCommand
	 */
	public byte[] getAtCommand() {
		return atCommand;
	}

	/**
	 * Get the AT command as a string.
	 * 
	 * @return the AT command
	 */
	public String getAtCommandString() {
		return atCommandString;
	}

	/**
	 * @return the commandStatus
	 */
	public int getCommandStatus() {
		return commandStatus;
	}
	
	/**
	 * Was the command successful?
	 * 
	 * @return {@code true} if successful
	 */
	public boolean isSuccess() {
		return commandStatus == XBeeApiConstants.AT_COMMAND_STATUS_SUCCESS;
	}

	/**
	 * @return the commandData
	 */
	public byte[] getCommandData() {
		return commandData;
	}

	@Override
	public String toString() {
		return "AtRemoteResponseXBeeFrame [frameId=" + frameId + ", address64="
				+ address64 + ", address16=" + address16 + ", atCommand="
				+ atCommandString + ", commandStatus=" + commandStatus + "]";
	}
}
