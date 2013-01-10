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

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.internal.rxtx.RxtxSerialCommunicationEndpoint;

/**
 * An nteractive Spaces implementation of an XBee communication endpoint.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesXbeeCommunicationEndpoint implements
		XBeeCommunicationEndpoint {
	
	/**
	 * The value of the start byte for an XBee API frame.
	 */
	public static final int FRAME_START_BYTE = 0x7e;
	
	/**
	 * XBee API command for sending a local AT command.
	 */
	private static final int COMMAND_AT_LOCAL_SEND = 0x08;
	
	/**
	 * XBee API command for a response for a local AT command.
	 */
	private static final int COMMAND_AT_LOCAL_RESPONSE = 0x88;

	public static void main(String[] args) {
		InteractiveSpacesXbeeCommunicationEndpoint endpoint = null;
		try {
			endpoint = new InteractiveSpacesXbeeCommunicationEndpoint();
			endpoint.startup();
			
			endpoint.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (endpoint != null) {
				endpoint.shutdown();
			}
		}
	}

	/**
	 * The communication endpoint for speaking with the XBee.
	 */
	private SerialCommunicationEndpoint commEndpoint;

	@Override
	public void startup() {
		commEndpoint = new RxtxSerialCommunicationEndpoint("/dev/ttyUSB0");
		commEndpoint.startup();
	}

	@Override
	public void shutdown() {
		if (commEndpoint != null) {
			commEndpoint.shutdown();
			commEndpoint = null;
		}
	}
	
	public void test() {
		FrameWriter content = new FrameWriter();
		
		content.add(COMMAND_AT_LOCAL_SEND);
		content.add(0x11);
		
		content.add(0x53);
		content.add(0x4c);
		
		content.write(commEndpoint);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int avail = commEndpoint.available();
		System.out.println("Avail " + avail);
		
		byte[] result = new byte[avail];
		commEndpoint.read(result);
		
		for (int i = 0; i < result.length; i++) {
			System.out.println(Integer.toHexString(result[i]));
		}
		
	}

	private static class FrameWriter {
		
		/**
		 * Where bytes are written for the frame
		 */
		private ByteOutputStream bos = new ByteOutputStream();
		
		private int checksum = 0;
		private int length = 0;
		
		public FrameWriter() {
			bos.write(FRAME_START_BYTE);
			
			// Save room for length
			bos.write(0x00);
			bos.write(0x00);
		}
		
		public void add(int b) {
			bos.write(b);
			checksum += b;
			length++;
		}
		
		public void write(SerialCommunicationEndpoint commEndpoint) {
			checksum &= 0xff;
			checksum = 0xff - checksum;
			bos.write(checksum);
			
			byte[] bytes = bos.getBytes();
			
			// Get proper length into the frame.
			bytes[1] = (byte)((length >> 8) & 0xff);
			bytes[2] = (byte)(length & 0xff);
			
			commEndpoint.write(bytes);
			commEndpoint.flush();
		}
	}
	
	
}
