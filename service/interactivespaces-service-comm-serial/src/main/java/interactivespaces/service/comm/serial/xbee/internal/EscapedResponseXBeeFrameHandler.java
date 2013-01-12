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

import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListener;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * A parser that parses escaped API XBee frames.
 * 
 * @author Keith M. Hughes
 */
public class EscapedResponseXBeeFrameHandler implements
		ResponseXBeeFrameHandler {

	@Override
	public void handle(XBeeCommunicationEndpoint endpoint, byte[] frame,
			List<XBeeResponseListener> listeners, Log log) {
		switch ((int) (frame[0] & 0xff)) {
		case XBeeApiConstants.FRAME_TYPE_AT_LOCAL_RESPONSE:
			parseAtLocalResponse(frame, listeners, log);
			break;
		case XBeeApiConstants.FRAME_TYPE_AT_REMOTE_RESPONSE:
			signalAtRemoteResponse(endpoint, parseAtRemoteResponse(frame, log),
					listeners, log);
			break;
		case XBeeApiConstants.FRAME_TYPE_TX_RESPONSE:
			signalTxResponse(endpoint, parseTxResponse(frame, log), listeners,
					log);
			break;
		case XBeeApiConstants.FRAME_TYPE_RX_RECEIVE:
			signalRxResponse(endpoint, parseReceiveResponse(frame, log),
					listeners, log);
			break;
		default:
			log.warn(String.format("Unknown frame type %d\n", frame[0]));
		}

		// for (int i = 0; i < frame.length; i++) {
		// System.out.println(Integer.toHexString(frame[i] & 0xff));
		// }
	}

	/**
	 * Parse an AT Local response.
	 * 
	 * @param frame
	 *            the raw bytes of the frame
	 * @param listeners
	 *            the listeners for the response
	 * @param log
	 *            the logger for issues
	 */
	private void parseAtLocalResponse(byte[] frame,
			List<XBeeResponseListener> listeners, Log log) {
		System.out.println("AT Local Response");
	}

	/**
	 * Parse an AT Remote response.
	 * 
	 * @param frame
	 *            the raw bytes of the frame
	 * @param log
	 *            the logger for issues
	 */
	public AtRemoteResponseXBeeFrame parseAtRemoteResponse(byte[] frame, Log log) {
		log.info("Received AT Remote Response");

		int frameId = getIntFromByte(frame[1]);

		XBeeAddress64 address64 = parseXBeeAddress64(frame, 2);
		XBeeAddress16 address16 = parseXBeeAddress16(frame, 10);

		AtRemoteResponseXBeeFrame response = new AtRemoteResponseXBeeFrame(
				frameId, address64, address16);

		System.out.println(response);
		return response;
	}

	/**
	 * Parse an Transmit response.
	 * 
	 * @param frame
	 *            the raw bytes of the frame
	 * @param log
	 *            the logger for issues
	 * 
	 * @return the response frame
	 */
	public TxResponseXBeeFrame parseTxResponse(byte[] frame, Log log) {
		log.info("Received TX Response");

		int frameId = getIntFromByte(frame[1]);
		XBeeAddress16 address16 = parseXBeeAddress16(frame, 2);
		int transmitRetryCount = getIntFromByte(frame[4]);
		int deliveryStatus = getIntFromByte(frame[5]);
		int discoveryStatus = getIntFromByte(frame[6]);

		return new TxResponseXBeeFrame(frameId, address16, transmitRetryCount,
				deliveryStatus, discoveryStatus);
	}

	/**
	 * Parse an Receive response.
	 * 
	 * @param frame
	 *            the raw bytes of the frame
	 * @param log
	 *            the logger for issues
	 */
	public RxResponseXBeeFrame parseReceiveResponse(byte[] frame, Log log) {
		log.info("RX Receive");

		XBeeAddress64 address64 = parseXBeeAddress64(frame, 1);
		XBeeAddress16 address16 = parseXBeeAddress16(frame, 9);

		return new RxResponseXBeeFrame(address64, address16);
	}

	/**
	 * Get an XBee 16 bit address from the frame
	 * 
	 * @param frame
	 *            the raw frame data
	 * @param pos
	 *            position to start reading the address
	 * 
	 * @return the XBee 64 bit address
	 */
	private XBeeAddress64 parseXBeeAddress64(byte[] frame, int pos) {
		return new XBeeAddress64((int) frame[pos] & 0xff,
				(int) frame[pos + 1] & 0xff, (int) frame[pos + 2] & 0xff,
				(int) frame[pos + 3] & 0xff, (int) frame[pos + 4] & 0xff,
				(int) frame[pos + 5] & 0xff, (int) frame[pos + 6] & 0xff,
				(int) frame[pos + 7] & 0xff);
	}

	/**
	 * Get an XBee 16 bit address from the frame
	 * 
	 * @param frame
	 *            the raw frame data
	 * @param pos
	 *            position to start reading the address
	 * 
	 * @return the XBee 16 bit address
	 */
	private XBeeAddress16 parseXBeeAddress16(byte[] frame, int pos) {
		return new XBeeAddress16(getIntFromByte(frame[pos]),
				getIntFromByte(frame[pos + 1]));
	}

	/**
	 * Create an int from a byte, making sure there is no sign extension
	 * 
	 * @param b
	 *            the byte
	 * 
	 * @return the byte made into an int
	 */
	private int getIntFromByte(byte b) {
		return (int) b & 0xff;
	}

	/**
	 * Signal an AT remote response.
	 * 
	 * @param endpoint
	 *            the endpoint that received the response
	 * @param response
	 *            the response
	 * @param listeners
	 *            the listeners
	 * @param log
	 *            logger for errors
	 */
	private void signalAtRemoteResponse(XBeeCommunicationEndpoint endpoint,
			AtRemoteResponseXBeeFrame response,
			List<XBeeResponseListener> listeners, Log log) {
		for (XBeeResponseListener listener : listeners) {
			try {
				listener.onAtRemoteXBeeResponse(endpoint, response);
			} catch (Exception e) {
				log.error(
						"Error during call to listener for XBee AT Remote reponse",
						e);
			}
		}
	}

	/**
	 * Signal an RX response.
	 * 
	 * @param endpoint
	 *            the endpoint that received the response
	 * @param response
	 *            the response
	 * @param listeners
	 *            the listeners
	 * @param log
	 *            logger for errors
	 */
	private void signalRxResponse(XBeeCommunicationEndpoint endpoint,
			RxResponseXBeeFrame response, List<XBeeResponseListener> listeners,
			Log log) {
		for (XBeeResponseListener listener : listeners) {
			try {
				listener.onRxXBeeResponse(endpoint, response);
			} catch (Exception e) {
				log.error("Error during call to listener for XBee RX response",
						e);
			}
		}
	}

	/**
	 * Signal a TX response.
	 * 
	 * @param endpoint
	 *            the endpoint that received the response
	 * @param response
	 *            the response
	 * @param listeners
	 *            the listeners
	 * @param log
	 *            logger for errors
	 */
	private void signalTxResponse(XBeeCommunicationEndpoint endpoint,
			TxResponseXBeeFrame response, List<XBeeResponseListener> listeners,
			Log log) {
		for (XBeeResponseListener listener : listeners) {
			try {
				listener.onTxResponseXBeeResponse(endpoint, response);
			} catch (Exception e) {
				log.error("Error during call to listener for XBee TX response",
						e);
			}
		}
	}
}
