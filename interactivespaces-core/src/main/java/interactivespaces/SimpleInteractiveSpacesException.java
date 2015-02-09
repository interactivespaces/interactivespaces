/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * A simple exception which probably doesn't need to send a stack trace.
 *
 * @author Keith M. Hughes
 */
public class SimpleInteractiveSpacesException extends InteractiveSpacesException {

  /**
   * Create a new formatted exception.
   *
   * @param message
   *          the message
   * @param args
   *          the args
   *
   * @return the new message
   */
  public static SimpleInteractiveSpacesException newFormattedException(String message, Object... args) {
    return new SimpleInteractiveSpacesException(String.format(message, args));
  }

  /**
   * Create a new formatted exception.
   *
   * @param message
   *          the message
   * @param args
   *          the args
   *
   * @throws SimpleInteractiveSpacesException
   *           the exception just created
   */
  public static void throwFormattedException(String message, Object... args) throws SimpleInteractiveSpacesException {
    throw newFormattedException(message, args);
  }

  /**
   * Create a new formatted exception.
   *
   * @param cause
   *          the cause of the exception
   * @param message
   *          the message
   * @param args
   *          the args
   *
   * @return the new message
   */
  public static SimpleInteractiveSpacesException newFormattedException(Throwable cause, String message, Object... args) {
    return new SimpleInteractiveSpacesException(String.format(message, args), cause);
  }

  /**
   * Create a new formatted exception.
   *
   * @param cause
   *          the cause of the exception
   * @param message
   *          the message
   * @param args
   *          the args
   *
   * @throws SimpleInteractiveSpacesException
   *           the exception just created
   */
  public static void throwFormattedException(Throwable cause, String message, Object... args) throws SimpleInteractiveSpacesException {
    throw newFormattedException(cause, message, args);
  }

  /**
   * Create a simple exception using the given message.
   *
   * @param message
   *          message for exception
   */
  public SimpleInteractiveSpacesException(String message) {
    super(message);
  }

  /**
   * Create a simple exception with message and cause.
   *
   * @param message
   *          message for exception
   * @param cause
   *          underlying cause of exception
   */
  public SimpleInteractiveSpacesException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Return a compounded message for this simple exception, including all the messages from the cause stack.
   *
   * @return compound exception message, separated by newlines
   */
  public String getCompoundMessage() {
    StringBuilder message = new StringBuilder();
    Throwable cause = getCause();
    while (cause != null) {
      String causeMessage = cause.getMessage();
      if (causeMessage != null) {
        message.append(causeMessage);
      } else {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        cause.printStackTrace(writer);
        writer.flush();
        message.append("\n").append(outputStream.toString());
      }
      message.append("\n");
      cause = cause.getCause();
    }
    return message.append(getMessage()).toString();
  }
}
