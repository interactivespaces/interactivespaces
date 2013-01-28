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

import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.AtRemoteResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.TxStatusXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListener;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * A parser that parses escaped API XBee frames.
 * 
 * @author Keith M. Hughes
 */
public class SimpleResponseXBeeFrameHandler implements ResponseXBeeFrameHandler {

	/**
	 * The frame parser for this handler.
	 */
	private XBeeResponseFrameParser parser = new SimpleXBeeResponseFrameParser();

	@Override
	public void handle(XBeeCommunicationEndpoint endpoint,
			EscapedXBeeFrameReader reader, int packetLength,
			List<XBeeResponseListener> listeners, Log log) {

		int frameType = reader.readByte();

		int bytesLeft = packetLength - 1;

		switch (frameType) {
		case XBeeApiConstants.FRAME_TYPE_AT_LOCAL_RESPONSE:
			signalAtLocalResponse(endpoint,
					parser.parseAtLocalResponse(reader, bytesLeft, log),
					listeners, log);
			break;
		case XBeeApiConstants.FRAME_TYPE_AT_REMOTE_RESPONSE:
			signalAtRemoteResponse(endpoint,
					parser.parseAtRemoteResponse(reader, bytesLeft, log),
					listeners, log);
			break;
		case XBeeApiConstants.FRAME_TYPE_TX_STATUS:
			signalTxStatus(endpoint,
					parser.parseTxStatus(reader, bytesLeft, log), listeners,
					log);
			break;
		case XBeeApiConstants.FRAME_TYPE_RX_RECEIVE:
			signalRxResponse(endpoint,
					parser.parseReceiveResponse(reader, bytesLeft, log),
					listeners, log);
			break;
		default:
			log.warn(String.format("Unknown frame type %d\n", frameType));
		}
	}

	/**
	 * Signal an AT local response.
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
	private void signalAtLocalResponse(XBeeCommunicationEndpoint endpoint,
			AtLocalResponseXBeeFrame response,
			List<XBeeResponseListener> listeners, Log log) {
		for (XBeeResponseListener listener : listeners) {
			try {
				listener.onAtLocalXBeeResponse(endpoint, response);
			} catch (Exception e) {
				log.error(
						"Error during call to listener for XBee AT Remote reponse",
						e);
			}
		}
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
	 * Signal a TX status.
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
	private void signalTxStatus(XBeeCommunicationEndpoint endpoint,
			TxStatusXBeeFrame response, List<XBeeResponseListener> listeners,
			Log log) {
		for (XBeeResponseListener listener : listeners) {
			try {
				listener.onTxStatusXBeeResponse(endpoint, response);
			} catch (Exception e) {
				log.error("Error during call to listener for XBee TX response",
						e);
			}
		}
	}
}
