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
 * ABee response frame for AT Local calls.
 * 
 * @author Keith M. Hughes
 */
public class AtLocalResponseXBeeFrame {

	/**
	 * ID for the frame.
	 */
	private int frameId;

	/**
	 * The AT command.
	 */
	private byte[] atCommand;

	/**
	 * The AT command as a string.
	 */
	private String atCommandString;

	/**
	 * The status of the atCommand.
	 */
	private int commandStatus;

	/**
	 * The commandData for the atCommand.
	 */
	private byte[] commandData;

	public AtLocalResponseXBeeFrame(int frameId, byte[] atCommand,
			int commandStatus, byte[] commandData) {
		this.frameId = frameId;
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
	 * @return the atCommand
	 */
	public byte[] getAtCommand() {
		return atCommand;
	}

	/**
	 * @return the commandStatus
	 */
	public int getCommandStatus() {
		return commandStatus;
	}

	/**
	 * @return the commandData
	 */
	public byte[] getCommandData() {
		return commandData;
	}
	
	/**
	 * Was the command successful?
	 * 
	 * @return {@code true} if successful
	 */
	public boolean isSuccess() {
		return commandStatus == XBeeApiConstants.AT_COMMAND_STATUS_SUCCESS;
	}

	@Override
	public String toString() {
		return "AtLocalResponseXBeeFrame [frameId=" + frameId + ", atCommand="
				+ atCommandString + ", commandStatus=" + commandStatus + "]";
	}
}
