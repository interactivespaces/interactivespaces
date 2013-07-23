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

package interactivespaces.hardware.driver.gaming.wilddivine;

import com.google.common.collect.Lists;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.hardware.driver.DriverSupport;
import interactivespaces.service.SimpleServiceRegistry;
import interactivespaces.service.comm.usb.UsbCommunicationEndpoint;
import interactivespaces.service.comm.usb.UsbCommunicationEndpointService;
import interactivespaces.service.comm.usb.internal.libusb4j.Usb4JavaUsbCommunicationEndpointService;
import interactivespaces.system.SimpleInteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;
import interactivespaces.util.concurrency.CancellableLoop;

import org.apache.commons.logging.impl.Jdk14Logger;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * A driver for the Wild Divine Iom biofeedback sensor.
 *
 * @author Keith M. Hughes
 */
public class IomDriver extends DriverSupport {

  public static void main(String[] args) {
    final SimpleInteractiveSpacesEnvironment spaceEnvironment = new SimpleInteractiveSpacesEnvironment();
    spaceEnvironment.setExecutorService(Executors.newScheduledThreadPool(100));
    SimpleServiceRegistry serviceRegistry = new SimpleServiceRegistry(spaceEnvironment);
    spaceEnvironment.setServiceRegistry(serviceRegistry);
    Configuration configuration = SimpleConfiguration.newConfiguration();
    spaceEnvironment.setLog(new Jdk14Logger("foo"));
    Usb4JavaUsbCommunicationEndpointService service = new Usb4JavaUsbCommunicationEndpointService();
    serviceRegistry.registerService(service);
    service.startup();

    IomDriver driver = new IomDriver();
    driver.prepare(spaceEnvironment, configuration, spaceEnvironment.getLog());

    driver.addListener(new IomDeviceListener() {

      @Override
      public void onEvent(IomDriver driver, double heartRateValue, double skinConductivityLevel) {
        spaceEnvironment.getLog().info(
            String.format("Heart is %f, skin is %f", heartRateValue, skinConductivityLevel));
      }
    });

    try {
      driver.startup();

      InteractiveSpacesUtilities.delay(20000);
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      driver.shutdown();
      spaceEnvironment.getExecutorService().shutdown();
    }
  }

  /**
   * Character for the beginning of a tag.
   */
  public static final byte CHARACTER_TAG_BEGIN = 0x3c;

  /**
   * Character for the end of a tag.
   */
  public static final byte CHARACTER_TAG_END = 0x3e;

  /**
   * Character for the the slash part of a tag.
   */
  public static final byte CHARACTER_SLASH_TAG = 0x5c;

  /**
   * Character for the RAW message tag.
   */
  public static final byte CHARACTER_RAW_START = 0x52;

  /**
   * Hex digit for A
   */
  public static final byte HEX_DIGIT_A = 'A';

  /**
   * Hex digit for F
   */
  public static final byte HEX_DIGIT_F = 'F';

  /**
   * Hex digit for 0
   */
  public static final byte HEX_DIGIT_0 = '0';

  /**
   * Hex digit for 9
   */
  public static final byte HEX_DIGIT_9 = '9';

  /**
   * Communication endpoint for talking to sensor.
   */
  private UsbCommunicationEndpoint endpoint;

  /**
   * The buffer for reading content into.
   *
   * <p>
   * The size of this buffer is the report size for the device.
   */
  private byte[] readBuffer;

  /**
   * The size of the read buffer, in bytes.
   */
  private int readBufferSize;

  /**
   * The current position in the read buffer.
   */
  private int curReadPosition;

  /**
   * How many bytes in the current packet are good?
   */
  private int curPacketSize;

  /**
   * The listeners for this driver.
   */
  private List<IomDeviceListener> listeners = Lists.newArrayList();

  /**
   * The sensor read loop
   */
  private CancellableLoop sensorReadLoop;

  @Override
  public void startup() {
    UsbCommunicationEndpointService service =
        spaceEnvironment.getServiceRegistry().getRequiredService(
            UsbCommunicationEndpointService.SERVICE_NAME);

    endpoint = service.newEndpoint("14fa", "0001");

    if (endpoint == null) {
      throw new InteractiveSpacesException("Cannot locate an Iom device on the USB bus");
    }

    // The IOM reader is second endpoint for this port.
    endpoint.setEndpointIndex(1);

    endpoint.startup();

    readBuffer = endpoint.newBuffer();
    readBufferSize = endpoint.getReadBufferSize();

    sensorReadLoop = new CancellableLoop() {

      @Override
      protected void loop() throws InterruptedException {
        readSensorMessage();
      }
    };

    // Having no idea where the message stream might be, just read
    // until the end of the current message in progress.
    readUntilEndOfMessage();

    spaceEnvironment.getExecutorService().execute(sensorReadLoop);
  }

