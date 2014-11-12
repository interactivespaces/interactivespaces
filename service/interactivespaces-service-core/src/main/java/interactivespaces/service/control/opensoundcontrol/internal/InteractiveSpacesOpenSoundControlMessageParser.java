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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.comm.network.server.UdpServerRequest;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlConstants;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlIncomingMessage;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlOutgoingMessage;
import interactivespaces.service.control.opensoundcontrol.RespondableOpenSoundControlIncomingMessage;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Parser for reading a Open Sound control packet.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOpenSoundControlMessageParser {

  /**
   * Parse the request data to get the message.
   *
   * @param senderAddress
   *          address of the sender of the message
   * @param messageData
   *          the request data that has come in
   *
   * @return the server packet
   */
  public OpenSoundControlIncomingMessage parseMessage(InetSocketAddress senderAddress, byte[] messageData) {
    InteractiveSpacesOpenSoundControlIncomingMessage message =
        new InteractiveSpacesOpenSoundControlIncomingMessage(senderAddress, messageData);
    message.parse();

    return message;
  }

  /**
   * Parse the respondable request data to get the message.
   *
   * @param serverRequest
   *          the server request
   *
   * @return the request message
   */
  public RespondableOpenSoundControlIncomingMessage parseRespondableMessage(UdpServerRequest serverRequest) {
    InteractiveSpacesRespondableOpenSoundControlIncomingMessage message =
        new InteractiveSpacesRespondableOpenSoundControlIncomingMessage(serverRequest);
    message.parse();

    return message;
  }

  /**
   * The Interactive Spaces representation of the server packet.
   *
   * @author Keith M. Hughes
   */
  static class InteractiveSpacesOpenSoundControlIncomingMessage implements OpenSoundControlIncomingMessage {

    /**
     * The argument list for the OSC message when the call has no arguments.
     */
    public static final Object[] NO_ARGS = new Object[0];

    /**
     * The address of the sender.
     */
    private InetSocketAddress senderAddress;

    /**
     * The message data from the incoming packet.
     */
    private byte[] messageData;

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
     * @param senderAddress
     *          the address of the sender
     * @param messageData
     *          the message data
     */
    public InteractiveSpacesOpenSoundControlIncomingMessage(InetSocketAddress senderAddress, byte[] messageData) {
      this.senderAddress = senderAddress;
      this.messageData = messageData;
    }

    @Override
    public InetSocketAddress getSenderAddress() {
      return senderAddress;
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
    public int getIntArgument(int arg) throws InteractiveSpacesException {
      if (isIntArgument(arg)) {
        return (Integer) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not an int", arg));
      }
    }

    @Override
    public long getLongArgument(int arg) throws InteractiveSpacesException {
      if (isLongArgument(arg)) {
        return (Long) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a long", arg));
      }
    }

    @Override
    public float getFloatArgument(int arg) throws InteractiveSpacesException {
      if (isFloatArgument(arg)) {
        return (Float) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a float", arg));
      }
    }

    @Override
    public double getDoubleArgument(int arg) throws InteractiveSpacesException {
      if (isDoubleArgument(arg)) {
        return (Double) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a double", arg));
      }
    }

    @Override
    public String getStringArgument(int arg) throws InteractiveSpacesException {
      if (isStringArgument(arg)) {
        return (String) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a string", arg));
      }
    }

    @Override
    public byte[] getBlobArgument(int arg) throws InteractiveSpacesException {
      if (isBlobArgument(arg)) {
        return (byte[]) arguments[arg];
      } else {
        throw new SimpleInteractiveSpacesException(String.format(
            "The Open sound Control argument at position %d is not a blob", arg));
      }
    }

    @Override
    public boolean isIntArgument(int arg) throws InteractiveSpacesException {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Integer;
    }

    @Override
    public boolean isLongArgument(int arg) throws InteractiveSpacesException {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Long;
    }

    @Override
    public boolean isFloatArgument(int arg) throws InteractiveSpacesException {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Float;
    }

    @Override
    public boolean isDoubleArgument(int arg) throws InteractiveSpacesException {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof Double;
    }

    @Override
    public boolean isStringArgument(int arg) throws InteractiveSpacesException {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof String;
    }

    @Override
    public boolean isBlobArgument(int arg) throws InteractiveSpacesException {
      checkArgumentPosition(arg);

      return arguments[arg] instanceof byte[];
    }

    @Override
    public String toString() {
      return "InteractiveSpacesOpenSoundControlServerPacket [address=" + address + ", arguments="
          + Arrays.toString(arguments) + "]";
    }

    /**
     * Parse the packet.
     */
    protected void parse() {
      address = parseString();

      parseArguments();
    }

    /**
     * Parse the arguments out of the packet.
     */
    private void parseArguments() {
      // See if there are arguments.
      if (parsePos < messageData.length
          && messageData[parsePos] == OpenSoundControlConstants.OPEN_SOUND_CONTROL_TYPE_STRING_DESIGNATOR) {
        // +1 because we want to start after the comma.
        int argTypesStart = parsePos + 1;
        if (argTypesStart < messageData.length) {
          findStringEnd();
          int argTypesEnd = parsePos;
          moveToNextBoundaryFromString();
          if (argTypesStart < argTypesEnd) {
            arguments = new Object[argTypesEnd - argTypesStart];

            ByteBuffer buffer =
                ByteBuffer.wrap(messageData).order(OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_ORDER);

            int argPos = 0;
            for (int typePos = argTypesStart; typePos < argTypesEnd; typePos++) {
              switch (messageData[typePos]) {
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_STRING:
                  arguments[argPos++] = parseString();
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT32:
                  arguments[argPos++] = buffer.getInt(parsePos);
                  parsePos += OpenSoundControlConstants.OPEN_SOUND_CONTROL_NUMBER_BYTES_INT32;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT64:
                  arguments[argPos++] = buffer.getLong(parsePos);
                  parsePos += OpenSoundControlConstants.OPEN_SOUND_CONTROL_NUMBER_BYTES_INT64;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT32:
                  arguments[argPos++] = buffer.getFloat(parsePos);
                  parsePos += OpenSoundControlConstants.OPEN_SOUND_CONTROL_NUMBER_BYTES_FLOAT32;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT64:
                  arguments[argPos++] = buffer.getFloat(parsePos);
                  parsePos += OpenSoundControlConstants.OPEN_SOUND_CONTROL_NUMBER_BYTES_FLOAT64;
                  break;
                case OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_BLOB:
                  arguments[argPos++] = parseBlob(buffer);
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

      String string = new String(messageData, startPos, parsePos - startPos);

      moveToNextBoundaryFromString();

      return string;
    }

    /**
     * Find the end of a string. {@code parsePos} will be left at the 0 terminator.
     */
    private void findStringEnd() {
      while (parsePos < messageData.length && messageData[parsePos] != 0) {
        parsePos++;
      }
    }

    /**
     * Move to the next parse boundary.
     */
    private void moveToNextBoundaryFromString() {
      parsePos +=
          OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY
              - (parsePos % OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY);
    }

    /**
     * Get a blob out of the OSC packet starting at the current parsing position.
     *
     * <p>
     * The parsing position will end up where the next part of the packet will start.
     *
     * @param buffer
     *          the buffer for translating data
     *
     * @return the string
     */
    private byte[] parseBlob(ByteBuffer buffer) {
      int length = buffer.getInt(parsePos);
      parsePos += OpenSoundControlConstants.OPEN_SOUND_CONTROL_NUMBER_BYTES_INT32;

      byte[] blob = new byte[length];
      System.arraycopy(messageData, parsePos, blob, 0, length);
      parsePos += length;

      int byteBoundaryPos = parsePos % OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY;
      if (byteBoundaryPos != 0) {
        parsePos += OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY - byteBoundaryPos;
      }

      return blob;
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

  /**
   * The Interactive Spaces representation of a respondable OSC incoming message.
   *
   * @author Keith M. Hughes
   */
  static class InteractiveSpacesRespondableOpenSoundControlIncomingMessage extends
      InteractiveSpacesOpenSoundControlIncomingMessage implements RespondableOpenSoundControlIncomingMessage {

    /**
     * The UDP server request.
     */
    private UdpServerRequest serverRequest;

    /**
     * Construct a new message.
     *
     * @param serverRequest
     *          the server request that generated this message
     */
    public InteractiveSpacesRespondableOpenSoundControlIncomingMessage(UdpServerRequest serverRequest) {
      super(serverRequest.getRemoteAddress(), serverRequest.getRequest());

      this.serverRequest = serverRequest;
    }

    @Override
    public OpenSoundControlOutgoingMessage sendResponseMessage(String address, Object... arguments) {
      OpenSoundControlOutgoingMessage message = newResponseMessage(address, arguments);
      message.send();

      return message;
    }

    @Override
    public OpenSoundControlOutgoingMessage newResponseMessage(String address, Object... arguments) {
      byte[] types = null;
      if (arguments != null && arguments.length > 0) {
        types = new byte[arguments.length];

        for (int arg = 0; arg < arguments.length; arg++) {
          Object argument = arguments[arg];
          if (argument instanceof String) {
            types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_STRING;
          } else if (argument instanceof Integer) {
            types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT32;
          } else if (argument instanceof Long) {
            types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT64;
          } else if (argument instanceof Float) {
            types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT32;
          } else if (argument instanceof Double) {
            types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT64;
          } else if (argument instanceof byte[]) {
            types[arg] = OpenSoundControlConstants.OPEN_SOUND_CONTROL_ARGUMENT_TYPE_BLOB;
          }
        }
      }

      OpenSoundControlOutgoingMessage message = newResponseMessageWithTypes(address, types);

      if (arguments != null && arguments.length > 0) {
        for (int arg = 0; arg < arguments.length; arg++) {
          Object argument = arguments[arg];
          if (argument instanceof String) {
            message.addString((String) argument);
          } else if (argument instanceof Integer) {
            message.addInt((Integer) argument);
          } else if (argument instanceof Long) {
            message.addLong((Long) argument);
          } else if (argument instanceof Float) {
            message.addFloat((Float) argument);
          } else if (argument instanceof Double) {
            message.addDouble((Double) argument);
          } else if (argument instanceof byte[]) {
            message.addBlob((byte[]) argument);
          } else {
            throw new SimpleInteractiveSpacesException(String.format(
                "Unsupported data type in Open Sound Control message %s", argument.getClass().getName()));
          }
        }
      }

      return message;
    }

    @Override
    public OpenSoundControlOutgoingMessage newResponseMessageWithTypes(String address, String types) {
      return new InteractiveSpacesOscOutgoingMessage(serverRequest.newDynamicWriteableUdpPacket(),
          serverRequest.getRemoteAddress(), address, types);
    }

    @Override
    public OpenSoundControlOutgoingMessage newResponseMessageWithTypes(String address, byte... types) {
      return new InteractiveSpacesOscOutgoingMessage(serverRequest.newDynamicWriteableUdpPacket(),
          serverRequest.getRemoteAddress(), address, types);
    }

  }
}
