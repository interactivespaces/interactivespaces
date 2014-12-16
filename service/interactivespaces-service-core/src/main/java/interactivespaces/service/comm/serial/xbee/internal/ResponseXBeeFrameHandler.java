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

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * A parser for XBee response frames.
 *
 * @author Keith M. Hughes
 */
public interface ResponseXBeeFrameHandler {

  /**
   * Parse the frame and send to the listeners.
   *
   * @param endpoint
   *          the endpoint which got the frame
   * @param reader
   *          the reader for frame bytes
   * @param listeners
   *          the listeners to send the response to
   * @param log
   *          the logger for the parsing
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  void handle(XBeeCommunicationEndpoint endpoint, EscapedXBeeFrameReader reader,
      List<XBeeResponseListener> listeners, Log log) throws InterruptedException;
}
