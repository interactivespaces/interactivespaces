/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.logging;

import interactivespaces.InteractiveSpacesExceptionUtils;

import org.apache.commons.logging.Log;

/**
 * A logger with extended functionality compared to the typical Apache Commons logger.
 *
 * @author Keith M. Hughes
 */
public interface ExtendedLog extends Log {

  /**
   * Log a formatted trace message.
   *
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedTrace(String message, Object... args);

  /**
   * Log a formatted trace message.
   *
   * <p>
   * The throwable will be formatted using the rules in
   * {@link InteractiveSpacesExceptionUtils#getExceptionDetail(Throwable)}.
   *
   * @param throwable
   *          the exception to be added to the message
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedTrace(Throwable throwable, String message, Object... args);

  /**
   * Log a formatted debug message.
   *
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedDebug(String message, Object... args);

  /**
   * Log a formatted debug message.
   *
   * <p>
   * The throwable will be formatted using the rules in
   * {@link InteractiveSpacesExceptionUtils#getExceptionDetail(Throwable)}.
   *
   * @param throwable
   *          the exception to be added to the message
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedDebug(Throwable throwable, String message, Object... args);

  /**
   * Log a formatted info message.
   *
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedInfo(String message, Object... args);

  /**
   * Log a formatted info message.
   *
   * <p>
   * The throwable will be formatted using the rules in
   * {@link InteractiveSpacesExceptionUtils#getExceptionDetail(Throwable)}.
   *
   * @param throwable
   *          the exception to be added to the message
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedInfo(Throwable throwable, String message, Object... args);

  /**
   * Log a formatted warn message.
   *
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedWarn(String message, Object... args);

  /**
   * Log a formatted warn message.
   *
   * <p>
   * The throwable will be formatted using the rules in
   * {@link InteractiveSpacesExceptionUtils#getExceptionDetail(Throwable)}.
   *
   * @param throwable
   *          the exception to be added to the message
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedWarn(Throwable throwable, String message, Object... args);

  /**
   * Log a formatted error message.
   *
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedError(String message, Object... args);

  /**
   * Log a formatted error message.
   *
   * <p>
   * The throwable will be formatted using the rules in
   * {@link InteractiveSpacesExceptionUtils#getExceptionDetail(Throwable)}.
   *
   * @param throwable
   *          the exception to be added to the message
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedError(Throwable throwable, String message, Object... args);

  /**
   * Log a formatted fatal message.
   *
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedFatal(String message, Object... args);

  /**
   * Log a formatted fatal message.
   *
   * <p>
   * The throwable will be formatted using the rules in
   * {@link InteractiveSpacesExceptionUtils#getExceptionDetail(Throwable)}.
   *
   * @param throwable
   *          the exception to be added to the message
   * @param message
   *          the formatted message string
   * @param args
   *          the arguments to be formatted
   */
  void formattedFatal(Throwable throwable, String message, Object... args);
}
