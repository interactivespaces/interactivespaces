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

import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.AtLocalRequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.AtRemoteRequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.TxRequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeAddress16;
import interactivespaces.service.comm.serial.xbee.XBeeAddress64;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeResponseListener;
import interactivespaces.util.concurrency.CancellableLoop;

import org.apache.commons.logging.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An Interactive Spaces implementation of an XBee communication endpoint.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesXBeeCommunicationEndpoint implements XBeeCommunicationEndpoint {

  /**
   * The communication endpoint for speaking with the XBee.
   */
  private SerialCommunicationEndpoint commEndpoint;

  /**
   * reader for the XBee frames.
   */
  private EscapedXBeeFrameReader reader;

  /**
   * Parser for response frames.
   */
  private ResponseXBeeFrameHandler frameHandler = new SimpleResponseXBeeFrameHandler();

  /**
   * The listeners for the endpoint.
   */
  private List<XBeeResponseListener> listeners = new CopyOnWriteArrayList<XBeeResponseListener>();

  /**
   * The executor service for running the reader loop.
   */
  private ScheduledExecutorService executorService;

  /**
   * Log for the endpoint.
   */
  private Log log;

  /**
   * Loop for reading info from the XBee.
   */
  private CancellableLoop readerLoop;

  /**
   * A generator for frame numbers.
   */
  private AtomicInteger autoFrameNumber = new AtomicInteger(1);

  /**
   * Construct a new endpoint.
   *
   * @param commEndpoint
   *          the serial endpoint
   * @param executorService
   *          the executor service to use
   * @param log
   *          the logger to use
   */
  public InteractiveSpacesXBeeCommunicationEndpoint(SerialCommunicationEndpoint commEndpoint,
      ScheduledExecutorService executorService, Log log) {
    this.commEndpoint = commEndpoint;
    this.executorService = executorService;
    this.log = log;

    this.reader = new EscapedXBeeFrameReader(commEndpoint);
  }

  @Override
  public void startup() {
    log.info(String.format("Starting up XBee connection with serial connection %s", commEndpoint));
    commEndpoint.startup();

    readerLoop = new CancellableLoop() {
      @Override
      protected void loop() throws InterruptedException {
        readFrame();
      }

      @Override
      protected void handleException(Exception e) {
        log.error("Error while reading XBee frame", e);
      }

      @Override
      protected void cleanup() {
        log.info("XBee serial connection read loop shut down");
      }
    };

    executorService.submit(readerLoop);
  }

  @Override
  public void shutdown() {
    log.info(String.format("Shutting down XBee connection with serial connection %s", commEndpoint));

    if (readerLoop != null) {
      readerLoop.cancel();

      readerLoop = null;
    }

    if (commEndpoint != null) {
      commEndpoint.shutdown();
      commEndpoint = null;
    }
  }

  @Override
  public void addListener(XBeeResponseListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(XBeeResponseListener listener) {
    listeners.remove(listener);
  }

  @Override
  public XBeeAddress16 newXBeeAddress16(String address) {
    return new XBeeAddress16Impl(address);
  }

  @Override
  public XBeeAddress16 newXBeeAddress16(int a1, int a2) {
    return new XBeeAddress16Impl(a1, a2);
  }

  @Override
  public XBeeAddress16 getBroadcastAddress16() {
    return XBeeAddress16Impl.BROADCAST_ADDRESS;
  }

  @Override
  public XBeeAddress64 newXBeeAddress64(String address) {
    return new XBeeAddress64Impl(address);
  }

  @Override
  public XBeeAddress64 newXBeeAddress64(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
    return new XBeeAddress64Impl(a1, a2, a3, a4, a5, a6, a7, a8);
  }

  @Override
  public XBeeAddress64 getCoordinatorAddress() {
    return XBeeAddress64Impl.COORDINATOR_ADDRESS;
  }

  @Override
  public XBeeAddress64 getBroadcastAddress64() {
    return XBeeAddress64Impl.BROADCAST_ADDRESS;
  }

  @Override
  public AtLocalRequestXBeeFrame newAtLocalRequestXBeeFrame(int[] command) {
    return newAtLocalRequestXBeeFrame(command, autoFrameNumber.getAndIncrement());
  }

  @Override
  public AtLocalRequestXBeeFrame newAtLocalRequestXBeeFrame(int[] command, int frameNumber) {
    return new AtLocalRequestXBeeFrameImpl(command, frameNumber);
  }

  @Override
  public AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16,
      int[] command, int options) {
    return newAtRemoteRequestXBeeFrame(address64, address16, command, autoFrameNumber.getAndIncrement(), options);
  }

  @Override
  public AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrameNoResponse(XBeeAddress64 address64,
      XBeeAddress16 address16, int[] command, int options) {
    return newAtRemoteRequestXBeeFrame(address64, address16, command, 0, options);
  }

  @Override
  public AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16,
      int[] command, int frameNumber, int options) {
    return new AtRemoteRequestXBeeFrameImpl(address64, command, frameNumber, options);
  }

  @Override
  public AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, int[] command, int options) {
    return newAtRemoteRequestXBeeFrame(address64, command, autoFrameNumber.getAndIncrement(), options);
  }

  @Override
  public AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrameNoResponse(XBeeAddress64 address64, int[] command,
      int options) {
    return newAtRemoteRequestXBeeFrame(address64, command, 0, options);
  }

  @Override
  public AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, int[] command, int frameNumber,
      int options) {
    return new AtRemoteRequestXBeeFrameImpl(address64, command, frameNumber, options);
  }

  @Override
  public TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16,
      int broadcastRadius, int options) {
    return newTxRequestXBeeFrame(address64, address16, autoFrameNumber.getAndIncrement(), broadcastRadius, options);
  }

  @Override
  public TxRequestXBeeFrame newTxRequestXBeeFrameNoResponse(XBeeAddress64 address64, XBeeAddress16 address16,
      int broadcastRadius, int options) {
    return newTxRequestXBeeFrame(address64, address16, 0, broadcastRadius, options);
  }

  @Override
  public TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16, int frameNumber,
      int broadcastRadius, int options) {
    return new TxRequestXBeeFrameImpl(address64, address16, frameNumber, broadcastRadius, options);
  }

  @Override
  public TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, int broadcastRadius, int options) {
    return newTxRequestXBeeFrame(address64, autoFrameNumber.getAndIncrement(), broadcastRadius, options);
  }

  @Override
  public TxRequestXBeeFrame newTxRequestXBeeFrameNoResponse(XBeeAddress64 address64, int broadcastRadius, int options) {
    return newTxRequestXBeeFrame(address64, 0, broadcastRadius, options);
  }

  @Override
  public TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, int frameNumber, int broadcastRadius,
      int options) {
    return new TxRequestXBeeFrameImpl(address64, frameNumber, broadcastRadius, options);
  }

  /**
   * Read a frame from the connected XBee.
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  private void readFrame() throws InterruptedException {
    if (!reader.waitForStartFrame()) {
      log.warn("End of XBee serial stream detected");
      return;
    }

    frameHandler.handle(this, reader, listeners, log);
  }

  @Override
  public SerialCommunicationEndpoint getSerialCommunicationEndpoint() {
    return commEndpoint;
  }

  @Override
  public String toString() {
    return "InteractiveSpacesXBeeCommunicationEndpoint [serial=" + commEndpoint + "]";
  }
}
