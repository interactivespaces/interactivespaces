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
 * A listener for XBee responses
 *
 * @author Keith M. Hughes
 */
public interface XBeeResponseListener {

  /**
   * A local AT response has been received.
   *
   * @param endpoint
   *          the endpoint that received the response
   * @param response
   *          the response frame
   */
  void onAtLocalXBeeResponse(XBeeCommunicationEndpoint endpoint, AtLocalResponseXBeeFrame response);

  /**
   * A remote AT response has been received.
   *
   * @param endpoint
   *          the endpoint that received the response
   * @param response
   *          the response frame
   */
      void
      onAtRemoteXBeeResponse(XBeeCommunicationEndpoint endpoint, AtRemoteResponseXBeeFrame response);

  /**
   * A TX status has been received.
   *
   * @param endpoint
   *          the endpoint that received the response
   * @param response
   *          the status frame
   */
  void onTxStatusXBeeResponse(XBeeCommunicationEndpoint endpoint, TxStatusXBeeFrame response);

  /**
   * An RX response has been received.
   *
   * @param endpoint
   *          the endpoint that received the response
   * @param response
   *          the response frame
   */
  void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint, RxResponseXBeeFrame response);

  /**
   * An RX IO Sample has been received.
   *
   * @param endpoint
   *          the endpoint that received the response
   * @param response
   *          the response frame
   */
  void onRxIoSampleXBeeResponse(XBeeCommunicationEndpoint endpoint, RxIoSampleXBeeFrame response);
}
