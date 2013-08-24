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

package interactivespaces.service.comm.network.client;

import org.jboss.netty.buffer.ChannelBuffer;

import java.net.InetSocketAddress;

/**
 * A packet to be sent to a remote UDP server.
 *
 * @author Keith M. Hughes
 */
public interface UdpPacket {

  /**
   * Write out the packet to the remote address.
   *
   * @param remoteAddress
   *          address where the packet will be sent
   */
  void write(InetSocketAddress remoteAddress);


  /**
   * Sets the specified byte at the current {@code writerIndex}
   * and increases the {@code writerIndex} by {@code 1} in this buffer.
   * The 24 high-order bits of the specified value are ignored.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 1}
   */
  void writeByte(int   value);

  /**
   * Sets the specified 16-bit short integer at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 2}
   * in this buffer.  The 16 high-order bits of the specified value are ignored.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 2}
   */
  void writeShort(int value);

  /**
   * Sets the specified 24-bit medium integer at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 3}
   * in this buffer.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 3}
   */
  void writeMedium(int   value);

  /**
   * Sets the specified 32-bit integer at the current {@code writerIndex}
   * and increases the {@code writerIndex} by {@code 4} in this buffer.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 4}
   */
  void writeInt(int   value);

  /**
   * Sets the specified 64-bit long integer at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 8}
   * in this buffer.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 8}
   */
  void writeLong(long  value);

  /**
   * Sets the specified 2-byte UTF-16 character at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 2}
   * in this buffer.  The 16 high-order bits of the specified value are ignored.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 2}
   */
  void writeChar(int value);

  /**
   * Sets the specified 32-bit floating point number at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 4}
   * in this buffer.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 4}
   */
  void writeFloat(float value);

  /**
   * Sets the specified 64-bit floating point number at the current
   * {@code writerIndex} and increases the {@code writerIndex} by {@code 8}
   * in this buffer.
   *
   * @throws IndexOutOfBoundsException
   *         if {@code this.writableBytes} is less than {@code 8}
   */
  void writeDouble(double value);

  /**
   * Transfers the specified source array's data to this buffer starting at
   * the current {@code writerIndex} and increases the {@code writerIndex}
   * by the number of the transferred bytes (= {@code src.length}).
   *
   * @throws IndexOutOfBoundsException
   *         if {@code src.length} is greater than {@code this.writableBytes}
   */
  void writeBytes(byte[] src);

  /**
   * Transfers the specified source array's data to this buffer starting at
   * the current {@code writerIndex} and increases the {@code writerIndex}
   * by the number of the transferred bytes (= {@code length}).
   *
   * @param srcIndex the first index of the source
   * @param length   the number of bytes to transfer
   *
   * @throws IndexOutOfBoundsException
   *         if the specified {@code srcIndex} is less than {@code 0},
   *         if {@code srcIndex + length} is greater than
   *            {@code src.length}, or
   *         if {@code length} is greater than {@code this.writableBytes}
   */
  void writeBytes(byte[] src, int srcIndex, int length);
}
