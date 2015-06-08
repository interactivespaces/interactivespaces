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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An Interactive Spaces exception happened.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesException extends RuntimeException {

  /**
   * Get the stack trace from an exception as a string.
   *
   * @param t
   *          the exception
   *
   * @return the stack trace
   */
  public static String getStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

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
  public static InteractiveSpacesException newFormattedException(String message, Object... args) {
    return new InteractiveSpacesException(String.format(message, args));
  }

  /**
   * Throw a new formatted exception.
   *
   * @param message
   *          the message
   * @param args
   *          the args
   *
   * @throws InteractiveSpacesException
   *           the exception just created
   */
  public static void throwFormattedException(String message, Object... args) throws InteractiveSpacesException {
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
  public static InteractiveSpacesException newFormattedException(Throwable cause, String message, Object... args) {
    return new InteractiveSpacesException(String.format(message, args), cause);
  }

  /**
   * Create an exception using the given message.
   *
   * @param message
   *          message for exception
   */
  public InteractiveSpacesException(String message) {
    super(message);
  }

  /**
   * Create an exception with message and cause.
   *
   * @param message
   *          message for exception
   * @param cause
   *          underlying cause of exception
   */
  public InteractiveSpacesException(String message, Throwable cause) {
    super(message, cause);
  }
}