  @Override
  public void shutdown() {
    if (endpoint != null) {
      sensorReadLoop.cancel();

      endpoint.shutdown();
    }
  }

  /**
   * Add a new listener to the driver.
   *
   * @param listener
   *          the listener to add
   */
  public void addListener(IomDeviceListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove a listener from the driver.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  public void removeListener(IomDeviceListener listener) {
    listeners.remove(listener);
  }

  /**
   * Read a new message from the sensor.
   */
  private void readSensorMessage() {
    while (true) {
      // System.out.println("Reading from character " + curReadPosition);
      byte b = readByte();
      if (b != CHARACTER_TAG_BEGIN) {
        // System.out
        // .println("Did not find open tag begin in expected place");
        return;
      }

      // System.out.println("Found message begin");

      b = readByte();
      if (b == CHARACTER_RAW_START) {
        // Skip over AW>
        readByte();
        readByte();
        b = readByte();
        // System.out.println(Integer.toHexString(b) + " "
        // + Integer.toHexString(CHARACTER_TAG_END));
        if (b != CHARACTER_TAG_END) {
          // System.out
          // .println("Did not find open tag end in expected place");
          return;
        }

        byte a1 = readByte();
        byte a2 = readByte();
        byte b1 = readByte();
        byte b2 = readByte();
        readByte(); // Just a space, can be ignored
        byte c1 = readByte();
        byte c2 = readByte();
        byte d1 = readByte();
        byte d2 = readByte();
        b = readByte();
        if (b != CHARACTER_TAG_BEGIN) {
          // System.out
          // .println("Did not find close tag begin in expected place");
          return;
        }

        // Just consume the rest
        readUntilEndOfMessage();

        // Now have all the message bits. Reconstruct the message.
        int a1v = a1 - ((a1 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int a2v = a2 - ((a2 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int b1v = b1 - ((b1 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int b2v = b2 - ((b2 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int c1v = c1 - ((c1 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int c2v = c2 - ((c2 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int d1v = d1 - ((d1 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);
        int d2v = d2 - ((d2 > HEX_DIGIT_9) ? (HEX_DIGIT_A - 10) : HEX_DIGIT_0);

        double heartRateValue = (((a1v << 8 | a2v) << 8) | (b1v << 8 | b2v)) * 0.01;
        double skinConductivityLevel = (((c1v << 8 | c2v) << 8) | (d1v << 8 | d2v)) * 0.001;

        signalListeners(heartRateValue, skinConductivityLevel);

        break;
      } else {
        // System.out.println("Have message "
        // + new String(new byte[] { b }));
        // Ignore any other messages
        readUntilEndOfMessage();
      }
    }
  }

  /**
   * Signal all listeners with the sensor values.
   *
   * @param heartRateValue
   *          the heart rate value
   * @param skinConductivityLevel
   *          the skin conductivity value
   */
  public void signalListeners(double heartRateValue, double skinConductivityLevel) {
    for (IomDeviceListener listener : listeners) {
      try {
        listener.onEvent(this, heartRateValue, skinConductivityLevel);
      } catch (Exception e) {
        log.error("Error while running IomDevice listener", e);
      }
    }
  }

  /**
   * Read and throw away bytes until the end of the current message.
   */
  private void readUntilEndOfMessage() {
    // Read to the end of the current message
    while (true) {
      // First look for the end of a tag
      while (readByte() != CHARACTER_TAG_END)
        ;

      byte b = readByte();
      if (b == 0x0a) {
        b = readByte();
        if (b == 0x0d) {
          // System.out.println("Found end of message ");
          break;
        }
      }
    }
  }

  /**
   * Read a byte from the endpoint
   *
   * @return the byte read
   *
   * @throws InteractiveSpacesException
   *           some sort of read error has happened
   */
  private byte readByte() {
    try {
      if (curReadPosition >= curPacketSize) {
        int length = endpoint.readReportSync(readBuffer);

        curPacketSize = (readBuffer[0] & 0xff) + 1;
        // System.out.println("CurPacketSize " + curPacketSize);

        // Want to start at position 1 because first byte is
        // ignored.
        curReadPosition = 1;

        // System.out.println(ByteUtils.toHexString(readBuffer));
        // byte[] b = new byte[curPacketSize - 1];
        // System.arraycopy(readBuffer, 1, b, 0, curPacketSize - 1);
        // System.out.println(new String(b));
      }

      return readBuffer[curReadPosition++];
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not read USB device", e);
    }
  }

}
