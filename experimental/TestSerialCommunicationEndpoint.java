/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.service.comm.serial.test;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;

import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A serial connection useful for testing.
 *
 * @author Keith M. Hughes
 */
public class TestSerialCommunicationEndpoint implements SerialCommunicationEndpoint {

  private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  private LinkedList<byte[]> arrays = Lists.newLinkedList();

  private int readPos = 0;

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public String getPortName() {
    return "FAKE";
  }

  @Override
  public int available() {
    if (arrays.isEmpty()) {
      return 0;
    } else {
      return 12;
    }
  }

  @Override
  public int read() {
    if (arrays.isEmpty()) {
      return -1;
    } else {
      return 12;
    }
  }

  @Override
  public int read(byte[] buffer) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void flush() {
    // Nothing to do
  }

  @Override
  public void write(int b) {
    outputStream.write(b);
  }

  @Override
  public void write(byte[] b) {
    try {
      outputStream.write(b);
    } catch (IOException e) {
      throw new InteractiveSpacesException("Error while writing test serial", e);
    }
  }

  @Override
  public void write(byte[] b, int offset, int length) {
    outputStream.write(b, offset, length);
  }

  @Override
  public SerialCommunicationEndpoint setBaud(int baud) {
    return this;
  }

  @Override
  public SerialCommunicationEndpoint setDataBits(int dataBits) {
    return this;
  }

  @Override
  public SerialCommunicationEndpoint setStopBits(int stopBits) {
    return this;
  }

  @Override
  public SerialCommunicationEndpoint setParity(Parity parity) {
    return this;
  }

  @Override
  public SerialCommunicationEndpoint setInputBufferSize(int size) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public SerialCommunicationEndpoint setOutputBufferSize(int size) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public SerialCommunicationEndpoint setFlowControl(FlowControl... flowControls) {
    // TODO Auto-generated method stub
    return this;
  }

  public SerialCommunicationEndpoint addData(byte[] data) {
    return addData(data, 0, data.length);
  }

  public SerialCommunicationEndpoint addData(byte[] data, int offset) {
    return addData(data, offset, data.length - offset);
  }

  public SerialCommunicationEndpoint addData(byte[] data, int offset, int length) {
    byte[] dataToAdd = new byte[length];
    System.arraycopy(data, offset, dataToAdd, 0, length);

    arrays.add(dataToAdd);

    return this;
  }

  public SerialCommunicationEndpoint addData(ByteArrayOutputStream stream) {
    arrays.add(stream.toByteArray());

    return this;
  }

  public byte[] getOutputData() {
    return outputStream.toByteArray();
  }
}
