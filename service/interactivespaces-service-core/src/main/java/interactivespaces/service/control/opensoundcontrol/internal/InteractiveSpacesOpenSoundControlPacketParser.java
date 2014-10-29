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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlConstants;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlServerPacket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Parser for reading a Open Sound control packet.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOpenSoundControlPacketParser {

  /**
   * Parse the request data to get the packet.
   *
   * @param requestData
   *          the request data which has come in
   *
   * @return the server packet
   */
  public OpenSoundControlServerPacket parsePacket(byte[] requestData) {
    InteractiveSpacesOpenSoundControlServerPacket packet =
        new InteractiveSpacesOpenSoundControlServerPacket(requestData);
    packet.parse();

    return packet;
  }

  /**
   * The Interactive Spaces representation of the server packet.
   *
   * @author Keith M. Hughes
   */
  static class InteractiveSpacesOpenSoundControlServerPacket implements OpenSoundControlServerPacket {

    /**
     * The character which starts an OSC packet type string.
     *
     * TODO(keith): Move to OpenSoundControlConstants when goes into Core.
     */
    public static final byte OPEN_SOUND_CONTROL_TYPE_STRING_DESIGNATOR = 0x2c;

    /**
     * The number of bytes in an OSC int32.
     *
     * <p>
     * TODO(keith): Move into OSCConstants.
     */
    public static final int OPEN_SOUND_CONTROL_NUMBER_BYTES_INT32 = 4;

    /**
     * The number of bytes in an OSC int64.
     *
     * <p>
     * TODO(keith): Move into OSCConstants.
     */
    public static final int OPEN_SOUND_CONTROL_NUMBER_BYTES_INT64 = 8;

    /**
     * The number of bytes in an OSC float32.
     *
     * <p>
     * TODO(keith): Move into OSCConstants.
     */
    public static final int OPEN_SOUND_CONTROL_NUMBER_BYTES_FLOAT32 = 4;

    /**
     * The number of bytes in an OSC float64.
     *
     * <p>
     * TODO(keith): Move into OSCConstants.
     */
    public static final int OPEN_SOUND_CONTROL_NUMBER_BYTES_FLOAT64 = 8;

    /**
     * The size of an OSC byte boundary.
     *
     * <p>
     * TODO(keith): Move into OSCConstants.
     */
    public static final int OPEN_SOUND_CONTROL_BYTE_BOUNDARY = 4;

    /**
     * The argument list for the OSC message when the call has no arguments.
     */
    public static final Object[] NO_ARGS = new Object[0];

    /**
     * The request data from the incoming packet.
     */
    private byte[] requestData;

    /**
     * The OSC packet for this address.
     */
    private String address;

    /**
     * The Open Sound control arguments for the packet.
     */
    private Object[] arguments;

    /**
     * Current position for parsing.
     */
    private int parsePos;

    /**
     * Construct a new server packet.
     *
     * @param requestData
     *          the request data
     */
    public InteractiveSpacesOpenSoundControlServerPacket(byte[] requestData) {
      this.requestData = requestData;
    }

    @Override
    public String getAddress() {
      return address;
    }

    @Override
    public int getNumberArguments() {
      return arguments.length;
    }

    @Override
    public Object[] getArguments() {
      return arguments;
    }

    @Override
    public int getIntArgument(int arg) {
      if (isIntArgument(arg)) {
        return (Integer) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not an int", arg));
      }
    }

    @Override
    public long getLongArgument(int arg) {
      if (isLongArgument(arg)) {
        return (Long) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a long", arg));
      }
    }

    @Override
    public float getFloatArgument(int arg) {
      if (isFloatArgument(arg)) {
        return (Float) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a float", arg));
      }
    }

    @Override
    public double getDoubleArgument(int arg) {
      if (isDoubleArgument(arg)) {
        return (Double) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a double", arg));
      }
    }

    @Override
    public String getStringArgument(int arg) {
      if (isStringArgument(arg)) {
        return (String) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a string", arg));
      }
    }

    @Override
    public boolean isIntArgument(int arg) {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Integer;
    }

    @Override
    public boolean isLongArgument(int arg) {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Long;
    }

    @Override
    public boolean isFloatArgument(int arg) {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Float;
    }

    @Override
    public boolean isDoubleArgument(int arg) {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Double;
    }

    @Override
    public boolean isStringArgument(int arg) {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof String;
    }

    @Override
    public String toString() {
      return "InteractiveSpacesOpenSoundControlServerPacket [address=" + address + ", arguments="
          + Arrays.toString(arguments) + "]";
    }

    /**
     * Parse the packet.
     */
    private void parse() {
      address = parseString();

      parseArguments();
    }

    /**
     * Parse the arguments out of the packet.
     */
    private void parseArguments() {
      // See if there are arguments.
      if (parsePos < requestData.length && requestData[parsePos] == OPEN_SOUND_CONTROL_TYPE_STRING_DESIGNATOR) {
        // +1 because we want to start after the comma.
        int argTypesStart = parsePos + 1;
        if (argTypesStart < requestData.length) {
          findStringEnd();
          int argTypesEnd = parsePos;
          moveToNextBoundary();
          if (argTypesStart < argTypesEnd) {
            arguments = new Object[argTypesEnd - argTypesStart];

            ByteBuffer buffer = ByteBuffer.wrap(requestData).order(ByteOrder.BIG_ENDIAN);

            int argPos = 0;
            for (int typePos = argTypesStart; typePos < argTypesEnd; typePos++) {
              switch (requestData[typePos]) {
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_STRING:
                  arguments[argPos++] = parseString();
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT32:
                  arguments[argPos++] = buffer.getInt(parsePos);
                  parsePos += OPEN_SOUND_CONTROL_NUMBER_BYTES_INT32;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT64:
                  arguments[argPos++] = buffer.getLong(parsePos);
                  parsePos += OPEN_SOUND_CONTROL_NUMBER_BYTES_INT64;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT32:
                  arguments[argPos++] = buffer.getFloat(parsePos);
                  parsePos += OPEN_SOUND_CONTROL_NUMBER_BYTES_FLOAT32;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT64:
                  arguments[argPos++] = buffer.getFloat(parsePos);
                  parsePos += OPEN_SOUND_CONTROL_NUMBER_BYTES_FLOAT64;
                  break;
                default:
                  throw new SimpleInteractiveSpacesException(String.format("Unsupported Open Sound Control type"));
              }
            }
          } else {
            // Is type, but then packet ends. That's fine.
            arguments = NO_ARGS;
          }
        } else {
          // Is type, but then packet ends. That's fine.
          arguments = NO_ARGS;
        }
      } else {
        arguments = NO_ARGS;
      }
    }

    /**
     * Get a string out of the OSC packet starting at the current parsing position.
     *
     * <p>
     * The parsing position will end up where the next part of the packet will start.
     *
     * @return the string
     */
    private String parseString() {
      int startPos = parsePos;

      findStringEnd();

      String string = new String(requestData, startPos, parsePos - startPos);

      moveToNextBoundary();

      return string;
    }

    /**
     * Find the end of a string. {@code parsePos} will be left at the 0 terminator.
     */
    private void findStringEnd() {
      while (parsePos < requestData.length && requestData[parsePos] != 0) {
        parsePos++;
      }
    }

    /**
     * Move to the next parse boundary.
     */
    private void moveToNextBoundary() {
      parsePos += OPEN_SOUND_CONTROL_BYTE_BOUNDARY - (parsePos % OPEN_SOUND_CONTROL_BYTE_BOUNDARY);
    }

    /**
     * Check whether an argument number is in range for the arguments in the packet and throw an exception if out of
     * range.
     *
     * @param arg
     *          the argument to check
     */
    private void checkArgumentPosition(int arg) {
      if (arg < 0 || arg >= arguments.length) {
        throw new SimpleInteractiveSpacesException(String.format(
            "Argument out of range for Open Sound Control packets, position is %d, number arguments is %d", arg,
            arguments.length));
      }
    }
  }
}
